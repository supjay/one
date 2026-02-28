package com.raven.app.data.local.dao

import androidx.room.*
import com.raven.app.data.local.entities.ConversationMessageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AssistantDao {

    @Query("SELECT * FROM conversation_messages ORDER BY timestamp ASC")
    fun getAllMessages(): Flow<List<ConversationMessageEntity>>

    @Query("SELECT * FROM conversation_messages ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getRecentMessages(limit: Int = 50): List<ConversationMessageEntity>

    @Insert
    suspend fun insertMessage(message: ConversationMessageEntity): Long

    @Query("DELETE FROM conversation_messages")
    suspend fun clearHistory()

    @Query("DELETE FROM conversation_messages WHERE id NOT IN (SELECT id FROM conversation_messages ORDER BY timestamp DESC LIMIT :keepCount)")
    suspend fun pruneHistory(keepCount: Int = 200)
}
