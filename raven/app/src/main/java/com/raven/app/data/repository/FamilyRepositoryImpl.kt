package com.raven.app.data.repository

import com.raven.app.data.local.dao.FamilyDao
import com.raven.app.data.local.entities.CommitmentEntity
import com.raven.app.data.local.entities.CommitmentMemberEntity
import com.raven.app.data.local.entities.FamilyMemberEntity
import com.raven.app.domain.model.*
import com.raven.app.domain.repository.FamilyRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FamilyRepositoryImpl @Inject constructor(
    private val dao: FamilyDao
) : FamilyRepository {

    override fun getAllMembers(): Flow<List<FamilyMember>> =
        dao.getAllMembers().map { list -> list.map { it.toDomain() } }

    override suspend fun getMemberById(id: Long): FamilyMember? =
        dao.getMemberById(id)?.toDomain()

    override suspend fun saveMember(member: FamilyMember): Long =
        dao.insertMember(member.toEntity())

    override suspend fun deleteMember(member: FamilyMember) =
        dao.deleteMember(member.toEntity())

    override fun getAllCommitments(): Flow<List<Commitment>> =
        dao.getAllCommitments().map { list -> list.map { it.toDomain() } }

    override fun getUpcomingCommitments(): Flow<List<Commitment>> =
        dao.getUpcomingCommitments().map { list -> list.map { it.toDomain() } }

    override fun getCommitmentsByType(type: String): Flow<List<Commitment>> =
        dao.getCommitmentsByType(type).map { list -> list.map { it.toDomain() } }

    override fun getCommitmentsForDateRange(startMillis: Long, endMillis: Long): Flow<List<Commitment>> =
        dao.getCommitmentsForDateRange(startMillis, endMillis).map { list -> list.map { it.toDomain() } }

    override suspend fun getCommitmentById(id: Long): Commitment? =
        dao.getCommitmentById(id)?.toDomain()

    override suspend fun saveCommitment(commitment: Commitment): Long {
        val id = dao.insertCommitment(commitment.toEntity())
        dao.deleteAllMembersForCommitment(id)
        commitment.members.forEach { member ->
            dao.insertCommitmentMember(CommitmentMemberEntity(commitmentId = id, memberId = member.id))
        }
        return id
    }

    override suspend fun deleteCommitment(commitment: Commitment) =
        dao.deleteCommitment(commitment.toEntity())

    override suspend fun setCommitmentCompleted(id: Long, completed: Boolean) =
        dao.setCommitmentCompleted(id, completed)

    private fun FamilyMemberEntity.toDomain() = FamilyMember(
        id = id, name = name, relation = relation,
        dateOfBirthMillis = dateOfBirthMillis, avatarColor = avatarColor,
        notes = notes, createdAt = createdAt
    )

    private fun FamilyMember.toEntity() = FamilyMemberEntity(
        id = id, name = name, relation = relation,
        dateOfBirthMillis = dateOfBirthMillis, avatarColor = avatarColor,
        notes = notes, createdAt = createdAt
    )

    private fun CommitmentEntity.toDomain() = Commitment(
        id = id, title = title, description = description,
        dateTimeMillis = dateTimeMillis,
        type = CommitmentType.valueOf(type),
        isRecurring = isRecurring, recurringPattern = recurringPattern,
        location = location, isCompleted = isCompleted,
        reminderMillis = reminderMillis, createdAt = createdAt
    )

    private fun Commitment.toEntity() = CommitmentEntity(
        id = id, title = title, description = description,
        dateTimeMillis = dateTimeMillis, type = type.name,
        isRecurring = isRecurring, recurringPattern = recurringPattern,
        location = location, isCompleted = isCompleted,
        reminderMillis = reminderMillis, createdAt = createdAt
    )
}
