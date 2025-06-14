package com.lts360.compose.ui.chat.camera

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Matrix
import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.camera.core.AspectRatio
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.MirrorMode
import androidx.camera.core.Preview
import androidx.camera.core.SurfaceRequest
import androidx.camera.core.UseCase
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.FileDescriptorOutputOptions
import androidx.camera.video.MediaStoreOutputOptions
import androidx.camera.video.Quality
import androidx.camera.video.QualitySelector
import androidx.camera.video.Recorder
import androidx.camera.video.Recording
import androidx.camera.video.VideoCapture
import androidx.camera.video.VideoRecordEvent
import androidx.core.content.ContextCompat
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.exoplayer.ExoPlayer
import com.lts360.R
import com.lts360.components.utils.LogUtils.TAG
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.concurrent.Executors
import kotlin.math.roundToInt

class ChatCameraXViewModel(val context: Context) :
    ViewModel() {

    private val _isCameraPermissionGranted = MutableStateFlow(hasCameraPermissionGranted())
    val isCameraPermissionGranted = _isCameraPermissionGranted.asStateFlow()


    private val _isMicPermissionGranted = MutableStateFlow(hasMicPermissionGranted())
    val isMicPermissionGranted = _isMicPermissionGranted.asStateFlow()

    private var cameraProvider: ProcessCameraProvider? = null

    private val _cameraSelector = MutableStateFlow(CameraSelector.DEFAULT_BACK_CAMERA)
    val cameraSelector = _cameraSelector.asStateFlow()

    private var previewUseCase: Preview? = null

    private val _surfaceRequest = MutableStateFlow<SurfaceRequest?>(null)
    val surfaceRequest = _surfaceRequest.asStateFlow()

    private var imageCaptureUseCase: ImageCapture? = null

    private val recorder = Recorder.Builder()
        .setExecutor(Executors.newSingleThreadExecutor()).setQualitySelector(
            QualitySelector.from(
                Quality.HIGHEST
            )
        ).setAspectRatio(AspectRatio.RATIO_16_9).build()


    private val _videoCaptureUseCase = MutableStateFlow<VideoCapture<Recorder>?>(
        VideoCapture.Builder(recorder)
            .setMirrorMode(MirrorMode.MIRROR_MODE_ON_FRONT_ONLY)
            .build()
    )
    val videoCaptureUseCase = _videoCaptureUseCase.asStateFlow()

    private val _camera = MutableStateFlow<Camera?>(null)
    val camera = _camera.asStateFlow()

    private val _flashEnabled = MutableStateFlow(false)
    val flashEnabled = _flashEnabled.asStateFlow()

    private val _isTakingPhoto = MutableStateFlow(false)
    val isTakingPhoto = _isTakingPhoto.asStateFlow()

    private val _recording = MutableStateFlow<Recording?>(null)
    val recording = _recording.asStateFlow()

    private val _isVideoRecording = MutableStateFlow(false)
    val isVideoRecording = _isVideoRecording.asStateFlow()

    private val _isVideoRecordPaused = MutableStateFlow(false)
    val isVideoRecordPaused = _isVideoRecordPaused.asStateFlow()

    private val _startTime = MutableStateFlow(0L)
    val startTime = _startTime.asStateFlow()

    private var pausedTime = 0L

    private val _seconds = MutableStateFlow(0)
    val seconds = _seconds.asStateFlow()

    private val _isBlinkingRecord = MutableStateFlow(false)
    val isBlinkingRecord: StateFlow<Boolean> = _isBlinkingRecord.asStateFlow()

    private val _currentItem = MutableStateFlow(0)
    val currentItem = _currentItem.asStateFlow()

    private val _lastCapturedUri = MutableStateFlow<Uri?>(null)
    val lastCapturedUri = _lastCapturedUri.asStateFlow()

    private val mediaPlayer = MediaPlayer.create(context, R.raw.shutter)


    var exoPlayer = ExoPlayer.Builder(context.applicationContext).build()


    fun updateCameraPermission(isPermissionGranted: Boolean) {
        _isCameraPermissionGranted.value = isPermissionGranted
    }


    fun updateMicPermission(isPermissionGranted: Boolean) {
        _isMicPermissionGranted.value = isPermissionGranted
    }

    fun updateSecondsRecorded(secondsRecorded: Int) {
        _seconds.value = secondsRecorded
    }

    fun updateBlinkingRecordingStatus(isBlinking: Boolean) {
        _isBlinkingRecord.value = isBlinking
    }

    fun updateImageCaptureUseCase(useCase: ImageCapture?) {
        viewModelScope.launch {
            imageCaptureUseCase = useCase
        }
    }

    fun updateCameraSelector(cameraSelector: CameraSelector) {
        _cameraSelector.value = cameraSelector
    }

    fun updateIsFlashEnabled(isFlashEnabled: Boolean) {
        _flashEnabled.value = isFlashEnabled
    }

    fun updateIsTakingPhoto(isTakingPhoto: Boolean) {
        _isTakingPhoto.value = isTakingPhoto
    }

    fun updateCurrentItem(currentItem: Int) {
        _currentItem.value = currentItem
    }

    fun updateLastCapturedUri(uri: Uri?) {
        _lastCapturedUri.value = uri
    }

    // Function to stop the capture (unbind camera use cases)
    private fun stopCapture(
        processCameraProvider: ProcessCameraProvider,
        invalidateUseCase: Boolean = false
    ) {
        // Unbind all use cases, which stops capturing
        processCameraProvider.unbindAll()
        _surfaceRequest.value = null
        previewUseCase = null
        if (invalidateUseCase) {
            imageCaptureUseCase = null
            _videoCaptureUseCase.value = null
        }
        _camera.value = null
        // Release the camera provider if no longer needed
        cameraProvider = null
    }

    // Function to start capturing
    fun startCapture(useCase: UseCase?, invalidateUseCase: Boolean = false) {
        viewModelScope.launch {
            cameraProvider?.let {
                stopCapture(it, invalidateUseCase)
            }

            val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
            cameraProvider = cameraProviderFuture.get()

            // Create Preview use case
            previewUseCase = Preview.Builder().build().apply {
                setSurfaceProvider { newSurfaceRequest ->
                    _surfaceRequest.value = newSurfaceRequest
                }
            }

            val useCases = mutableListOf(useCase)

            imageCaptureUseCase?.let {
                useCases.add(it)
            }
            // Bind the use cases to the camera lifecycle
            _camera.value = cameraProvider?.bindToLifecycle(
                ProcessLifecycleOwner.get(),
                _cameraSelector.value,
                previewUseCase,
                *useCases.toTypedArray()
            )
        }
    }


    fun takePhoto(
        onSuccess: (Uri) -> Unit,
        onError: () -> Unit
    ) {
        imageCaptureUseCase?.let {
            if (!_isTakingPhoto.value) {
                updateIsTakingPhoto(true)
                playShutter()
                it.takePicture(
                    ContextCompat.getMainExecutor(context),
                    object :
                        ImageCapture.OnImageCapturedCallback() {
                        override fun onCaptureSuccess(image: ImageProxy) {
                            super.onCaptureSuccess(image)

                            val isFrontCamera = _cameraSelector.value == CameraSelector.DEFAULT_FRONT_CAMERA

                            val resolver = context.contentResolver

                            val imageUri = resolver.insert(
                                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                                ContentValues().apply {
                                    put(
                                        MediaStore.Images.Media.DISPLAY_NAME,
                                        "IMG_${System.currentTimeMillis()}.jpg"
                                    )
                                    put(
                                        MediaStore.Images.Media.MIME_TYPE,
                                        "image/jpeg"
                                    )
                                    put(
                                        MediaStore.Images.Media.RELATIVE_PATH,
                                        Environment.DIRECTORY_DCIM + "/${
                                            context.getString(R.string.app_name)
                                        }"
                                    )
                                }
                            )

                            if (imageUri == null) {
                                updateIsTakingPhoto(false)
                                return
                            }


                            val capturedBitmap = image.toBitmap()

                            // If the front camera is used, flip the image on the X-axis (horizontally)
                            val finalBitmap = if (isFrontCamera) {

                                Bitmap.createBitmap(capturedBitmap, 0, 0, capturedBitmap.width, capturedBitmap.height,  Matrix().apply {
                                    postScale(-1f, 1f)
                                }, true)

                            } else {
                                capturedBitmap
                            }

                            saveImageToDCIMFolder(
                                context,
                                finalBitmap,
                                imageUri,
                                {
                                    updateIsTakingPhoto(false)
                                    onSuccess(imageUri)
                                }) {
                                updateIsTakingPhoto(false)
                                onError()
                            }

                        }

                        override fun onError(exception: ImageCaptureException) {
                            super.onError(exception)
                            exception.printStackTrace()
                            onError()
                            updateIsTakingPhoto(false)

                        }
                    }
                )
            }
        }
    }


    @RequiresPermission(android.Manifest.permission.RECORD_AUDIO)
    fun prepareVideoRecording(onCaptured: (Uri) -> Unit) {

        _recording.value = recorder.prepareRecording(
            context, MediaStoreOutputOptions.Builder(context.contentResolver,  MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
                .setContentValues(
                    ContentValues().apply {
                        put(
                            MediaStore.Images.Media.DISPLAY_NAME,
                            "VIDEO_${System.currentTimeMillis()}.mp4"
                        )
                        put(MediaStore.Images.Media.MIME_TYPE, "video/mp4")
                        put(
                            MediaStore.Images.Media.RELATIVE_PATH,
                            Environment.DIRECTORY_MOVIES + "/${context.getString(R.string.app_name)}"
                        )
                    }
                )
                .build()

        ).apply {
            withAudioEnabled()
        }.start(
            ContextCompat.getMainExecutor(context)
        ) { event: VideoRecordEvent ->
            when (event) {
                is VideoRecordEvent.Start -> {
                    _startTime.value = System.currentTimeMillis()
                    _isVideoRecording.value = true
                }

                is VideoRecordEvent.Pause -> {
                    pausedTime = System.currentTimeMillis()
                    _isVideoRecordPaused.value = true
                }

                is VideoRecordEvent.Resume -> {
                    _startTime.value += (System.currentTimeMillis() - pausedTime)
                    _isVideoRecordPaused.value = false
                }

                is VideoRecordEvent.Finalize -> {


                    _isVideoRecording.value = false
                    _isVideoRecordPaused.value = false

                    val elapsedSeconds =
                        ((System.currentTimeMillis() - _startTime.value) / 1000f).roundToInt()
                    if (_seconds.value != elapsedSeconds) {
                        _seconds.value = elapsedSeconds
                    }

                    if (event.hasError()) {
                        Log.e(TAG, "Video recording failed: ${event.error}")
                    } else {
                        val outPutUri= event.outputResults.outputUri
                        MediaScannerConnection.scanFile(context, arrayOf(outPutUri.path), null) { path, uri -> }
                        onCaptured(outPutUri)
                    }
                }

                is VideoRecordEvent.Status -> {

                }
            }
        }
    }


    private fun hasCameraPermissionGranted(): Boolean {
        return ContextCompat.checkSelfPermission(
            context, Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun hasMicPermissionGranted(): Boolean {
        return ContextCompat.checkSelfPermission(
            context, Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
    }


    private fun playShutter() {
        mediaPlayer.start()
    }


    private fun saveImageToDCIMFolder(
        context: Context,
        bitmap: Bitmap,
        out: Uri,
        onSuccess: () -> Unit,
        onError: () -> Unit
    ) {

        try {
            val resolver = context.contentResolver
            resolver.openOutputStream(out)?.use { outputStream ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
            }

            MediaScannerConnection.scanFile(context, arrayOf(out.path), null) { path, uri -> }

            onSuccess()
        } catch (e: Exception) {
            onError()
            e.printStackTrace()
        }

    }

    fun isImageOrVideo(context: Context, capturedUri: Uri?): String {
        if (capturedUri == null) return "Unknown"

        // Get MIME type from ContentResolver
        val mimeType = context.contentResolver.getType(capturedUri)
        return when {
            mimeType?.startsWith("image") == true -> "Image"
            mimeType?.startsWith("video") == true -> "Video"
            else -> "Unknown"
        }
    }

    fun getVideoDimensionsFromUri(context: Context, videoUri: Uri): Pair<Int, Int> {
        var videoWidth = 0
        var videoHeight = 0
        var rotation: Int

        try {
            MediaMetadataRetriever().apply {
                // Set data source directly from Uri
                setDataSource(context, videoUri)

                // Extract rotation, width, height, and duration
                rotation =
                    extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION)?.toIntOrNull()
                        ?: 0
                videoWidth =
                    extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)?.toInt() ?: -1
                videoHeight =
                    extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)?.toInt() ?: -1
            }

            // Adjust width and height for rotation (swap width and height if rotated 90° or 270°)
            if (rotation == 90 || rotation == 270) {
                val tempWidth = videoWidth
                videoWidth = videoHeight
                videoHeight = tempWidth
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }

        return Pair(videoWidth, videoHeight)
    }


    override fun onCleared() {
        super.onCleared()
        cameraProvider?.let {
            stopCapture(it)
        }
        mediaPlayer.release()
        exoPlayer.release()

    }

}
