package com.knightvision

import android.os.Bundle
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.material3.Text
import androidx.activity.compose.setContent
import androidx.activity.ComponentActivity
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable

import com.knightvision.ui.screens.AnalysisScreen
import com.knightvision.ui.screens.ScanBoardScreen
import com.knightvision.ui.screens.BoardDetectionScreen
import com.knightvision.ui.screens.BoardEditingScreen
import com.knightvision.ui.screens.WelcomeScreen
import com.knightvision.ui.screens.SettingsScreen
import com.knightvision.ui.theme.ChessVisionTheme


class MainActivity : ComponentActivity() {
    private val CAMERA_PERM_REQUEST_CODE = 42

    @RequiresApi(Build.VERSION_CODES.P)
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

@RequiresApi(Build.VERSION_CODES.P)
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
                onBackClick = { navController.popBackStack() },
                onSaveClick = { navController.popBackStack() }
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


