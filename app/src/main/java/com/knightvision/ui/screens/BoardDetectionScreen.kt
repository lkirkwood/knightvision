package com.knightvision.ui.screens

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
import android.graphics.Bitmap
import android.util.Log
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import java.io.ByteArrayOutputStream
import java.io.IOException
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.platform.LocalContext
import androidx.activity.ComponentActivity

import com.knightvision.StockfishBridge
import com.knightvision.ui.screens.SettingsViewModel
import com.knightvision.ui.screens.BoardImageViewModel

const val STARTING_FEN = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR"

suspend fun analyseImage(client: OkHttpClient, serverAddress: String, image: Bitmap): String = withContext(Dispatchers.IO) {
    val imageBytes = ByteArrayOutputStream()
    image.compress(Bitmap.CompressFormat.JPEG, 100, imageBytes)
    val request = Request.Builder()
        .url("http://" + serverAddress + "/parse-board")
        .post(imageBytes.toByteArray().toRequestBody("image/png".toMediaTypeOrNull()))
        .build()

    val responseBody = client.newCall(request).execute().use { response ->
        if (!response.isSuccessful) {
            Log.e("com.knightvision", "Request to extract position from board failed: ${response.body}")
            throw IOException("Request to extract position from board failed: ${response.body}")
        }

        if (response.body == null) {
            Log.e("com.knightvision", "Response from board analyser was empty.")
            throw IllegalArgumentException("Response from board analyser was empty.")
        }

        response.body!!.string()
    }
    responseBody
}

suspend fun searchPosition(boardFen: String, depth: Int = 20) = withContext(Dispatchers.Default) {
    StockfishBridge.runCmd("position " + boardFen)
    StockfishBridge.runCmd("go depth " + depth)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BoardDetectionScreen(
    onBackClick: () -> Unit = {},
    onAnalyseClick: (String) -> Unit,
    onEditBoardClick: () -> Unit = {}
) {
    val settings: SettingsViewModel = viewModel(LocalContext.current as ComponentActivity)
    val boardImageModel: BoardImageViewModel = viewModel(LocalContext.current as ComponentActivity)
    var boardFen by remember { mutableStateOf<String>(STARTING_FEN) }
    var stockfishReady by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            StockfishBridge.initEngine()
            StockfishBridge.runCmd("uci")
            stockfishReady = true
        }
    }

    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(boardImageModel.boardImage) {
        if (boardImageModel.boardImage != null) {
            try {
                boardFen = analyseImage(OkHttpClient(), settings.serverAddress, boardImageModel.boardImage!!)
            } catch (exc : Exception) {
                snackbarHostState.showSnackbar("Failed to extract board position from image.")
                Log.e(
                    "com.knightvision",
                    "Request to extract position from board threw an error: ",
                    exc
                )
            }
        }
    }

    var boardState = remember(boardFen) { parseFenToBoard(boardFen) }
    LaunchedEffect(boardFen, stockfishReady) {
        boardState = parseFenToBoard(boardFen)
        if (stockfishReady) {
            searchPosition(boardFen)
        }
    }

    // State for board information
    var detectedOpening by remember { mutableStateOf("Starting Position") }
    var piecesDetected by remember { mutableStateOf("32/32") }
    var evaluation by remember { mutableStateOf("") }
    var advantage by remember { mutableStateOf("Equal") }


    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) {
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
                                text = "Detected Opening:",
                                fontSize = 14.sp,
                                color = Color.DarkGray
                            )
                            Text(
                                text = detectedOpening,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Pieces detected:",
                                fontSize = 14.sp,
                                color = Color.DarkGray
                            )
                            Text(
                                text = piecesDetected,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Position evaluation:",
                                fontSize = 14.sp,
                                color = Color.DarkGray
                            )
                            Text(
                                text = "$evaluation $advantage",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                // Action Buttons
                Button(
                    onClick = { -> onAnalyseClick(boardFen) },
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
    val board = Array(8) { Array(8) { '.' } } // Empty board with '.' representing empty squares
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

// For preview/testing purposes - Add a simple drawing of pieces
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
