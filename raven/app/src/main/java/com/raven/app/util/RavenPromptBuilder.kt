package com.raven.app.util

import com.raven.app.domain.model.ConversationMessage
import com.raven.app.domain.model.MessageRole

/**
 * Builds prompts in Gemma's chat format:
 * <start_of_turn>user
 * [message]
 * <end_of_turn>
 * <start_of_turn>model
 * [response]
 * <end_of_turn>
 *
 * The system context is prepended to the first user turn.
 */
object RavenPromptBuilder {

    private const val MAX_HISTORY_TURNS = 6  // last 3 exchanges (user + model)
    private const val START_OF_TURN = "<start_of_turn>"
    private const val END_OF_TURN = "<end_of_turn>"
    private const val BOS = "<bos>"

    private val systemPrompt = """
        You are Raven, a warm and intelligent personal assistant living on this device.
        You have access to the user's personal data shown below.

        Your capabilities:
        - Answer questions about their data (reminders, expenses, trips, family)
        - Help log new entries (expense, reminder, note)
        - Provide insights and summaries
        - Give proactive suggestions based on what you see

        Rules:
        - Be concise and friendly — 2-4 sentences unless more detail is needed
        - Ground your answers in the actual data provided, not assumptions
        - For action requests, confirm clearly what you'll do
        - If data is missing, say so honestly rather than guessing
        - Never mention being an AI model or Gemma — you are Raven
    """.trimIndent()

    /**
     * Builds a complete prompt for a new user message, including system context
     * and recent conversation history.
     */
    fun buildFullPrompt(
        appContext: String,
        userInput: String,
        history: List<ConversationMessage>
    ): String {
        val sb = StringBuilder()
        sb.append(BOS)

        // First turn: inject system context + user message together
        val recentHistory = history.takeLast(MAX_HISTORY_TURNS)

        if (recentHistory.isEmpty()) {
            // First message in session — include full system + context preamble
            sb.append(START_OF_TURN).append("user\n")
            sb.append(systemPrompt)
            sb.append("\n\n")
            sb.append(appContext)
            sb.append("\n\n---\n\n")
            sb.append(userInput)
            sb.append("\n").append(END_OF_TURN).append("\n")
        } else {
            // Subsequent messages — replay history, inject context in first user turn
            val firstUserTurn = recentHistory.firstOrNull { it.role == MessageRole.USER }
            recentHistory.forEach { msg ->
                val role = if (msg.role == MessageRole.USER) "user" else "model"
                sb.append(START_OF_TURN).append(role).append("\n")
                if (msg == firstUserTurn) {
                    sb.append(systemPrompt).append("\n\n").append(appContext).append("\n\n---\n\n")
                }
                sb.append(msg.content)
                sb.append("\n").append(END_OF_TURN).append("\n")
            }
            // Current user message
            sb.append(START_OF_TURN).append("user\n")
            sb.append(userInput)
            sb.append("\n").append(END_OF_TURN).append("\n")
        }

        // Signal model turn — Gemma will complete from here
        sb.append(START_OF_TURN).append("model\n")

        return sb.toString()
    }

    /**
     * Strips Gemma control tokens from a generated response for clean display.
     */
    fun cleanResponse(raw: String): String {
        return raw
            .replace(END_OF_TURN, "")
            .replace(START_OF_TURN, "")
            .replace("<eos>", "")
            .replace(BOS, "")
            .trim()
    }
}
