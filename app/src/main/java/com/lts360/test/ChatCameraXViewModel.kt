package com.lts360.test

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.camera.core.Camera
import androidx.camera.core.CameraProvider
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.MirrorMode
import androidx.camera.core.Preview
import androidx.camera.core.SurfaceRequest
import androidx.camera.core.UseCase
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.FileDescriptorOutputOptions
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
import com.lts360.R
import com.lts360.components.utils.LogUtils.TAG
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.concurrent.Executors
import javax.inject.Inject
import kotlin.math.roundToInt

@HiltViewModel
class ChatCameraXViewModel @Inject constructor (@ApplicationContext val context: Context) : ViewModel() {


    private val _isPermissionGranted = MutableStateFlow(hasPermissionGranted())
    val isPermissionGranted = _isPermissionGranted.asStateFlow()

    private var cameraProvider:ProcessCameraProvider? = null

    private val _cameraSelector = MutableStateFlow(CameraSelector.DEFAULT_BACK_CAMERA)
    val cameraSelector = _cameraSelector.asStateFlow()

    var previewUseCase :Preview? = null

    private val _surfaceRequest = MutableStateFlow<SurfaceRequest?>(null)
    val surfaceRequest= _surfaceRequest.asStateFlow()

    private val _imageCaptureUseCase = MutableStateFlow<ImageCapture?>(null)
    val imageCaptureUseCase= _imageCaptureUseCase.asStateFlow()


    private val recorder = Recorder.Builder()
        .setExecutor(Executors.newSingleThreadExecutor()).setQualitySelector(
            QualitySelector.from(
                Quality.LOWEST
            )
        )
        .build()


    private val _videoCaptureUseCase = MutableStateFlow<VideoCapture<Recorder>?>(
        VideoCapture.Builder(recorder)
            .setMirrorMode(MirrorMode.MIRROR_MODE_ON_FRONT_ONLY)
            .build())
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

    private val _lastCaptureUri = MutableStateFlow<Uri?>(null)
    val lastCaptureUri = _lastCaptureUri.asStateFlow()

    private val mediaPlayer = MediaPlayer.create(context, R.raw.shutter)

    fun updatePermission(isPermissionGranted: Boolean){
        _isPermissionGranted.value = isPermissionGranted
    }

    fun updateSecondsRecorded(secondsRecorded: Int){
        _seconds.value = secondsRecorded
    }

    fun updateBlinkingRecordingStatus(isBlinking: Boolean){
        _isBlinkingRecord.value = isBlinking
    }

    fun updateImagePreviewUseCase(useCase:ImageCapture?){
        viewModelScope.launch {
            _imageCaptureUseCase.value = useCase
        }
    }

    fun updateCameraSelector(cameraSelector: CameraSelector){
        _cameraSelector.value = cameraSelector
    }

    fun updateIsFlashEnabled(isFlashEnabled:Boolean){
        _flashEnabled.value = isFlashEnabled
    }

    fun updateIsTakingPhoto(isTakingPhoto: Boolean){
        _isTakingPhoto.value = isTakingPhoto
    }

    fun updateCurrentItem(currentItem:Int) {
        _currentItem.value = currentItem
    }

    fun updateLastCapturedUri(uri:Uri?) {
        _lastCaptureUri.value = uri
    }

    // Function to stop the capture (unbind camera use cases)
    private fun stopCapture(processCameraProvider: ProcessCameraProvider, invalidateUseCase: Boolean = false) {
        // Unbind all use cases, which stops capturing
        processCameraProvider.unbindAll()
        _surfaceRequest.value = null
        previewUseCase = null
        if (invalidateUseCase) {
            _imageCaptureUseCase.value = null
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

            // Bind the use cases to the camera lifecycle
            _camera.value = cameraProvider?.bindToLifecycle(
                ProcessLifecycleOwner.get(),
                _cameraSelector.value,
                previewUseCase,
                useCase
            )
        }
    }



    fun prepareVideoRecording(onCaptured:(Uri)->Unit){
        val videoUri = context.contentResolver.insert(
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
            ContentValues().apply {
                put(
                    MediaStore.Images.Media.DISPLAY_NAME,
                    "VIDEO_${System.currentTimeMillis()}.mp4"
                )
                put(MediaStore.Images.Media.MIME_TYPE, "video/mpf")
                put(
                    MediaStore.Images.Media.RELATIVE_PATH,
                    Environment.DIRECTORY_MOVIES + "/${context.getString(R.string.app_name)}"
                )
            }
        )

        if (videoUri == null) {
            return
        }

        val fd = context.contentResolver.openFileDescriptor(videoUri, "rw")
            ?: return

        _recording.value = recorder.prepareRecording(
            context, FileDescriptorOutputOptions.Builder(fd)
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
                    Log.d(TAG, "Video recording started")
                }

                is VideoRecordEvent.Pause -> {
                    pausedTime = System.currentTimeMillis()
                    _isVideoRecordPaused.value = true
                    Log.d(TAG, "Video recording paused")
                }

                is VideoRecordEvent.Resume -> {
                    _startTime.value += (System.currentTimeMillis() - pausedTime)
                    _isVideoRecordPaused.value = false
                    Log.d(TAG, "Video recording resumed")
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
                        onCaptured(event.outputResults.outputUri)
                        Log.d(
                            TAG,
                            "Video saved to URI: ${event.outputResults.outputUri}"
                        )
                    }
                }

                is VideoRecordEvent.Status -> {

                }
            }
        }
    }



    // Function to check camera permission
    private fun hasPermissionGranted(): Boolean {
        return ContextCompat.checkSelfPermission(
            context, Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun playShutter(){
        mediaPlayer.start()
    }


    override fun onCleared() {
        super.onCleared()
    }

}
