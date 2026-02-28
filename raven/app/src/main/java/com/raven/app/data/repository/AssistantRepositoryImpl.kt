package com.raven.app.data.repository

import com.raven.app.data.local.dao.AssistantDao
import com.raven.app.data.local.entities.ConversationMessageEntity
import com.raven.app.domain.model.ConversationMessage
import com.raven.app.domain.model.MessageRole
import com.raven.app.domain.repository.AssistantRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AssistantRepositoryImpl @Inject constructor(
    private val dao: AssistantDao
) : AssistantRepository {

    override fun getAllMessages(): Flow<List<ConversationMessage>> =
        dao.getAllMessages().map { list -> list.map { it.toDomain() } }

    override suspend fun getRecentMessages(limit: Int): List<ConversationMessage> =
        dao.getRecentMessages(limit).map { it.toDomain() }

    override suspend fun addMessage(message: ConversationMessage): Long =
        dao.insertMessage(message.toEntity())

    override suspend fun clearHistory() = dao.clearHistory()

    private fun ConversationMessageEntity.toDomain() = ConversationMessage(
        id = id,
        role = MessageRole.valueOf(role.uppercase()),
        content = content,
        timestamp = timestamp
    )

    private fun ConversationMessage.toEntity() = ConversationMessageEntity(
        id = id,
        role = role.name.lowercase(),
        content = content,
        timestamp = timestamp
    )
}
