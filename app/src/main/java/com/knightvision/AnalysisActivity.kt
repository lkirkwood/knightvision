package com.knightvision

import java.io.File
import java.io.IOException
import java.io.OutputStreamWriter
import java.io.InputStreamReader
import java.io.BufferedWriter
import java.io.BufferedReader
import java.lang.ProcessBuilder
import android.os.Bundle
import android.os.Environment
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.Composable
import androidx.compose.material.Text
import androidx.compose.foundation.Image
import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.FilterQuality
import androidx.activity.compose.setContent
import android.util.Log
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import com.knightvision.StockfishBridge


@Composable
fun Analysis(boardFen: String, bestMove: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Board state FEN: " + boardFen)
        Text("Best next move: " + bestMove)
    }
}

@Composable
fun Waiting() {
    Text("Waiting!")
}

class AnalysisActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        var imageBytes = intent.getByteArrayExtra("image_bytes")
        if (imageBytes == null) {
            Toast.makeText(this, "Image to analyse is null", Toast.LENGTH_SHORT)
            Log.e("com.knightvision", "Image to analyse is null")
            throw IllegalArgumentException("Image to analyse is null")
        }

        setContent {
            Waiting()
        }

        val client = OkHttpClient()
        val request = Request.Builder()
            .url( "http://10.0.2.2:8080/parse-board")
            .post(imageBytes.toRequestBody("image/png".toMediaTypeOrNull()))
            .build()

        Thread {
            var boardFen: String
            try {
                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) {
                        Log.e("com.knightvision", "Request to extract position from board failed: ${response.body}")
                        throw IOException("Request to extract position from board failed: ${response.body}")
                    }

                    if (response.body == null) {
                        Log.e("com.knightvision", "Response from board analyser was empty.")
                        throw IllegalArgumentException("Response from board analyser was empty.")
                    }

                    boardFen = response.body!!.string()
                }
            } catch (exc : Exception) {
                Log.e("com.knightvision", "Request to extract position from board threw an error: ", exc)
                throw exc
            }

            StockfishBridge.initEngine()
            var output = ""
            output += StockfishBridge.runCmd("uci")
            output += StockfishBridge.runCmd("position " + boardFen)
            output += StockfishBridge.runCmd("go")
            Thread.sleep(3000)
            val bestmove = StockfishBridge.bestmove()
            Log.e("stockfish", bestmove)
            setContent {
                Analysis(boardFen, bestmove)
            }
        }.start()
    }
}
