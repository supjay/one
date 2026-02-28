package com.raven.app.data.local.dao

import androidx.room.*
import com.raven.app.data.local.entities.CommitmentEntity
import com.raven.app.data.local.entities.CommitmentMemberEntity
import com.raven.app.data.local.entities.FamilyMemberEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FamilyDao {

    @Query("SELECT * FROM family_members ORDER BY name ASC")
    fun getAllMembers(): Flow<List<FamilyMemberEntity>>

    @Query("SELECT * FROM family_members WHERE id = :id")
    suspend fun getMemberById(id: Long): FamilyMemberEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMember(member: FamilyMemberEntity): Long

    @Update
    suspend fun updateMember(member: FamilyMemberEntity)

    @Delete
    suspend fun deleteMember(member: FamilyMemberEntity)

    // Commitments
    @Query("SELECT * FROM commitments ORDER BY dateTimeMillis ASC")
    fun getAllCommitments(): Flow<List<CommitmentEntity>>

    @Query("SELECT * FROM commitments WHERE isCompleted = 0 ORDER BY dateTimeMillis ASC")
    fun getUpcomingCommitments(): Flow<List<CommitmentEntity>>

    @Query("SELECT * FROM commitments WHERE type = :type ORDER BY dateTimeMillis ASC")
    fun getCommitmentsByType(type: String): Flow<List<CommitmentEntity>>

    @Query("SELECT * FROM commitments WHERE dateTimeMillis BETWEEN :startMillis AND :endMillis ORDER BY dateTimeMillis ASC")
    fun getCommitmentsForDateRange(startMillis: Long, endMillis: Long): Flow<List<CommitmentEntity>>

    @Query("SELECT * FROM commitments WHERE id = :id")
    suspend fun getCommitmentById(id: Long): CommitmentEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCommitment(commitment: CommitmentEntity): Long

    @Update
    suspend fun updateCommitment(commitment: CommitmentEntity)

    @Delete
    suspend fun deleteCommitment(commitment: CommitmentEntity)

    @Query("UPDATE commitments SET isCompleted = :completed WHERE id = :id")
    suspend fun setCommitmentCompleted(id: Long, completed: Boolean)

    // Commitment-Member relationships
    @Query("SELECT * FROM commitment_members WHERE commitmentId = :commitmentId")
    suspend fun getMembersForCommitment(commitmentId: Long): List<CommitmentMemberEntity>

    @Query("SELECT * FROM commitment_members WHERE memberId = :memberId")
    fun getCommitmentsForMember(memberId: Long): Flow<List<CommitmentMemberEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCommitmentMember(link: CommitmentMemberEntity)

    @Query("DELETE FROM commitment_members WHERE commitmentId = :commitmentId")
    suspend fun deleteAllMembersForCommitment(commitmentId: Long)
}
