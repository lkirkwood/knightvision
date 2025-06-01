package com.knightvision.ui.screens

import androidx.compose.material3.*

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material.icons.filled.UploadFile

import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.activity.ComponentActivity
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi

import com.knightvision.ui.screens.BoardImageViewModel

@RequiresApi(Build.VERSION_CODES.P)
@Composable
fun WelcomeScreen(
    onScanBoardClick: () -> Unit,
    onUploadImage: () -> Unit,
    onPreviousAnalysisClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    val boardImgModel: BoardImageViewModel = viewModel(LocalContext.current as ComponentActivity)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) {
        TopAppBar(onSettingsClick = onSettingsClick)

        Spacer(modifier = Modifier.height(64.dp))

        Column(
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            // Scan Chessboard Button
            Button(
                onClick = { onScanBoardClick() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF4D4B6E)
                )
            ) {
                Icon(
                    imageVector = Icons.Default.AddCircle,
                    contentDescription = "Scan ChessBoard Icon",
                    tint = Color.White
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Scan Chessboard",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            UploadImage(
                onUpload = { bitmap ->
                    boardImgModel.orientation = "left"
                    boardImgModel.boardImage = bitmap
                    onUploadImage()
                }
            )


            Spacer(modifier = Modifier.height(32.dp))


            // Recent Games Section
            Text(
                text = "Recent Games",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                modifier = Modifier.padding(vertical = 8.dp)
            )


            RecentGameItem( // hard coded, needs to display actual recent games
                title = "Italian Game",
                date = "April 10, 2025",
                evaluation = "+1.3 (White)"
            )

            Spacer(modifier = Modifier.height(16.dp))


            RecentGameItem(
                title = "Sicilian Defense",
                date = "April 7, 2025",
                evaluation = "-0.5 (Black)"
            )

            Spacer(modifier = Modifier.height(16.dp))


            RecentGameItem(
                title = "Queen's Gambit",
                date = "April 3, 2025",
                evaluation = "+0.8 (White)"
            )

        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopAppBar(onSettingsClick: () -> Unit) {
    androidx.compose.material3.TopAppBar(
        title = {
            Text(
                text = "KnightVision",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        },
        actions = {
            IconButton(
                onClick = onSettingsClick
            ) {
                Icon(
                    imageVector = Icons.Filled.Settings,
                    contentDescription = "Settings",
                    tint = Color.White
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color(0xFF4D4B6E),
            titleContentColor = Color.White
        )
    )
}

@Composable
fun RecentGameItem(title: String, date: String, evaluation: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp)
    ) {

        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color.LightGray)
        ) {

            ChessBoardIcon()
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row {
                Text(
                    text = date,
                    fontSize = 14.sp,
                    color = Color.Gray
                )
                Text(
                    text = " · $evaluation",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }
        }
    }
}

@Composable
fun ChessBoardIcon() {
    Row(
        modifier = Modifier.fillMaxSize()
    ) {
        for (i in 0..3) {
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(1f)
            ) {
                for (j in 0..3) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .background(
                                if ((i + j) % 2 == 0) Color.LightGray else Color.DarkGray
                            )
                    )
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.P)
@Composable
fun UploadImage(onUpload: (Bitmap) -> Unit) {
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri: Uri? ->
            uri?.let {
                val source = ImageDecoder.createSource(context.contentResolver, uri)
                onUpload(ImageDecoder.decodeBitmap(source))
            }
        }
    )

    Button(
        onClick = { launcher.launch("image/*") },
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(28.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.LightGray
        )
    ) {
        Icon(
            imageVector = Icons.Default.UploadFile,
            contentDescription = "Upload image",
            tint = Color.DarkGray
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "Upload image to analyse",
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = Color.DarkGray
        )
    }
}

