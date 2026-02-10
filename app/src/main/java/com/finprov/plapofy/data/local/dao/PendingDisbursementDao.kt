package com.finprov.plapofy.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.finprov.plapofy.data.local.entity.PendingDisbursementEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PendingDisbursementDao {
    @Query("SELECT * FROM pending_disbursement WHERE status = 'PENDING' ORDER BY timestamp DESC")
    fun getAllPendingFlow(): Flow<List<PendingDisbursementEntity>>

    @Query("SELECT * FROM pending_disbursement ORDER BY timestamp DESC")
    suspend fun getAll(): List<PendingDisbursementEntity>

    @Query("SELECT * FROM pending_disbursement WHERE status = 'PENDING' ORDER BY timestamp DESC")
    suspend fun getPendingForSync(): List<PendingDisbursementEntity>

    @Query("SELECT * FROM pending_disbursement WHERE status = 'FAILED' ORDER BY timestamp DESC")
    suspend fun getFailed(): List<PendingDisbursementEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: PendingDisbursementEntity)

    @Update
    suspend fun update(item: PendingDisbursementEntity)

    @Delete
    suspend fun delete(item: PendingDisbursementEntity)

    @Query("DELETE FROM pending_disbursement WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM pending_disbursement")
    suspend fun clearAll()

    @Query("""
        UPDATE pending_disbursement 
        SET status = :status, retryCount = :retryCount, lastAttemptAt = :lastAttemptAt, errorMessage = :errorMessage 
        WHERE id = :id
    """)
    suspend fun updateStatus(id: String, status: String, retryCount: Int, lastAttemptAt: Long?, errorMessage: String?)

    // Count pending disbursements for a credit line (duplicate prevention)
    @Query("SELECT COUNT(*) FROM pending_disbursement WHERE creditLineId = :creditLineId AND status = 'PENDING'")
    suspend fun countPendingForCreditLine(creditLineId: Long): Int
}
