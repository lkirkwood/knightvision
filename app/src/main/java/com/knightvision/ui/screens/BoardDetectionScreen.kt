package com.knightvision.ui.screens

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import androidx.compose.material.icons.filled.ContentCopy

import com.knightvision.StockfishBridge
import com.knightvision.BoardEvaluationViewModel
import com.knightvision.ui.screens.SettingsViewModel
import com.knightvision.ui.screens.BoardImageViewModel
import com.knightvision.BoardState
import com.knightvision.analyseImage

fun copyToClipboard(context: Context, text: String) {
    val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clipData = ClipData.newPlainText("FEN String", text)
    clipboardManager.setPrimaryClip(clipData)
    Toast.makeText(context, "FEN copied to clipboard", Toast.LENGTH_SHORT).show()
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BoardDetectionScreen(
    onBackClick: () -> Unit,
    onAnalyseClick: () -> Unit,
    onEditBoardClick: () -> Unit
) {
    val settings: SettingsViewModel = viewModel(LocalContext.current as ComponentActivity)
    val boardImageModel: BoardImageViewModel = viewModel(LocalContext.current as ComponentActivity)
    var boardEvalModel: BoardEvaluationViewModel = viewModel(LocalContext.current as ComponentActivity)

    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            StockfishBridge.initEngine()
            StockfishBridge.runCmd("uci")
            boardEvalModel.setReady(true)
        }
    }

    var detectionComplete by rememberSaveable { mutableStateOf(false)}
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(boardImageModel.boardImage) {
        if (boardImageModel.boardImage != null && !detectionComplete) {
            try {
                boardEvalModel.boardState = analyseImage(
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

    var boardArray by rememberSaveable { mutableStateOf(parseFenToBoard(boardEvalModel.boardState.boardFen)) }
    var lastFen by rememberSaveable { mutableStateOf(boardEvalModel.boardState.boardFen) }
    LaunchedEffect(boardEvalModel.boardState.boardFen) {
        if (boardEvalModel.boardState.boardFen != lastFen) {
            lastFen = boardEvalModel.boardState.boardFen
            boardArray = parseFenToBoard(boardEvalModel.boardState.boardFen)
            boardEvalModel.setComplete(false)
        }
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF5F5F5))
        ) {
            // Top App Bar
            BoardDetectionTopAppBar(onBackClick = onBackClick)

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Chess Board
                BoardDisplay(boardArray)

                Spacer(modifier = Modifier.height(8.dp))

                // Position Information Card
                FenStringCard(fenString = boardEvalModel.boardState.boardFen)

                Spacer(modifier = Modifier.height(8.dp))

                // Castling and Turn Control Card
                CastlingAndTurnControlCard(
                    parseFenFlags(boardEvalModel.boardState.boardFen),
                    { flags ->
                        boardEvalModel.boardState.boardFen = updateFenFlags(
                            boardEvalModel.boardState.boardFen, flags)
                        boardEvalModel.setComplete(false)
                    }
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Action Buttons
                ActionButtons(
                    onAnalyseClick = onAnalyseClick,
                    onEditBoardClick = onEditBoardClick
                )
            }
        }
    }

    if (!detectionComplete) {
        LoadingOverlay("Detecting board position")
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BoardDetectionTopAppBar(
    onBackClick: () -> Unit
) {
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
}

@Composable
private fun BoardDisplay(
    boardArray: Array<Array<Char>>
) {
    ChessBoard(
        boardArray = boardArray,
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clip(RoundedCornerShape(4.dp))
            .border(1.dp, Color.LightGray, RoundedCornerShape(4.dp))
    )
}

@Composable
private fun FenStringCard(
    fenString: String
) {
    val context = LocalContext.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        shape = RoundedCornerShape(4.dp),
        elevation = CardDefaults.cardElevation(3.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
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
                    onClick = { copyToClipboard(context, fenString) },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copy FEN",
                        tint = Color(0xFF4D4B6E)
                    )
                }
            }
        }
    }
}

@Composable
fun CastlingAndTurnControlCard(flags: FenFlags, onChanged: (FenFlags) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        shape = RoundedCornerShape(4.dp),
        elevation = CardDefaults.cardElevation(3.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp)
        ) {
            // Castling Section
            CastlingSection(flags, onChanged)

            Spacer(modifier = Modifier.height(4.dp))

            // Turn Toggle Section
            ActivePlayerSection(flags, onChanged)
        }
    }
}

@Composable
fun CastlingSection(flags: FenFlags, onChanged: (FenFlags) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // White Section castling
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = "White Castling",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color.DarkGray
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CastlingCheckbox(
                    label = "0-0",
                    checked = flags.whiteCastleKing,
                    onCheckedChange = { checked ->
                        flags.whiteCastleKing = checked
                        onChanged(flags)
                    }
                )

                CastlingCheckbox(
                    label = "0-0-0",
                    checked = flags.whiteCastleQueen,
                    onCheckedChange = { checked ->
                        flags.whiteCastleQueen = checked
                        onChanged(flags)
                    }
                )
            }
        }

        // Black Section castling
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = "Black Castling",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color.DarkGray
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CastlingCheckbox(
                    label = "0-0",
                    checked = flags.blackCastleKing,
                    onCheckedChange = { checked ->
                        flags.blackCastleKing = checked
                        onChanged(flags)
                    }
                )

                CastlingCheckbox(
                    label = "0-0-0",
                    checked = flags.blackCastleQueen,
                    onCheckedChange = { checked ->
                        flags.blackCastleQueen = checked
                        onChanged(flags)
                    }
                )
            }
        }
    }
}

@Composable
fun CastlingCheckbox(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = Color.DarkGray,
            modifier = Modifier.padding(bottom = 3.dp)
        )
        var isChecked by remember { mutableStateOf(checked) }
        Checkbox(
            checked = isChecked,
            onCheckedChange = {
                isChecked = it
                onCheckedChange(it)
            },
            colors = CheckboxDefaults.colors(
                checkedColor = Color(0xFF4D4B6E)
            ),
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
fun ActivePlayerSection(flags: FenFlags, onChanged: (FenFlags) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Active Player",
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = Color.DarkGray
        )

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (flags.whiteActive) "White" else "Black",
                fontSize = 14.sp,
                color = Color.DarkGray,
                modifier = Modifier.padding(end = 4.dp)
            )

            var whiteActive by remember { mutableStateOf(flags.whiteActive) }
            Switch(
                checked = whiteActive,
                onCheckedChange = {
                    whiteActive = it
                    flags.whiteActive = it
                    onChanged(flags)
                },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = Color(0xFF4D4B6E),
                    uncheckedThumbColor = Color.White,
                    uncheckedTrackColor = Color.Gray
                )
            )
        }
    }
}

@Composable
private fun ActionButtons(
    onAnalyseClick: () -> Unit,
    onEditBoardClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Button(
            onClick = onAnalyseClick,
            modifier = Modifier
                .weight(1f)
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
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = "Analysis",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White
            )
        }

        OutlinedButton(
            onClick = onEditBoardClick,
            modifier = Modifier
                .weight(1f)
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
        contentAlignment = Alignment.Center
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

data class FenFlags(
    var whiteActive: Boolean,

    var whiteCastleKing: Boolean,
    var whiteCastleQueen: Boolean,
    var blackCastleKing: Boolean,
    var blackCastleQueen: Boolean
)

fun parseFenFlags(fen: String): FenFlags {
    val parts = fen.split(" ")
    val active = parts[1]
    val castling = parts[2]
    return FenFlags(
        active == "w",
        castling.contains('K'),
        castling.contains('Q'),
        castling.contains('k'),
        castling.contains('q'),
    )
}

fun updateFenFlags(fen: String, flags: FenFlags): String {
    var parts = fen.split(" ").toTypedArray()
    parts[1] = if (flags.whiteActive) "w" else "b"

    var castling = StringBuilder()
    if (flags.whiteCastleKing) {
        castling.append('K')
    }
    if (flags.whiteCastleQueen) {
        castling.append('Q')
    }
    if (flags.blackCastleKing) {
        castling.append('k')
    }
    if (flags.blackCastleQueen) {
        castling.append('q')
    }
    parts[2] = castling.toString()

    return parts.joinToString(" ")
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
