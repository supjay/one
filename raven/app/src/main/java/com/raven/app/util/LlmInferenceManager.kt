package com.raven.app.util

import android.content.Context
import com.google.mediapipe.tasks.genai.llminference.LlmInference
import com.google.mediapipe.tasks.genai.llminference.LlmInferenceSession
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

sealed class LlmState {
    object Uninitialized : LlmState()
    object Loading : LlmState()
    object Ready : LlmState()
    data class Error(val message: String) : LlmState()
}

@Singleton
class LlmInferenceManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val _state = MutableStateFlow<LlmState>(LlmState.Uninitialized)
    val state: StateFlow<LlmState> = _state.asStateFlow()

    val isReady: Boolean get() = _state.value is LlmState.Ready

    private var llmInference: LlmInference? = null
    private var currentSession: LlmInferenceSession? = null

    suspend fun initializeModel(modelPath: String) = withContext(Dispatchers.IO) {
        if (_state.value is LlmState.Ready) return@withContext
        _state.value = LlmState.Loading

        try {
            val options = LlmInference.LlmInferenceOptions.builder()
                .setModelPath(modelPath)
                .setMaxTokens(1024)
                .build()

            llmInference = LlmInference.createFromOptions(context, options)
            openNewSession()
            _state.value = LlmState.Ready
        } catch (e: Exception) {
            _state.value = LlmState.Error("Failed to load model: ${e.message}")
        }
    }

    /**
     * Streaming via callback. Prompt is enqueued with addQueryChunk before generate.
     */
    fun generateStreamingWithCallback(
        prompt: String,
        onToken: (token: String, isDone: Boolean) -> Unit
    ) {
        val session = currentSession ?: return

        try {
            session.addQueryChunk(prompt)
            session.generateResponseAsync { partialResult: String?, isDone: Boolean? ->
                val cleaned = RavenPromptBuilder.cleanResponse(partialResult ?: "")
                onToken(cleaned, isDone ?: false)
            }
        } catch (e: Exception) {
            onToken("Error: ${e.message}", true)
        }
    }

    /**
     * Synchronous generation. Prompt is enqueued with addQueryChunk before generate.
     */
    suspend fun generateSync(prompt: String): String = withContext(Dispatchers.IO) {
        val session = currentSession ?: return@withContext "Model not ready."
        try {
            session.addQueryChunk(prompt)
            val result = session.generateResponse()
            RavenPromptBuilder.cleanResponse(result)
        } catch (e: Exception) {
            "I couldn't process that. Please try again."
        }
    }

    fun resetSession() {
        currentSession?.close()
        openNewSession()
    }

    private fun openNewSession() {
        currentSession?.close()
        currentSession = llmInference?.let { inference ->
            try {
                val sessionOptions = LlmInferenceSession.LlmInferenceSessionOptions.builder()
                    .setTopK(40)
                    .setTemperature(0.8f)
                    .build()
                inference.createSession(sessionOptions)
            } catch (_: Exception) {
                null
            }
        }
    }

    fun close() {
        currentSession?.close()
        currentSession = null
        llmInference?.close()
        llmInference = null
        _state.value = LlmState.Uninitialized
    }
}
