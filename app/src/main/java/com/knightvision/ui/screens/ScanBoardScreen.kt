package com.knightvision.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import android.view.ViewGroup
import android.widget.Toast
import android.os.Handler
import android.os.Looper
import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageProxy
import androidx.camera.core.ImageCaptureException
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.Image
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.layout.ContentScale
import androidx.core.content.ContextCompat
import androidx.camera.view.PreviewView
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.lifecycle.LifecycleOwner
import com.google.common.util.concurrent.ListenableFuture
import java.util.concurrent.Executors
import androidx.compose.ui.unit.dp

import com.knightvision.R

fun setupCamera(
    provider: ProcessCameraProvider, capture: ImageCapture, lifecycleOwner: LifecycleOwner, preview: Preview
) {
    try{
        // Unbind any previous use cases first
        provider.unbindAll()

        // Bind use cases to camera
        provider.bindToLifecycle(
            lifecycleOwner,
            CameraSelector.DEFAULT_BACK_CAMERA,
            preview,
            capture
        )
    } catch(ex: Exception) {
        Log.e("CameraPreview", "Failed to bind camera use cases", ex)
    }
}

@Composable
fun ScanBoardScreen(
    onBackClick: () -> Unit = {},
    onPictureTaken: (Bitmap) -> Unit = {}
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalContext.current as LifecycleOwner

    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }
    val cameraCapture = remember { ImageCapture.Builder().build() }

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    var cameraProviderReady by remember { mutableStateOf(false) }
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    cameraProviderFuture.addListener({ cameraProviderReady = true }, cameraExecutor)


    var cameraPreview by remember {
        if (hasCameraPermission) {
            mutableStateOf<Preview?>(Preview.Builder().build())
        } else {
            mutableStateOf<Preview?>(null)
        }
    }
    val previewView = remember { PreviewView(context) }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
        // Only gets launched if permission was false when composable was entered first time,
        // so we know we have to create preview
        cameraPreview = Preview.Builder().build()
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    var cameraBound by remember { mutableStateOf(false) }
    LaunchedEffect(cameraPreview, cameraProviderReady) {
        if (cameraProviderReady && cameraPreview != null) {
            setupCamera(cameraProviderFuture.get(), cameraCapture, lifecycleOwner, cameraPreview!!)
            cameraBound = true
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            if (cameraBound) {
                cameraProviderFuture.get().unbindAll()
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) {

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .background(Color(0xFF4D4B6E))
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = "Scan Board",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Camera Preview Area
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(Color(0xFFF5F5F5)),
            contentAlignment = Alignment.Center
        ) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { previewView }

            ) {
                cameraPreview?.setSurfaceProvider(previewView.surfaceProvider)
            }

            val barHeight = (this.constraints.maxHeight - this.constraints.maxWidth) / 2

            // Top black bar
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(with(LocalDensity.current) { barHeight.toDp() })
                    .align(Alignment.TopCenter)
                    .background(Color.Black)
            )

            // Bottom black bar
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(with(LocalDensity.current) { barHeight.toDp() })
                    .align(Alignment.BottomCenter)
                    .background(Color.Black)
            )

            // Instruction Text
            Text(
                text = "Center the chessboard within the frame and ensure all pieces are clearly visible",
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(26.dp),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )

            // Camera capture button
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 36.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .border(2.dp, Color(0xFF4D4B6E), CircleShape)
                        .clickable {
                            cameraCapture.takePicture(cameraExecutor, object : ImageCapture.OnImageCapturedCallback() {
                                override fun onCaptureSuccess(image : ImageProxy) {
                                    Handler(Looper.getMainLooper()).post {
                                        onPictureTaken(image.toBitmap())
                                    }
                                }

                                override fun onError(exception : ImageCaptureException) {
                                    Toast.makeText(context, exception.toString(), Toast.LENGTH_LONG).show()
                                }
                            })
                        }
                        .padding(4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(Color.White, CircleShape)
                    )
                }
            }
        }
    }
}
