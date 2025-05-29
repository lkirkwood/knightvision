package com.knightvision.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.foundation.layout.*
import androidx.compose.ui.unit.dp
import androidx.compose.material3.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.activity.ComponentActivity
import android.widget.Toast

class SettingsViewModel : ViewModel() {
    var serverAddress by mutableStateOf("")
    var stockfishDepth by mutableStateOf(10)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onBackClick: () -> Unit) {
    val context = LocalContext.current
    val viewModel: SettingsViewModel = viewModel(context as ComponentActivity)

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("Settings") },
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

        val keyboardController = LocalSoftwareKeyboardController.current

        TextField(
            value = viewModel.serverAddress,
            onValueChange = { viewModel.serverAddress = it },
            label = { Text("Address of KnightVision server") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    keyboardController?.hide()
                }
            ),
            singleLine = true
        )

        Spacer(modifier = Modifier.width(10.dp))

        var stockfishDepth by rememberSaveable { mutableStateOf(viewModel.stockfishDepth.toString()) }
        TextField(
            value = stockfishDepth,
            onValueChange = { newDepth: String ->
                stockfishDepth = newDepth
            },
            label = { Text("Analysis search depth") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    if (!stockfishDepth.all { it.isDigit() }) {
                        Toast.makeText(
                            context, "Search depth must be a number.", Toast.LENGTH_SHORT
                        ).show()
                        stockfishDepth = viewModel.stockfishDepth.toString()
                    } else {
                        viewModel.stockfishDepth = stockfishDepth.toInt()
                        keyboardController?.hide()
                    }
                }
            ),
            singleLine = true
        )
    }
}
