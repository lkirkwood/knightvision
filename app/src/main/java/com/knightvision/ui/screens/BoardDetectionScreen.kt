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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
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
import java.io.ByteArrayOutputStream
import java.io.IOException
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.ViewModel
import androidx.compose.ui.platform.LocalContext
import androidx.activity.ComponentActivity
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material.icons.filled.ContentCopy
import kotlinx.coroutines.delay

import com.knightvision.StockfishBridge
import com.knightvision.ui.screens.SettingsViewModel
import com.knightvision.ui.screens.BoardImageViewModel
import com.knightvision.BoardState
import com.knightvision.analyseImage

const val STARTING_FEN = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR"

suspend fun searchPosition(boardFen: String, depth: Int = 20): String = withContext(Dispatchers.Default) {
    StockfishBridge.runCmd("position " + boardFen)
    StockfishBridge.goBlocking(depth)
}

fun copyToClipboard(context: Context, text: String) {
    val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clipData = ClipData.newPlainText("FEN String", text)
    clipboardManager.setPrimaryClip(clipData)
    Toast.makeText(context, "FEN copied to clipboard", Toast.LENGTH_SHORT).show()
}

public class BoardStateViewModel : ViewModel() {
    var boardState by mutableStateOf<BoardState>(BoardState(STARTING_FEN))
}

public class BoardEvaluationViewModel : ViewModel() {
    var boardEval by mutableStateOf<Evaluation>(Evaluation("e2e4", "e7e5", "+0.99"))
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BoardDetectionScreen(
    onBackClick: () -> Unit = {},
    onAnalyseClick: () -> Unit,
    onEditBoardClick: () -> Unit = {}
) {
    val settings: SettingsViewModel = viewModel(LocalContext.current as ComponentActivity)
    val boardImageModel: BoardImageViewModel = viewModel(LocalContext.current as ComponentActivity)
    var boardStateModel: BoardStateViewModel = viewModel(LocalContext.current as ComponentActivity)
    var boardEvalModel: BoardEvaluationViewModel = viewModel(LocalContext.current as ComponentActivity)
    var stockfishReady by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            StockfishBridge.initEngine()
            StockfishBridge.runCmd("uci")
            stockfishReady = true
        }
    }

    var detectionComplete by rememberSaveable { mutableStateOf(false)}
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(boardImageModel.boardImage) {
        if (boardImageModel.boardImage != null && !detectionComplete) {
            try {
                boardStateModel.boardState = analyseImage(
                    settings.serverAddress,
                    boardImageModel.boardImage!!,
                    boardImageModel.orientation
                )
                detectionComplete = true
            } catch (exc : Exception) {
                detectionComplete = true
                snackbarHostState.showSnackbar("Failed to extract board position from image.")
                Log.e(
                    "com.knightvision",
                    "Request to extract position from board threw an error: ",
                    exc
                )
            }
        }
    }

    var analysisComplete by rememberSaveable { mutableStateOf(false) }
    var boardArray by rememberSaveable { mutableStateOf(parseFenToBoard(boardStateModel.boardState.boardFen)) }
    LaunchedEffect(boardStateModel.boardState, stockfishReady) {
        boardArray = parseFenToBoard(boardStateModel.boardState.boardFen)
        if (stockfishReady) {
            val outputLines = searchPosition(boardStateModel.boardState.boardFen, 10).split("\n").dropLast(1)
            val bestmoveParts = outputLines.last().split(" ")

            val lastInfo = outputLines.dropLast(2).last()
            var seenCpTag = false
            val cpEval = run breakval@ {
                for (part in lastInfo.split(" ")) {
                    if (seenCpTag) {
                        return@breakval part
                    } else if (part == "cp") {
                        seenCpTag = true
                    }
                }
                Log.e("com.knightvision", "Couldnt find centipawn score in last info: " + lastInfo)
                null
            }

            if (bestmoveParts.size < 4) {
                Log.e("com.knightvision", "Unexpected bestmove output: ${bestmoveParts.joinToString(" ")}")
            } else {
                Log.e(
                    "com.knightvision",
                    "Successfully set evaluation; bestmove: ${bestmoveParts[1]}, ponder: ${bestmoveParts[3]}, eval: $cpEval"
                )
                boardEvalModel.boardEval = Evaluation(bestmoveParts[1], bestmoveParts[3], cpEval)
            }

            analysisComplete = true
        }
    }


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
                    boardArray = boardArray,
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
                            text = boardStateModel.boardState.boardFen,
                            fontSize = 14.sp,
                            color = Color.DarkGray,
                            maxLines = 1,
                            modifier = Modifier.weight(1f),
                        )
                        val clipboardContext = LocalContext.current
                        IconButton(
                            onClick = { copyToClipboard(clipboardContext, boardStateModel.boardState.boardFen) },
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

            Spacer(modifier = Modifier.height(16.dp))

                // Action Buttons
                Button(
                    onClick = { -> if (detectionComplete) onAnalyseClick() },
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

    if (!detectionComplete) {
        LoadingOverlay("Detecting board position")
    } else if (!analysisComplete) {
        LoadingOverlay("Analysing game state")
    }
}

@Composable
fun LoadingOverlay(message: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.7f)),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .padding(32.dp)
                .fillMaxWidth(0.8f),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            ),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(64.dp),
                    color = Color(0xFF4D4B6E),
                    strokeWidth = 6.dp
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = message,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF4D4B6E),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Please wait as your position is being analysed...",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )
            }
        }
    }
}

suspend fun serverAnalysis(onComplete: (String) -> Unit) {

    val detectedFen = "rnbqkbnr/pppppppp/8/8/4P3/8/PPPP1PPP/RNBQKBNR"
    onComplete(detectedFen)
}

@Composable
fun ChessBoard(
    boardArray: Array<Array<Char>>,
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
                        piece = boardArray[row][col],
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
