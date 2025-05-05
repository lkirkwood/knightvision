package com.example.gui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.gui.ui.theme.ChessVisionTheme
import com.example.gui.ui.screens.WelcomeScreen
import com.example.gui.ui.screens.ScanBoardScreen
import androidx.compose.material3.Text


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ChessVisionTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ChessVisionApp()
                }
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
                onPreviousAnalysisClick = { navController.navigate("previous") },
                onSettingsClick = { navController.navigate("settings") }
            )
        }

        // Other screens would be defined here
        composable("scan") {
            // Placeholder for the scan screen
            ScanBoardScreen()
        }

        composable("previous") {
            // Placeholder for the previous analysis screen
            PlaceholderScreen("Previous Analysis Screen")
        }

        composable("settings") {
            // Placeholder for the settings screen
            PlaceholderScreen("Settings Screen")
        }
    }
}


@Composable
fun PlaceholderScreen(screenName: String) {
    // A simple placeholder screen for demonstration
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

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    ChessVisionTheme {
        ChessVisionApp()
    }
}