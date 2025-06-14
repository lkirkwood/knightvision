package com.knightvision.ui.screens

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.draw.clip
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Save
import androidx.activity.ComponentActivity
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.ViewModel
import android.widget.Toast
import androidx.compose.material.icons.automirrored.filled.ArrowBack

import com.knightvision.ui.screens.CastlingAndTurnControlCard
import com.knightvision.ui.screens.FenFlags
import com.knightvision.ui.screens.parseFenFlags
import com.knightvision.ui.screens.updateFenFlags
import com.knightvision.BoardState
import com.knightvision.StockfishBridge
import com.knightvision.BoardEvaluationViewModel

class BoardEditViewModel : ViewModel() {
    var currentPiece by mutableStateOf('.')
    var boardArray by mutableStateOf(Array(8, { Array(8, { '.' }) }))
}

fun parseBoardToFen(boardArray: Array<Array<Char>>): String {
    val fenBuilder = StringBuilder()
    for (row in boardArray) {
        var emptyCount = 0
        for (square in row) {
            when {
                square == '.' -> emptyCount++
                else -> {
                    if (emptyCount > 0) {
                        fenBuilder.append(emptyCount)
                        emptyCount = 0
                    }
                    fenBuilder.append(square)
                }
            }
        }
        if (emptyCount > 0) {
            fenBuilder.append(emptyCount)
        }
        fenBuilder.append('/')
    }
    fenBuilder.setLength(fenBuilder.length - 1)
    return fenBuilder.toString() + " w KQkq - 0 1" // TODO add who-to-move and castling
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BoardEditingTopAppBar(
    onBackClick: () -> Unit,
    onSaveClick: () -> Unit
) {
    val context = LocalContext.current
    val boardEditModel: BoardEditViewModel = viewModel(context as ComponentActivity)
    val boardEvalModel: BoardEvaluationViewModel = viewModel(context as ComponentActivity)
    boardEditModel.boardArray = parseFenToBoard(boardEvalModel.boardState.boardFen)
    val onSaveClick = {
        val boardFen = parseBoardToFen(boardEditModel.boardArray)
        if (StockfishBridge.validFen(boardFen)) {
            boardEvalModel.boardState = BoardState(boardFen)
            boardEvalModel.analysisComplete = false
            onSaveClick()
        } else {
            Toast.makeText(context, "Invalid board state!", Toast.LENGTH_SHORT).show()
        }
    }
    androidx.compose.material3.TopAppBar(
        title = {
            Text(
                text = "Board Editing",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        },
        actions = {
            IconButton(
                onClick = onSaveClick
            ) {
                Icon(
                    imageVector = Icons.Default.Save,
                    contentDescription = "Save edits",
                    tint = Color.White
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
        },
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
fun BoardEditingScreen(onSaveClick: () -> Unit, onBackClick: () -> Unit) {
    val context = LocalContext.current
    val boardEditModel: BoardEditViewModel = viewModel(context as ComponentActivity)
    val boardEvalModel: BoardEvaluationViewModel = viewModel(context as ComponentActivity)
    var flags by remember { mutableStateOf(parseFenFlags(boardEvalModel.boardState.boardFen)) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) {
        Column() {
            BoardEditingTopAppBar(
                onSaveClick = {
                    var boardFen = parseBoardToFen(boardEditModel.boardArray)
                    boardFen = updateFenFlags(boardFen, flags)
                    if (StockfishBridge.validFen(boardFen)) {
                        boardEvalModel.boardState = BoardState(boardFen)
                        boardEvalModel.setComplete(false)
                        onSaveClick()
                    } else {
                        Toast.makeText(context, "Invalid board state!", Toast.LENGTH_SHORT).show()
                    }
                },
                onBackClick = { onBackClick() }
            )
            EditableChessBoard(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .border(1.dp, Color.LightGray, RoundedCornerShape(8.dp))
            )
            Spacer(Modifier.height(16.dp))
            PieceSelectionArea()
            Spacer(Modifier.height(8.dp))
            CastlingAndTurnControlCard(
                parseFenFlags(boardEvalModel.boardState.boardFen),
                { newFlags ->
                    flags = newFlags
                    boardEvalModel.setComplete(false)
                }
            )
        }
    }
}

@Composable
fun PieceSelection(piece: Char, modifier: Modifier) {
    val editModel: BoardEditViewModel = viewModel(LocalContext.current as ComponentActivity)

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .border(1.dp, Color.LightGray)
            .clickable(
                onClick = {
                    editModel.currentPiece = piece
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        ChessPiece(piece = piece)
    }
}

@Composable
fun PieceSelectionArea() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        listOf('P', 'B', 'N', 'R', 'Q', 'K').mapIndexed {idx, piece ->
            PieceSelection(
                piece,
                Modifier.weight(1f).background(
                    if (idx % 2 == 0) Color(0xFFE8D0AA) else Color(0xFFB58863)
                )
            )
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        listOf('p', 'b', 'n', 'r', 'q', 'k').mapIndexed {idx, piece ->
            PieceSelection(
                piece,
                Modifier.weight(1f).background(
                    if (idx % 2 == 1) Color(0xFFE8D0AA) else Color(0xFFB58863)
                )
            )
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(8f)
    ) {
        PieceSelection(
            '.',
            Modifier
                .background(Color.Red)
                .align(Alignment.Center)
                .fillMaxHeight()
        )
    }
}

@Composable
fun EditableChessSquare(
    piece: Char,
    isLightSquare: Boolean,
    onPieceClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(if (isLightSquare) Color(0xFFE8D0AA) else Color(0xFFB58863))
            .clickable(onClick = onPieceClick),
        contentAlignment = Alignment.Center
    ) {
        if (piece != '.') {
            ChessPiece(piece = piece)
        }
    }
}

@Composable
fun EditableChessBoard(
    modifier: Modifier = Modifier
) {
    val editModel: BoardEditViewModel = viewModel(LocalContext.current as ComponentActivity)

    Column(modifier = modifier) {
        for (row in 0..7) {
            Row(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                for (col in 0..7) {
                    EditableChessSquare(
                        piece = editModel.boardArray[row][col],
                        isLightSquare = (row + col) % 2 == 0,
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f),
                        onPieceClick = {
                            val newArray = editModel.boardArray.map { it.copyOf() }.toTypedArray()
                            newArray[row][col] = editModel.currentPiece
                            editModel.boardArray = newArray
                        }
                    )
                }
            }
        }
    }
}
