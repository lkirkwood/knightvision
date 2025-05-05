package com.example.gui.ui.screens

import android.content.Context
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.LifecycleOwner
import androidx.compose.material3.*
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.foundation.layout.*
import androidx.camera.view.PreviewView
import android.view.ViewGroup

@Composable
fun ScanBoardScreen() {
    val context = LocalContext.current
    val lifecycleOwner = LocalContext.current as LifecycleOwner

    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "Scan ChessBoard",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(16.dp)
        )
        CameraPreviewView(context, lifecycleOwner)
    }
}

@Composable
fun CameraPreviewView(context: Context, lifecycleOwner: LifecycleOwner) {
    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { ctx ->
            val previewView = PreviewView(ctx)
            previewView.layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            previewView
        }
    )
}



