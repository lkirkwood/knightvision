package com.knightvision

import java.io.File
import java.io.IOException
import android.os.Bundle
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


@Composable
fun Analysis(boardFen: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Board state FEN: " + boardFen)
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
            var boardFen: String?

            try {
                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) {
                        Log.e("com.knightvision", "Request to extract position from board failed: ${response.body}")
                        throw IOException("Request to extract position from board failed: ${response.body}")
                    }

                    boardFen = response.body?.string()
                }
            } catch (exc : Exception) {
                Log.e("com.knightvision", "Request to extract position from board threw an error: ", exc)
                throw exc
            }

            if (boardFen == null) {
                Log.e("com.knightvision", "Extracted board position was null.")
                throw IllegalArgumentException("Extracted board position was null.")
            }

            setContent {
                Analysis(boardFen!!)
            }
        }.start()

    }
}
