package com.knightvision.ui.screens

import androidx.compose.material3.*

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.MoreVert

import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun KnightVisionApp() {
    WelcomeScreen(
        onScanBoardClick = {},
        onPreviousAnalysisClick = {},
        onSettingsClick = {},
        onDirectToBoardDetectionClick = {}
    )
}

@Composable
fun WelcomeScreen(
    onScanBoardClick: () -> Unit,
    onPreviousAnalysisClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onDirectToBoardDetectionClick: () -> Unit
) {


    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) {
        TopAppBar()

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
            Button(
                    onClick = { onDirectToBoardDetectionClick() },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(28.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF4D4B6E)
            )
            ) {
            Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = "Direct to Board Detection",
                tint = Color.White
            )
            Spacer(modifier = Modifier.width(32.dp))
            Text(
                text = "Direct to Board Detection",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White
            )
        }

            Spacer(modifier = Modifier.height(32.dp))

            Spacer(modifier = Modifier.height(32.dp))

            // Set Board from FEN Button
            Button(
                onClick = { /* Add import functionality */ },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.LightGray
                )
            ) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "Import Icon",
                    tint = Color.DarkGray
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Set Board from FEN",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.DarkGray
                )
            }

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

@Composable
fun TopAppBar() {
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
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // add app icon here

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = "KnightVision",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }



        }
    }
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

@Preview(showBackground = true)
@Composable
fun WelcomeScreenPreview() {
    WelcomeScreen(
        onScanBoardClick = {},
        onPreviousAnalysisClick = {},
        onSettingsClick = {},
        onDirectToBoardDetectionClick = {}
    )
}