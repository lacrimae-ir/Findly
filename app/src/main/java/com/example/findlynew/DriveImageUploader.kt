package com.example.findlynew

import android.content.Context
import android.net.Uri
import android.util.Base64
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.InputStream
import java.util.concurrent.TimeUnit

object DriveImageUploader {

    // Ganti URL ini dengan URL Web App Apps Script setelah Anda mendeploy script tersebut
    var SCRIPT_URL = "https://script.google.com/macros/s/AKfycbzgsg_-taVcxZUjGRTGroPKPIXeO5SpiCYc_AdYSmxVjcx3hgyHDaUCULMcpMrCjbeWxw/exec"
    fun uploadImage(context: Context, imageUri: Uri, type: String, callback: (String?) -> Unit) {
        Thread {
            try {
                val inputStream: InputStream? = context.contentResolver.openInputStream(imageUri)
                val bytes = inputStream?.readBytes()
                inputStream?.close()

                if (bytes == null) {
                    callback(null)
                    return@Thread
                }

                val base64Image = Base64.encodeToString(bytes, Base64.NO_WRAP)
                val fileName = "img_${System.currentTimeMillis()}.jpg"

                val json = JSONObject().apply {
                    put("image", base64Image)
                    put("name", fileName)
                    put("type", type)
                }

                val client = OkHttpClient.Builder()
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .writeTimeout(30, TimeUnit.SECONDS)
                    .build()

                val requestBody = json.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
                val request = Request.Builder()
                    .url(SCRIPT_URL)
                    .post(requestBody)
                    .build()

                client.newCall(request).execute().use { response ->
                    android.util.Log.d("FindlyUpload", "Response code: ${response.code}")
                    val bodyString = response.body?.string()
                    if (bodyString != null) {
                        logLargeString("FindlyUpload", "Response body: $bodyString")
                    }

                    if (response.isSuccessful && bodyString != null) {
                        try {
                            val resJson = JSONObject(bodyString)
                            if (resJson.optString("status") == "success") {
                                callback(resJson.getString("url"))
                                return@Thread
                            } else {
                                android.util.Log.e("FindlyUpload", "Script error: ${resJson.optString("message")}")
                            }
                        } catch (jsonEx: Exception) {
                            android.util.Log.e("FindlyUpload", "JSON Parse error: ${jsonEx.message}")
                        }
                    }
                    callback(null)
                }
            } catch (e: Exception) {
                android.util.Log.e("FindlyUpload", "Exception during upload: ${e.message}", e)
                callback(null)
            }
        }.start()
    }

    private fun logLargeString(tag: String, content: String) {
        val maxLogSize = 2000
        for (i in 0..content.length / maxLogSize) {
            val start = i * maxLogSize
            var end = (i + 1) * maxLogSize
            end = if (end > content.length) content.length else end
            android.util.Log.d(tag, content.substring(start, end))
        }
    }
}
