package com.knightvision

import android.os.Bundle
import android.view.ViewGroup
import android.content.Context
import android.content.pm.PackageManager
import android.Manifest
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.Column
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.ui.viewinterop.AndroidView
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.core.Preview
import androidx.camera.core.CameraSelector
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.core.app.ActivityCompat
import androidx.activity.compose.setContent
import androidx.lifecycle.LifecycleOwner
import com.google.common.util.concurrent.ListenableFuture

@Composable
fun CameraPreview(cameraProvider : ProcessCameraProvider) {
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
                preview
            )

            previewView
        }
    )
}

// @Composable
// fun MainMenu(cameraProvider: ProcessCameraProvider) {
//     Column {
//         Button(onClick = {CameraPreview(cameraProvider)}) {
//             Text("Take a picture")
//         }
//     }
// }


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
                CameraPreview(cameraProvider)
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
