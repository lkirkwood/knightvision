package com.knightvision.ui.screens


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import androidx.activity.ComponentActivity
import androidx.lifecycle.viewmodel.compose.viewModel
import android.util.Log

import com.knightvision.ui.screens.BoardStateViewModel
import com.knightvision.ui.screens.BoardEvaluationViewModel

class Evaluation(val bestMove: String, val ponder: String, val score: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalysisScreen(onBackClick: () -> Unit = {}) {
    val boardState = viewModel<BoardStateViewModel>(LocalContext.current as ComponentActivity).boardState
    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabs = listOf("Moves", "Openings")
    var boardEval = viewModel<BoardEvaluationViewModel>(LocalContext.current as ComponentActivity).boardEval
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) {

        TopAppBar(
            title = { Text("Analysis") },
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

        // Tabs (unchanged)
        TabRow(
            selectedTabIndex = selectedTabIndex,
            containerColor = Color.White,
            contentColor = Color(0xFF4D4B6E),
            indicator = { tabPositions ->
                TabRowDefaults.Indicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                    height = 3.dp,
                    color = Color(0xFF4D4B6E)
                )
            }
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    text = {
                        Text(
                            text = title,
                            fontSize = 16.sp,
                            fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    selectedContentColor = Color(0xFF4D4B6E),
                    unselectedContentColor = Color.Gray
                )
            }
        }

        // Tab content
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp))
        {
            when (selectedTabIndex) {
                0 -> EvaluationTab(boardEval)
                1 -> OpeningContent(boardState.openingName, boardState.openingMoves)
            }
        }
    }
}

@Composable
fun EvaluationTab(evaluation: Evaluation) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 8.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val score = if (evaluation.score.startsWith("-")) evaluation.score else "+${evaluation.score}"
        AnalysisItemCard(
            "Best move: ${evaluation.bestMove}",
            "Most likely response: ${evaluation.ponder}",
            "Centipawn score: ${score}"
        )
    }
}

fun joinOpeningMoves(moves: List<List<String>>): String {
    return "Moves: " + moves.mapIndexed { idx, move -> "${idx + 1}. $move" }.joinToString(" ")
}

@Composable
fun OpeningContent(openingName: String?, openingMoves: List<List<String>>?) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 8.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AnalysisItemCard(
            openingName ?.let { "Opening: $it" } ?: "Unable to detect opening.",
            openingMoves ?.let { joinOpeningMoves(it) }  ?: ""
        )
    }
}


@Composable
fun AnalysisItemCard(
    mainText: String,
    secondaryText: String,
    tertiaryText: String? = null,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = mainText, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = secondaryText, fontSize = 14.sp, color = Color.DarkGray)
            if (tertiaryText != null) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(text = tertiaryText, fontSize = 14.sp, color = Color.Gray)
            }
        }
    }
}
