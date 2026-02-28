package com.raven.app.domain.repository

import com.raven.app.domain.model.Commitment
import com.raven.app.domain.model.FamilyMember
import kotlinx.coroutines.flow.Flow

interface FamilyRepository {
    fun getAllMembers(): Flow<List<FamilyMember>>
    suspend fun getMemberById(id: Long): FamilyMember?
    suspend fun saveMember(member: FamilyMember): Long
    suspend fun deleteMember(member: FamilyMember)

    fun getAllCommitments(): Flow<List<Commitment>>
    fun getUpcomingCommitments(): Flow<List<Commitment>>
    fun getCommitmentsByType(type: String): Flow<List<Commitment>>
    fun getCommitmentsForDateRange(startMillis: Long, endMillis: Long): Flow<List<Commitment>>
    suspend fun getCommitmentById(id: Long): Commitment?
    suspend fun saveCommitment(commitment: Commitment): Long
    suspend fun deleteCommitment(commitment: Commitment)
    suspend fun setCommitmentCompleted(id: Long, completed: Boolean)
}
