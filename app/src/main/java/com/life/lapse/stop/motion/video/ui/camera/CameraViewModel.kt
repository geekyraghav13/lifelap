package com.life.lapse.stop.motion.video.ui.camera

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.Executor

data class CameraUiState(
    val capturedImages: List<Uri> = emptyList(),
    val cameraSelector: CameraSelector = CameraSelector.DEFAULT_BACK_CAMERA,
    val isCapturing: Boolean = false
)

class CameraViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(CameraUiState())
    val uiState = _uiState.asStateFlow()

    fun takePhoto(
        context: Context,
        imageCapture: ImageCapture,
        executor: Executor,
        onSuccess: (Uri) -> Unit,
        onError: (ImageCaptureException) -> Unit
    ) {
        Log.d("CameraViewModel", "takePhoto called. Setting isCapturing = true")
        _uiState.update { it.copy(isCapturing = true) }

        val photoFile = createFile(context)
        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        imageCapture.takePicture(
            outputOptions,
            executor,
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    val savedUri = outputFileResults.savedUri ?: Uri.fromFile(photoFile)
                    Log.d("CameraViewModel", "✅ Photo capture SUCCEEDED: $savedUri")
                    _uiState.update { currentState ->
                        currentState.copy(
                            capturedImages = currentState.capturedImages + savedUri,
                            isCapturing = false
                        )
                    }
                    onSuccess(savedUri)
                }

                override fun onError(exception: ImageCaptureException) {
                    Log.e("CameraViewModel", "❌ Photo capture FAILED: ${exception.message}", exception)
                    _uiState.update { it.copy(isCapturing = false) }
                    onError(exception)
                }
            }
        )
    }

    fun onFlipCameraClicked() {
        Log.d("CameraViewModel", "Flip camera button clicked.")
        _uiState.update { currentState ->
            val newSelector = if (currentState.cameraSelector == CameraSelector.DEFAULT_BACK_CAMERA) {
                CameraSelector.DEFAULT_FRONT_CAMERA
            } else {
                CameraSelector.DEFAULT_BACK_CAMERA
            }
            currentState.copy(cameraSelector = newSelector)
        }
    }

    private fun createFile(context: Context): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(System.currentTimeMillis())
        val filesDir = context.filesDir
        return File(filesDir, "LapseFrame_${timeStamp}.jpg")
    }
}
