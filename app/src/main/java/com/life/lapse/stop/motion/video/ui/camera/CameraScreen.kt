package com.life.lapse.stop.motion.video.ui.camera

import android.Manifest
import android.content.Context
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import coil.compose.AsyncImage
import com.life.lapse.stop.motion.video.ui.editor.EditorViewModel
import com.life.lapse.stop.motion.video.ui.editor.Frame
import com.life.lapse.stop.motion.video.ui.theme.Pink_Primary
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.Executor

@Composable
fun CameraScreen(
    onNavigateBack: () -> Unit,
    onNavigateToEditor: () -> Unit,
    projectViewModel: EditorViewModel
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val uiState by projectViewModel.uiState.collectAsState()

    var hasPermission by remember { mutableStateOf(false) }
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted -> hasPermission = isGranted }
    )

    var cameraSelector by remember { mutableStateOf(CameraSelector.DEFAULT_BACK_CAMERA) }

    LaunchedEffect(Unit) {
        permissionLauncher.launch(Manifest.permission.CAMERA)
    }

    val imageCapture = remember { ImageCapture.Builder().build() }

    Box(modifier = Modifier.fillMaxSize()) {
        if (hasPermission) {
            CameraPreviewView(
                modifier = Modifier.fillMaxSize(),
                imageCapture = imageCapture,
                lifecycleOwner = lifecycleOwner,
                cameraSelector = cameraSelector
            )
            CameraControls(
                modifier = Modifier.fillMaxSize(),
                onNavigateBack = onNavigateBack,
                onCaptureClick = {
                    takePhoto(
                        context = context,
                        imageCapture = imageCapture,
                        executor = ContextCompat.getMainExecutor(context),
                        onImageCaptured = { uri ->
                            projectViewModel.addFrame(uri)
                            Toast.makeText(context, "Frame Added!", Toast.LENGTH_SHORT).show()
                        },
                        onError = { error ->
                            Log.e("CameraScreen", "Capture error: $error")
                        }
                    )
                },
                onFlipCameraClick = {
                    cameraSelector = if (cameraSelector == CameraSelector.DEFAULT_BACK_CAMERA) {
                        CameraSelector.DEFAULT_FRONT_CAMERA
                    } else {
                        CameraSelector.DEFAULT_BACK_CAMERA
                    }
                },
                capturedImages = uiState.frames,
                onDoneClick = onNavigateToEditor
            )
        } else {
            PermissionDeniedView()
        }
    }
}

fun takePhoto(
    context: Context,
    imageCapture: ImageCapture,
    executor: Executor,
    onImageCaptured: (Uri) -> Unit,
    onError: (ImageCaptureException) -> Unit
) {
    val photoFile = File.createTempFile(
        "LapseFrame_${System.currentTimeMillis()}",
        ".jpg",
        context.cacheDir
    )
    val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

    imageCapture.takePicture(
        outputOptions,
        executor,
        object : ImageCapture.OnImageSavedCallback {
            override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                val savedUri = outputFileResults.savedUri ?: Uri.fromFile(photoFile)
                onImageCaptured(savedUri)
            }
            override fun onError(exception: ImageCaptureException) {
                onError(exception)
            }
        }
    )
}


@Composable
fun CameraPreviewView(
    modifier: Modifier = Modifier,
    imageCapture: ImageCapture,
    lifecycleOwner: LifecycleOwner,
    cameraSelector: CameraSelector
) {
    val context = LocalContext.current
    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            PreviewView(ctx).apply { this.scaleType = PreviewView.ScaleType.FILL_CENTER }
        },
        update = { previewView ->
            val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()
                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }
                try {
                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, preview, imageCapture)
                } catch (e: Exception) {
                    Log.e("CameraPreviewView", "Use case binding failed", e)
                }
            }, ContextCompat.getMainExecutor(context))
        }
    )
}

@Composable
fun CameraControls(
    modifier: Modifier = Modifier,
    onNavigateBack: () -> Unit,
    onCaptureClick: () -> Unit,
    onFlipCameraClick: () -> Unit,
    capturedImages: List<Frame>,
    onDoneClick: () -> Unit
) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onNavigateBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White) }
            Row {
                IconButton(onClick = { /* TODO */ }) { Icon(Icons.Default.FlashOn, "Flash", tint = Color.White) }
                IconButton(onClick = onFlipCameraClick) { Icon(Icons.Default.PhotoCamera, "Flip Camera", tint = Color.White) }
                IconButton(onClick = { /* TODO */ }) { Icon(Icons.Default.GridOn, "Grid", tint = Color.White) }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        if (capturedImages.isNotEmpty()) {
            LazyRow(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                reverseLayout = true
            ) {
                items(capturedImages, key = { it.id }) { frame ->
                    AsyncImage(model = frame.uri, contentDescription = "Captured frame", modifier = Modifier.size(64.dp).clip(RoundedCornerShape(8.dp)), contentScale = ContentScale.Crop)
                }
            }
        } else {
            Box(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp), contentAlignment = Alignment.Center) {
                Text(text = "Captured frames will appear here", color = Color.White.copy(alpha = 0.7f))
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp, start = 16.dp, end = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.size(64.dp).clip(RoundedCornerShape(8.dp)).border(width = 1.dp, color = if (capturedImages.isNotEmpty()) Color.White else Color.Transparent, shape = RoundedCornerShape(8.dp))) {
                if (capturedImages.isNotEmpty()) {
                    AsyncImage(model = capturedImages.last().uri, contentDescription = "Last captured frame", modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                }
            }

            val shutterScale by animateFloatAsState(targetValue = if (false) 1.2f else 1.0f, label = "shutterScale")
            Box(contentAlignment = Alignment.Center) {
                IconButton(
                    onClick = onCaptureClick,
                    enabled = true,
                    modifier = Modifier.size(80.dp).scale(shutterScale).clip(CircleShape).background(Pink_Primary)
                ) {
                    Icon(Icons.Default.Camera, "Capture", tint = Color.White, modifier = Modifier.size(40.dp))
                }
            }

            IconButton(
                onClick = onDoneClick,
                modifier = Modifier.size(64.dp).clip(CircleShape).background(Color.DarkGray).border(2.dp, Color.White, CircleShape)
            ) {
                Icon(Icons.Default.Check, "Done", tint = Color.White)
            }
        }
    }
}

@Composable
fun PermissionDeniedView() {
    Box(modifier = Modifier.fillMaxSize().background(Color.Black), contentAlignment = Alignment.Center) {
        Text(text = "Camera permission is required to use this feature.", color = Color.White)
    }
}
