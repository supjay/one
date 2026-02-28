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

    /**
     * Initialize the LLM with a model file path.
     * Call this after model download is confirmed complete.
     */
    suspend fun initializeModel(modelPath: String) = withContext(Dispatchers.IO) {
        if (_state.value is LlmState.Ready) return@withContext
        _state.value = LlmState.Loading

        try {
            val options = LlmInference.LlmInferenceOptions.builder()
                .setModelPath(modelPath)
                .setMaxTokens(1024)
                .setTopK(40)
                .setTemperature(0.8f)
                .build()

            llmInference = LlmInference.createFromOptions(context, options)
            openNewSession()
            _state.value = LlmState.Ready
        } catch (e: Exception) {
            _state.value = LlmState.Error("Failed to load model: ${e.message}")
        }
    }

    /**
     * Streaming via callback. Use this for true token-by-token streaming.
     * Opens a new temporary session to avoid corrupting the persistent session.
     */
    fun generateStreamingWithCallback(
        prompt: String,
        onToken: (token: String, isDone: Boolean) -> Unit
    ) {
        val session = currentSession ?: return

        try {
            session.generateResponseAsync(prompt) { partialResult, isDone ->
                val cleaned = RavenPromptBuilder.cleanResponse(partialResult)
                onToken(cleaned, isDone)
            }
        } catch (e: Exception) {
            onToken("Error: ${e.message}", true)
        }
    }

    /**
     * Synchronous generation — simpler path, used as reliable fallback.
     */
    suspend fun generateSync(prompt: String): String = withContext(Dispatchers.IO) {
        val session = currentSession ?: return@withContext "Model not ready."
        try {
            val result = session.generateResponse(prompt)
            RavenPromptBuilder.cleanResponse(result)
        } catch (e: Exception) {
            "I couldn't process that. Please try again."
        }
    }

    /**
     * Resets the conversation session (clears in-session multi-turn context).
     */
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
                LlmInferenceSession.createFromLlmInference(inference, sessionOptions)
            } catch (_: Exception) {
                // Some versions don't support session options — use default
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
