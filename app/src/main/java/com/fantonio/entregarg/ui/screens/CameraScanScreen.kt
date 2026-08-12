package com.fantonio.entregarg.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageProxy
import androidx.camera.mlkit.vision.MlKitAnalyzer
import androidx.camera.view.CameraController.COORDINATE_SYSTEM_VIEW_REFERENCED
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.fantonio.entregarg.ui.viewmodel.IdentityViewModel
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.util.concurrent.Executors

@OptIn(ExperimentalGetImage::class)
@Composable
fun CameraScanScreen(
    viewModel: IdentityViewModel,
    onNavigateToReview: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }
    
    val textRecognizer = remember { TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS) }
    
    val cameraController = remember {
        LifecycleCameraController(context).apply {
            setEnabledUseCases(LifecycleCameraController.IMAGE_CAPTURE or LifecycleCameraController.IMAGE_ANALYSIS)
            setImageAnalysisAnalyzer(
                cameraExecutor,
                MlKitAnalyzer(
                    listOf(textRecognizer),
                    COORDINATE_SYSTEM_VIEW_REFERENCED,
                    cameraExecutor
                ) { result ->
                    val visionText = result.getValue(textRecognizer)
                    if (visionText is com.google.mlkit.vision.text.Text) {
                        viewModel.onTextDetected(visionText)
                    }
                }
            )
        }
    }
    
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }
    
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            hasCameraPermission = granted
        }
    )
    
    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            launcher.launch(Manifest.permission.CAMERA)
        }
    }
    
    DisposableEffect(lifecycleOwner) {
        cameraController.bindToLifecycle(lifecycleOwner)
        onDispose {
            cameraController.unbind()
        }
    }
    
    if (hasCameraPermission) {
        Box(modifier = Modifier.fillMaxSize()) {
            AndroidView(
                factory = { ctx ->
                    PreviewView(ctx).apply {
                        controller = cameraController
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
            
            // Real-time bounding boxes
            Canvas(modifier = Modifier.fillMaxSize()) {
                viewModel.detectedRects.forEach { rect ->
                    drawRect(
                        color = Color.Yellow,
                        topLeft = Offset(rect.left.toFloat(), rect.top.toFloat()),
                        size = Size(rect.width().toFloat(), rect.height().toFloat()),
                        style = Stroke(width = 2.dp.toPx())
                    )
                }
            }
            
            // Capture Button
            if (viewModel.isProcessingImage) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else {
                IconButton(
                    onClick = {
                        cameraController.takePicture(
                            cameraExecutor,
                            object : ImageCapture.OnImageCapturedCallback() {
                                @ExperimentalGetImage
                                override fun onCaptureSuccess(image: ImageProxy) {
                                    val mediaImage = image.image
                                    if (mediaImage != null) {
                                        val inputImage = InputImage.fromMediaImage(
                                            mediaImage,
                                            image.imageInfo.rotationDegrees
                                        )
                                        textRecognizer.process(inputImage)
                                            .addOnSuccessListener { visionText ->
                                                viewModel.processScannedImage(visionText) {
                                                    image.close()
                                                    onNavigateToReview()
                                                }
                                            }
                                            .addOnFailureListener {
                                                image.close()
                                            }
                                    } else {
                                        image.close()
                                    }
                                }
                            }
                        )
                    },
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 48.dp)
                        .size(80.dp)
                        .background(Color.White.copy(alpha = 0.5f), CircleShape)
                ) {
                    Icon(
                        Icons.Default.Camera,
                        contentDescription = "Capturar",
                        modifier = Modifier.size(48.dp),
                        tint = Color.Black
                    )
                }
            }
        }
    } else {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Permissão de câmera necessária")
        }
    }
}
