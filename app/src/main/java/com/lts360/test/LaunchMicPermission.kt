package com.lts360.test


import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.media.audiofx.NoiseSuppressor
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LifecycleResumeEffect
import com.lts360.libs.imagepicker.utils.redirectToAppSettings
import com.lts360.libs.media.ui.permissions.PermissionRationaleRequestDialog
import com.lts360.libs.media.ui.permissions.PermissionRequestDialog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt


@SuppressLint("MutableCollectionMutableState")
@Composable
fun ChatRecordAudio() {

    // Audio recording states
    var isRecording by remember { mutableStateOf(false) }
    var isPaused by remember { mutableStateOf(false) }

    var audioData by remember { mutableStateOf(ByteArray(0)) }
    var currentUpdatedChunk by remember { mutableStateOf(ByteArray(0)) }

    var audioRecord: AudioRecord? by remember { mutableStateOf(null) }

    var startTime by remember { mutableLongStateOf(0L) }
    var pausedTime by remember { mutableLongStateOf(0L) }
    var seconds by remember { mutableIntStateOf(0) }
    var frequencies by remember { mutableStateOf<List<Float>>(emptyList()) }

    // Setting up constants for AudioRecord
    val sampleRate = 44100 // Sampling rate (44.1 kHz)
    val channelConfig = AudioFormat.CHANNEL_IN_MONO
    val audioFormat = AudioFormat.ENCODING_PCM_16BIT
    val bufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat)

    val context = LocalContext.current
    var job: Job? = remember { null }


    fun initAudioRecorder() {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        audioRecord = AudioRecord(
            MediaRecorder.AudioSource.VOICE_RECOGNITION,
            sampleRate,
            channelConfig,
            audioFormat,
            bufferSize
        )


    }


    // Handle the start of the recording
    val startRecording = {
        if (audioRecord == null) {
            initAudioRecorder()
        }
        // Start recording in a new thread
        startTime = System.currentTimeMillis()
        audioRecord?.startRecording()
        isRecording = true
        isPaused = false
        CoroutineScope(Dispatchers.IO).launch {
            while (isRecording && !isPaused) {
                val buffer = ByteArray(bufferSize)
                val bytesRead = audioRecord?.read(buffer, 0, buffer.size) ?: 0

                if (bytesRead > 0) {
                    val data = buffer.copyOfRange(0, bytesRead)
                    if (job == null || job?.isCompleted == true) {
                        if (job != null) {
                            job = null
                        }
                        job = launch {
                            val top3MaxAmplitudes = (0 until data.size / 2)
                                .map { i ->
                                    // Extract 16-bit samples (2 bytes per sample)
                                    val sample =
                                        ((data[i * 2].toInt() and 0xFF) or (data[i * 2 + 1].toInt() shl 8))
                                    abs(sample)
                                }
                                .take(3)  // Get the top 3 maximum amplitudes

                            frequencies = frequencies.toMutableList()
                                .apply {
                                    addAll(top3MaxAmplitudes.map { it.toFloat() })
                                }

                            delay(200)

                        }

                    }
                    currentUpdatedChunk = data
                    audioData += data
                }
            }
        }


    }

// Handle pause of the recording
    val pauseRecording = {
        audioRecord?.stop()
        pausedTime = System.currentTimeMillis()
        isRecording = false
        isPaused = true
    }

// Handle resume of the recording
    val resumeRecording = {
        audioRecord?.startRecording()
        startTime += (System.currentTimeMillis() - pausedTime)
        isPaused = false
        isRecording = true
        CoroutineScope(Dispatchers.IO).launch {
            while (isRecording && !isPaused) {
                val buffer = ByteArray(bufferSize)
                val bytesRead = audioRecord?.read(buffer, 0, buffer.size) ?: 0
                if (bytesRead > 0) {
                    val data = buffer.copyOfRange(0, bytesRead)
                    if (job == null || job?.isCompleted == true) {
                        if (job != null) {
                            job = null
                        }
                        job = launch {
                            val top3MaxAmplitudes = (0 until data.size / 2)
                                .map { i ->
                                    // Extract 16-bit samples (2 bytes per sample)
                                    val sample =
                                        ((data[i * 2].toInt() and 0xFF) or (data[i * 2 + 1].toInt() shl 8))
                                    abs(sample)
                                }
                                .take(3)  // Get the top 3 maximum amplitudes

                            frequencies = frequencies.toMutableList()
                                .apply {
                                    addAll(top3MaxAmplitudes.map { it.toFloat() })
                                }

                            delay(200)

                        }

                    }
                    currentUpdatedChunk = data
                    audioData += data
                }
            }
        }

    }

// Handle stopping the recording
    val stopRecording = {
        audioRecord?.stop()
        val elapsedSeconds = ((System.currentTimeMillis() - startTime) / 1000f).roundToInt()
        if (seconds != elapsedSeconds) {
            seconds = elapsedSeconds
        }
        isRecording = false
        isPaused = false
        audioRecord?.release()
        audioRecord = null
    }


    LaunchedEffect(isRecording) {
        audioRecord?.let {
            if (NoiseSuppressor.isAvailable()) {
                val noiseSuppressor = NoiseSuppressor.create(it.audioSessionId)
                noiseSuppressor.enabled = true // Enable noise suppression
            }
        }
    }


    LaunchedEffect(Unit) {
        startRecording()
    }

// Update the duration dynamically every second
    LaunchedEffect(isRecording, isPaused) {
        while (isRecording) {
            if (!isPaused) {
                val elapsedSeconds = ((System.currentTimeMillis() - startTime) / 1000f).roundToInt()
                if (seconds != elapsedSeconds) {
                    seconds = elapsedSeconds
                }
            }
            delay(50) // Update every second
        }
    }


    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {


        ChatAudioRecordDialog(
            isRecording,
            isPaused,
            seconds.toFloat(),
            frequencies,
            onPauseButtonClicked = {
                pauseRecording()
            },
            onResumeButtonClicked = {
                resumeRecording()
            },
            onSendButtonClicked = {
                stopRecording()
            },
            onDeleteButtonClicked = {
                stopRecording()
            })

    }
}


@Composable
fun RecordAudioWithPermission(onPermissionGranted: () -> Unit, onDismissRequest: () -> Unit) {

    val context = LocalContext.current

    val permissionRequest = Manifest.permission.RECORD_AUDIO

    var showRationale by remember {
        mutableStateOf(
            ActivityCompat.shouldShowRequestPermissionRationale(
                context as Activity,
                permissionRequest
            )
        )
    }


    val hasPermissionGranted: () -> Boolean = {
        ContextCompat.checkSelfPermission(
            context, permissionRequest
        ) == PackageManager.PERMISSION_GRANTED
    }

    // State to handle permission request
    val (permissionsGranted, setPermissionsGranted) = remember {
        mutableStateOf(
            hasPermissionGranted()
        )
    }

    var isShowingDialogRationale by rememberSaveable { mutableStateOf(false) }
    var isShowingPermissionRequestDialog by rememberSaveable { mutableStateOf(false) }


    // Register the permission request callback for multiple permissions
    val requestPermissionsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val micPermissionGranted = permissions[permissionRequest] == true

        // Update state based on the permissions result
        if (micPermissionGranted) {
            setPermissionsGranted(true)
        } else {
            setPermissionsGranted(false)
            showRationale = ActivityCompat.shouldShowRequestPermissionRationale(
                context as Activity,
                permissionRequest
            )
            if (!showRationale) {
                isShowingDialogRationale = true
            } else {
                isShowingPermissionRequestDialog = true
            }
            // Optionally, show rationale or notify the user why these permissions are required
        }
    }


    val requestPermission: () -> Unit = {
        requestPermissionsLauncher.launch(
            arrayOf(permissionRequest)
        )
    }

    LaunchedEffect(permissionsGranted) {
        if (permissionsGranted) {
            onPermissionGranted()
        }
    }


    LifecycleResumeEffect(Unit) {
        if (!permissionsGranted) {
            if (hasPermissionGranted()) {
                setPermissionsGranted(true)
            }
        }
        onPauseOrDispose {}
    }



    LaunchedEffect(Unit) {
        if (!permissionsGranted) {
            if (!showRationale) {
                isShowingDialogRationale = true
            } else {
                // Request both permissions at once
                requestPermission()
            }
        }
    }


    if (isShowingPermissionRequestDialog) {

        PermissionRequestDialog(
            Icons.Filled.Mic,
            "Mic",
            "To record voice mic permission is required",
            {
                isShowingPermissionRequestDialog = false
                requestPermission()
            },
            {
                isShowingPermissionRequestDialog = false
                onDismissRequest()
            })

    }

    if (isShowingDialogRationale) {

        PermissionRationaleRequestDialog(
            Icons.Filled.Mic,
            "Mic",
            "To record voice mic permission is required",
            {
                isShowingDialogRationale = false
                redirectToAppSettings(context)
                onDismissRequest()
            },
            {
                isShowingDialogRationale = false
                onDismissRequest()
            })

    }


}


