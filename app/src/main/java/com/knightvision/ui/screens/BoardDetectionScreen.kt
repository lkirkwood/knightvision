package com.knightvision.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material.icons.filled.ContentCopy

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BoardDetectionScreen(
    onBackClick: () -> Unit = {},
    imageUri: String = "",
    onAnalyseClick: () -> Unit = {},
    onEditBoardClick: () -> Unit = {},
    fenString: String = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR" // Default starting position
) {

    LaunchedEffect(imageUri) {
        if (imageUri.isNotEmpty()){
            // process image here
        }
    }
    // State for board information
    var detectedOpening by remember { mutableStateOf("Starting Position") }
    var piecesDetected by remember { mutableStateOf("32/32") }
    var evaluation by remember { mutableStateOf("") }
    var advantage by remember { mutableStateOf("Equal") }

    // Parse FEN to determine board state
    val boardState = remember(fenString) { parseFenToBoard(fenString) }
    val context = LocalContext.current
    fun copyToClipboard(text: String) {
        val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clipData = ClipData.newPlainText("FEN String", text)
        clipboardManager.setPrimaryClip(clipData)
        Toast.makeText(context, "FEN copied to clipboard", Toast.LENGTH_SHORT).show()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) {
        // Top App Bar
        TopAppBar(
            title = { Text("Board Detection") },
            navigationIcon = {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
            },

            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color(0xFF4D4B6E),
                titleContentColor = Color.White
            )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Detected Position Label
            Text(
                text = "Detected Position:",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color.DarkGray,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )

            // Chess Board
            ChessBoard(
                boardState = boardState,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .border(1.dp, Color.LightGray, RoundedCornerShape(8.dp))
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Position Information Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                ),
                shape = RoundedCornerShape(8.dp),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "FEN String",
                            fontSize = 16.sp,
                            color = Color.DarkGray
                        )

                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween

                    ) {
                        Text(
                            text = fenString,
                            fontSize = 14.sp,
                            color = Color.DarkGray,
                            maxLines = 1,
                            modifier = Modifier.weight(1f),
                        )
                        IconButton(
                           onClick = { copyToClipboard(fenString)},
                            modifier = Modifier.size(24.dp)
                        ){
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copy FEN",
                                tint = Color(0xFF4D4B6E)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Action Buttons
            Button(
                onClick = onAnalyseClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF4D4B6E)
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "Analyse Position Icon",
                    tint = Color.White
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Analyse Position",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedButton(
                onClick = onEditBoardClick, // TODO: add edit board screen
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color(0xFF4D4B6E)
                ),
                border = ButtonDefaults.outlinedButtonBorder.copy(
                    brush = androidx.compose.ui.graphics.SolidColor(Color(0xFF4D4B6E))
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit Board Icon",
                    tint = Color(0xFF4D4B6E)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Edit Board",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF4D4B6E)
                )
            }
        }
    }
}

@Composable
fun ChessBoard(
    boardState: Array<Array<Char>>,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        for (row in 0..7) {
            Row(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                for (col in 0..7) {
                    ChessSquare(
                        piece = boardState[row][col],
                        isLightSquare = (row + col) % 2 == 0,
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                    )
                }
            }
        }
    }
}

@Composable
fun ChessSquare(
    piece: Char,
    isLightSquare: Boolean,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(if (isLightSquare) Color(0xFFE8D0AA) else Color(0xFFB58863)),
        contentAlignment = Alignment.Center
    ) {
        if (piece != '.') {
            ChessPiece(piece = piece)
        }
    }
}

@Composable
fun ChessPiece(piece: Char) {
    // unicode chess symbols
    val isWhitePiece = piece.isUpperCase()
    val pieceSymbol = when (piece.lowercaseChar()) {
        'p' -> "♟"
        'r' -> "♜"
        'n' -> "♞"
        'b' -> "♝"
        'q' -> "♛"
        'k' -> "♚"
        else -> ""
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        Text(
            text = pieceSymbol,
            fontSize = 28.sp,
            color = if (isWhitePiece) Color.White else Color.Black,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Bold
        )
    }
}

// Function to parse FEN string to 2D board array
fun parseFenToBoard(fen: String): Array<Array<Char>> {
    val board = Array(8) { Array(8) { '.' } }
    val fenParts = fen.split(" ")
    val fenBoard = fenParts[0]
    val ranks = fenBoard.split("/")

    for (rankIndex in ranks.indices) {
        var fileIndex = 0
        for (c in ranks[rankIndex]) {
            if (c.isDigit()) {
                // Skip empty squares
                fileIndex += c.digitToInt()
            } else {
                // Place piece
                board[rankIndex][fileIndex] = c
                fileIndex++
            }
        }
    }

    return board
}


@Composable
fun SimplePieceDrawing(piece: Char, color: Color) {
    Text(
        text = when (piece) {
            'P', 'p' -> "♟"
            'R', 'r' -> "♜"
            'N', 'n' -> "♞"
            'B', 'b' -> "♝"
            'Q', 'q' -> "♛"
            'K', 'k' -> "♚"
            else -> ""
        },
        fontSize = 24.sp,
        color = color,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxSize()
    )
}