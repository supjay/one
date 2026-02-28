package com.raven.app.domain.repository

import com.raven.app.domain.model.ConversationMessage
import kotlinx.coroutines.flow.Flow

interface AssistantRepository {
    fun getAllMessages(): Flow<List<ConversationMessage>>
    suspend fun getRecentMessages(limit: Int = 50): List<ConversationMessage>
    suspend fun addMessage(message: ConversationMessage): Long
    suspend fun clearHistory()
}
