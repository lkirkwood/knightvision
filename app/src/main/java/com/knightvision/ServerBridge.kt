package com.knightvision

import okhttp3.OkHttpClient
import okhttp3.OkHttpClient.Builder
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonPrimitive
import android.graphics.Bitmap
import android.util.Log
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.util.concurrent.TimeUnit

public class BoardState(
    val boardFen: String, val openingName: String? = null, val openingMoves: List<List<String>>? = null)

suspend fun analyseImage(
    serverAddress: String, image: Bitmap, orientation: String
): BoardState = withContext(Dispatchers.IO) {
    val client = OkHttpClient.Builder()
        .readTimeout(90, TimeUnit.SECONDS)
        .build()

    val imageBytes = ByteArrayOutputStream()
    image.compress(Bitmap.CompressFormat.JPEG, 100, imageBytes)

    val request = Request.Builder()
        .url("http://" + serverAddress + "/parse-board?orientation=" + orientation.lowercase())
        .post(imageBytes.toByteArray().toRequestBody("image/png".toMediaTypeOrNull()))
        .build()

    val analysis = client.newCall(request).execute().use { response ->
        if (!response.isSuccessful) {
            Log.e("com.knightvision", "Request to extract position from board failed: ${response.body}")
            throw IOException("Request to extract position from board failed: ${response.body}")
        }

        if (response.body == null) {
            Log.e("com.knightvision", "Response from board analyser was empty.")
            throw IllegalArgumentException("Response from board analyser was empty.")
        }
        val jsonResp = Json.parseToJsonElement(response.body!!.string())
        val boardFen = jsonResp.jsonObject.get("fen")!!.jsonPrimitive.content
        val openingName: String?
        val openingMoves: List<List<String>>?
         if (jsonResp.jsonObject.containsKey("opening")) {
            val opening = jsonResp.jsonObject.get("opening")!!
            openingName = opening.jsonObject.get("name").toString()
            openingMoves = opening.jsonObject.get("moves")?.jsonArray?.map { it.jsonArray.map { it.jsonPrimitive.content } }
        } else {
            openingName = null
            openingMoves = null
        }

        BoardState(boardFen, openingName, openingMoves)
    }
    analysis
}
