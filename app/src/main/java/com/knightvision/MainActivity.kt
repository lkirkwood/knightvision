package com.knightvision

import android.os.Bundle
import android.os.Build
import android.view.ViewGroup
import android.content.Context
import android.content.pm.PackageManager
import android.Manifest
import android.widget.Toast
import android.graphics.Color
import android.graphics.Bitmap
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.OutlinedButton
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.core.Preview
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageProxy
import androidx.camera.core.ImageCaptureException
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.core.app.ActivityCompat
import androidx.activity.compose.setContent
import androidx.lifecycle.LifecycleOwner
import com.google.common.util.concurrent.ListenableFuture
import kotlinx.coroutines.newSingleThreadContext
import java.util.concurrent.Executors
import java.io.ByteArrayOutputStream
import com.knightvision.AnalysisActivity
import android.content.Intent

@Composable
fun CameraPreview(cameraProvider : ProcessCameraProvider, onPictureTaken : (ImageProxy) -> Unit) {
    val capture = ImageCapture.Builder().build()

    AndroidView(
        factory = { context ->
            val previewView = PreviewView(context).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                scaleType = PreviewView.ScaleType.FILL_CENTER
            }

            val preview = Preview.Builder().build()
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            preview.setSurfaceProvider(previewView.surfaceProvider)

            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(
                context as LifecycleOwner,
                cameraSelector,
                preview,
                capture
            )

            previewView
        }
    )

    val cameraExecutor = Executors.newSingleThreadExecutor()
    val context = LocalContext.current
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        OutlinedButton(shape = CircleShape, onClick = {
            capture.takePicture(cameraExecutor, object : ImageCapture.OnImageCapturedCallback() {
                override fun onCaptureSuccess(image : ImageProxy) {
                    onPictureTaken(image)
                }

                override fun onError(exception : ImageCaptureException) {
                    Toast.makeText(context, exception.toString(), Toast.LENGTH_LONG).show()
                }
            })
        }) {}
    }
}

class MainActivity : AppCompatActivity() {
    private lateinit var cameraProviderFuture : ListenableFuture<ProcessCameraProvider>
    private lateinit var cameraProvider : ProcessCameraProvider

    private val CAMERA_PERM_REQUEST_CODE = 42

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener(Runnable {
            cameraProvider = cameraProviderFuture.get()
            setContent {
                CameraPreview(cameraProvider, {image : ImageProxy -> Unit

                    val stream = ByteArrayOutputStream()
                    image.toBitmap().compress(Bitmap.CompressFormat.PNG, 100, stream)
                    val byteArray = stream.toByteArray()

                    val intent = Intent(this, AnalysisActivity::class.java)
                    intent.putExtra("image_bytes", byteArray)
                    this.startActivity(intent)
                })
            }
        }, ContextCompat.getMainExecutor(this))

        val cameraPerms = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
        if (cameraPerms != PackageManager.PERMISSION_GRANTED) {
            // Permission not granted
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CAMERA),
                CAMERA_PERM_REQUEST_CODE
            )
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERM_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Camera permission is required!", Toast.LENGTH_LONG).show()
            }
        }
    }
}
