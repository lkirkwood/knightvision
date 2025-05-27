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
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
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
import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import kotlinx.serialization.Serializable
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.NavType
import androidx.navigation.toRoute

import com.knightvision.ui.screens.AnalysisScreen
import com.knightvision.ui.screens.ScanBoardScreen
import com.knightvision.ui.screens.BoardDetectionScreen
import com.knightvision.ui.screens.BoardEditingScreen
import com.knightvision.ui.screens.WelcomeScreen
import com.knightvision.ui.screens.SettingsScreen
import com.knightvision.ui.theme.ChessVisionTheme


class MainActivity : ComponentActivity() {
    private val CAMERA_PERM_REQUEST_CODE = 42

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            ChessVisionTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ChessVisionApp()
                }
            }
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

@Composable
fun ChessVisionApp() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "welcome") {
        composable("welcome") {
            WelcomeScreen(
                onScanBoardClick = { navController.navigate("scan") },
                onUploadImage = { navController.navigate("boardDetection") },
                onPreviousAnalysisClick = { navController.navigate("previous") },
                onSettingsClick = { navController.navigate("settings") }
            )
        }
        
        composable("scan") {
            ScanBoardScreen(
                onBackClick = { navController.popBackStack() },
                onPictureTaken = { navController.navigate("boardDetection") }
            )
        }
        
        composable("boardDetection") {
            BoardDetectionScreen(
                onBackClick = { navController.popBackStack() },
                onAnalyseClick = { navController.navigate("analysis") },
                onEditBoardClick = { navController.navigate("boardEditing") }
            )
        }

        composable("boardEditing") {
            BoardEditingScreen(
                onSave = { navController.popBackStack() }
            )
        }

        composable("analysis") {
            AnalysisScreen(
                onBackClick = { navController.popBackStack() },
            )
        }

        composable("settings") {
            SettingsScreen(
                onBackClick = { navController.navigate("welcome") }
            )
        }

        composable("previous") {
            // Placeholder for the previous analysis screen
            PlaceholderScreen("Previous Analysis Screen")
        }

    }
}


@Composable
fun PlaceholderScreen(screenName: String) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Text(
            text = screenName,
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.fillMaxSize()
        )
    }
}


