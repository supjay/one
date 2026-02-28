package com.raven.app.util

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject
import javax.inject.Singleton

sealed class DownloadState {
    object Idle : DownloadState()
    data class Downloading(
        val progressPercent: Int,
        val downloadedBytes: Long,
        val totalBytes: Long
    ) : DownloadState()
    object Complete : DownloadState()
    data class Error(val message: String) : DownloadState()
}

@Singleton
class ModelDownloadManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    companion object {
        const val MODEL_FILENAME = "gemma3b.task"
        private const val MODEL_DIR = "raven_models"

        /**
         * Direct download URL for the Gemma 3B IT float16 MediaPipe model.
         *
         * IMPORTANT: Users must download this model file from Google's official source:
         * https://www.kaggle.com/models/google/gemma/tfLite/gemma-3b-it-gpu-int4
         *
         * The URL below is a placeholder. In production, configure your own CDN
         * or instruct users to manually place the file in the models directory.
         *
         * Alternative: Gemma 2B IT is smaller (~1.4GB) and may run on more devices:
         * https://www.kaggle.com/models/google/gemma/tfLite/gemma-2b-it-gpu-int4
         */
        const val MODEL_DOWNLOAD_URL = "https://your-cdn-or-server.example.com/gemma3b.task"
        const val MODEL_SIZE_MB = 2700 // approximate
    }

    private val modelDir: File
        get() = File(context.getExternalFilesDir(null), MODEL_DIR).also { it.mkdirs() }

    val modelFile: File
        get() = File(modelDir, MODEL_FILENAME)

    fun modelExists(): Boolean = modelFile.exists() && modelFile.length() > 1_000_000L

    fun getModelPath(): String = modelFile.absolutePath

    /**
     * Downloads the Gemma model with progress reporting.
     * Supports resuming a partial download via HTTP Range header.
     */
    fun downloadModel(downloadUrl: String = MODEL_DOWNLOAD_URL): Flow<DownloadState> = flow {
        emit(DownloadState.Idle)

        val destFile = modelFile
        val existingBytes = if (destFile.exists()) destFile.length() else 0L

        var connection: HttpURLConnection? = null
        try {
            val url = URL(downloadUrl)
            connection = (url.openConnection() as HttpURLConnection).apply {
                connectTimeout = 30_000
                readTimeout = 60_000
                setRequestProperty("User-Agent", "Raven-Android/1.0")
                if (existingBytes > 0) {
                    setRequestProperty("Range", "bytes=$existingBytes-")
                }
                connect()
            }

            val responseCode = connection.responseCode
            val isResume = responseCode == 206  // HTTP Partial Content
            if (responseCode != 200 && responseCode != 206) {
                emit(DownloadState.Error("Server returned HTTP $responseCode"))
                return@flow
            }

            val contentLength = connection.contentLengthLong
            val totalBytes = if (isResume) existingBytes + contentLength else contentLength

            connection.inputStream.use { input ->
                FileOutputStream(destFile, isResume).use { output ->
                    val buffer = ByteArray(8 * 1024)
                    var downloadedBytes = existingBytes
                    var bytesRead: Int

                    while (input.read(buffer).also { bytesRead = it } != -1) {
                        output.write(buffer, 0, bytesRead)
                        downloadedBytes += bytesRead
                        val progress = if (totalBytes > 0) {
                            ((downloadedBytes.toFloat() / totalBytes) * 100).toInt()
                        } else 0
                        emit(DownloadState.Downloading(progress, downloadedBytes, totalBytes))
                    }
                    output.flush()
                }
            }

            emit(DownloadState.Complete)

        } catch (e: Exception) {
            emit(DownloadState.Error(e.message ?: "Download failed"))
        } finally {
            connection?.disconnect()
        }
    }.flowOn(Dispatchers.IO)

    /**
     * Copies a model file that the user has manually placed in the public Downloads folder.
     * Call this when [downloadUrl] is not available and the user provides the file themselves.
     */
    fun copyFromPath(sourcePath: String): Boolean {
        return try {
            val source = File(sourcePath)
            if (!source.exists()) return false
            source.copyTo(modelFile, overwrite = true)
            true
        } catch (_: Exception) {
            false
        }
    }

    fun deleteModel() {
        modelFile.delete()
    }
}
