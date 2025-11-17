package fr.eseo.ld.ts.cinelog.network

import android.content.Context
import android.net.Uri
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.asRequestBody
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream

object FreeImageHostUploader {

    private const val TAG = "FreeImageHostUploader"
    private const val API_KEY = "6d207e02198a847aa98d0a2a901485a5"
    private val client = OkHttpClient()

    suspend fun uploadImage(context: Context, imageUri: Uri): Result<String> = withContext(Dispatchers.IO) {
        Log.d(TAG, "Starting upload for URI: $imageUri")
        Log.d(TAG, "URI scheme: ${imageUri.scheme}, authority: ${imageUri.authority}")

        try {
            // Step 1: Convert Uri → File
            val file = uriToFile(context, imageUri)
                ?: return@withContext Result.failure(Exception("Failed to copy Uri to cache file"))

            Log.d(TAG, "Successfully copied image to cache: ${file.absolutePath}")
            Log.d(TAG, "File size: ${file.length()} bytes")

            // Step 2: Build multipart request
            val requestBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("key", API_KEY)
                .addFormDataPart("action", "upload")
                .addFormDataPart(
                    "source", "profile_${System.currentTimeMillis()}.jpg",
                    file.asRequestBody("image/*".toMediaType())
                )
                .addFormDataPart("format", "json")
                .build()

            Log.d(TAG, "Multipart request built (${requestBody.contentLength()} bytes)")

            // Step 3: Send request
            val request = Request.Builder()
                .url("https://freeimage.host/api/1/upload")
                .post(requestBody)
                .build()

            Log.d(TAG, "Sending POST request to freeimage.host...")

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""

            Log.d(TAG, "HTTP ${response.code} | Response length: ${responseBody.length} chars")

            if (!response.isSuccessful) {
                Log.e(TAG, "Upload failed with HTTP ${response.code}")
                Log.e(TAG, "Response body: $responseBody")
                return@withContext Result.failure(Exception("Upload failed: HTTP ${response.code}"))
            }

            // Step 4: Parse JSON
            Log.d(TAG, "Raw JSON response: $responseBody")

            val json = JSONObject(responseBody)

            if (json.has("error")) {
                val errorMsg = json.getJSONObject("error").optString("message", "Unknown error")
                Log.e(TAG, "API returned error: $errorMsg")
                return@withContext Result.failure(Exception(errorMsg))
            }

            if (!json.has("image")) {
                Log.e(TAG, "JSON missing 'image' object: $json")
                return@withContext Result.failure(Exception("Invalid API response format"))
            }

            val imageObj = json.getJSONObject("image")
            val url = imageObj.getString("url")

            Log.d(TAG, "Upload SUCCESS! Public URL: $url")
            Log.d(TAG, "Thumb URL: ${imageObj.optString("thumb", "").takeIf { it.isNotEmpty() }?.let { imageObj.getJSONObject("thumb").getString("url") }}")

            // Optional: clean up temp file
            file.delete()
            Log.d(TAG, "Temporary file deleted")

            return@withContext Result.success(url)

        } catch (e: Exception) {
            Log.e(TAG, "Upload failed with exception", e)
            return@withContext Result.failure(e)
        }
    }

    private fun uriToFile(context: Context, uri: Uri): File? {
        val file = File(context.cacheDir, "upload_${System.currentTimeMillis()}.jpg")
        return try {
            Log.d(TAG, "Copying Uri → File: $uri → ${file.absolutePath}")
            val inputStream = context.contentResolver.openInputStream(uri)
                ?: throw Exception("Cannot open InputStream for Uri")

            inputStream.use { input ->
                FileOutputStream(file).use { output ->
                    val bytesCopied = input.copyTo(output)
                    Log.d(TAG, "Copied $bytesCopied bytes successfully")
                }
            }
            file
        } catch (e: Exception) {
            Log.e(TAG, "Failed to copy Uri to file", e)
            if (file.exists()) file.delete()
            null
        }
    }
}