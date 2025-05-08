package com.knightvision.ui.screens

import androidx.camera.core.ImageProxy
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.material.MaterialTheme
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import android.graphics.Bitmap
import android.util.Log
import android.widget.Toast
import java.io.ByteArrayOutputStream
import java.io.IOException

import com.knightvision.StockfishBridge
import com.knightvision.ui.screens.analyseImage

fun analyseImage(client: OkHttpClient, image: Bitmap): String {
    try {
        val imageBytes = ByteArrayOutputStream()
        image.compress(Bitmap.CompressFormat.JPEG, 100, imageBytes)
        val request = Request.Builder()
            .url( "http://10.0.2.2:8080/parse-board") // TODO make this configurable or something
            .post(imageBytes.toByteArray().toRequestBody("image/png".toMediaTypeOrNull()))
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                Log.e("com.knightvision", "Request to extract position from board failed: ${response.body}")
                throw IOException("Request to extract position from board failed: ${response.body}")
            }

            if (response.body == null) {
                Log.e("com.knightvision", "Response from board analyser was empty.")
                throw IllegalArgumentException("Response from board analyser was empty.")
            }

            return response.body!!.string()
        }
    } catch (exc : Exception) {
        Log.e("com.knightvision", "Request to extract position from board threw an error: ", exc)
        throw exc
    }
}

const val STARTING_FEN = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1"

@Composable
fun BoardDetectionScreen(image: Bitmap?) {
    if (image == null) {
        throw IllegalStateException("BoardDetectionScreen entered with null image value.")
    }

    Image(
        bitmap = image.asImageBitmap(),
        contentDescription = null,
        contentScale = ContentScale.Fit,
        alignment = Alignment.Center
    )

    val httpClient = remember { OkHttpClient() }
    var bestMove = remember { mutableStateOf<String?>(null) }
    var boardFen = remember { mutableStateOf<String>(STARTING_FEN) }
    var analysisError = remember { mutableStateOf<Boolean>(false) }
    LaunchedEffect(Unit) {
        Thread {
            try {
                boardFen.value = analyseImage(httpClient, image)
            } catch (exc : Exception) {
                Log.e("com.knightvision", "Exception during board image analysis", exc)
                analysisError.value = true
            }

            StockfishBridge.initEngine()
            var output = ""
            output += StockfishBridge.runCmd("uci")
            output += StockfishBridge.runCmd("position " + boardFen)
            output += StockfishBridge.runCmd("go")
            Log.d("com.knightvision - libstockfish", output)
            Thread.sleep(3000) // TODO definitely make this a slider or add a stop button
            bestMove.value = StockfishBridge.bestmove()
        }.start()
    }


    Column {
        Text(boardFen.value)

        if (analysisError.value) {
            Toast.makeText(
                LocalContext.current,
                "Failed to extract board position from image.",
                Toast.LENGTH_SHORT
            ).show()
        }

        if (bestMove.value != null) {
            Text(bestMove.value!!)
        } else {
            Text("Calculating best move...")
        }
    }
}
