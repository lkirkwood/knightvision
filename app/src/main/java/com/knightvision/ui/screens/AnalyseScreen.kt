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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalysisScreen(
    onBackClick: () -> Unit = {},
    fenString: String = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR" // Default starting position
) {

    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabs = listOf("Moves", "Openings")

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
                0 -> MovesContent()
                1 -> OpeningContent()
            }
        }
    }
}

@Composable
fun MovesContent() {
    var moveSuggestions by remember { mutableStateOf<List<Triple<String, String, String>>>(emptyList()) }

    // TODO: Replace with actual data fetch logic
    LaunchedEffect(Unit) {
        moveSuggestions = listOf(
            Triple("e4", "+0.34", "e5") // add api call here
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 8.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (moveSuggestions.isEmpty()) {
            Text(
                text = "Loading suggestions...",
                fontSize = 14.sp,
                color = Color.Gray,
                modifier = Modifier.padding(top = 16.dp)
            )
        } else {
            moveSuggestions.forEach { (move, eval, pondering) ->
                AnalysisItemCard(move, eval, pondering)
            }
        }
    }
}

@Composable
fun OpeningContent() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 8.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val openingLines = listOf(
            Triple("Ruy Lopez", "e4 e5 Nf3 Nc6 Bb5", "+0.25") // replace with api call too

        )

        openingLines.forEach { (name, moves, eval) ->
            AnalysisItemCard(name, eval, "Line: $moves")
        }
    }
}


@Composable
fun AnalysisItemCard(move: String, evaluation: String, pondering: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "Move: $move", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "Evaluation: $evaluation", fontSize = 14.sp, color = Color.DarkGray)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = "Pondering: $pondering", fontSize = 14.sp, color = Color.Gray)
        }
    }
}
