package com.finprov.plapofy.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.finprov.plapofy.data.local.entity.PendingLoanEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PendingLoanDao {
    @Query("SELECT * FROM pending_loan ORDER BY timestamp DESC")
    fun getAllPendingLoansFlow(): Flow<List<PendingLoanEntity>>
    
    @Query("SELECT * FROM pending_loan ORDER BY timestamp DESC")
    suspend fun getAllPendingLoans(): List<PendingLoanEntity>
    
    // Get pending items ready for sync (status = PENDING), latest first
    @Query("SELECT * FROM pending_loan WHERE status = 'PENDING' ORDER BY timestamp DESC")
    suspend fun getPendingForSync(): List<PendingLoanEntity>
    
    // Get all non-sent items for display in history
    @Query("SELECT * FROM pending_loan WHERE status != 'SENT' ORDER BY timestamp DESC")
    fun getPendingForDisplay(): Flow<List<PendingLoanEntity>>
    
    // Get failed items
    @Query("SELECT * FROM pending_loan WHERE status = 'FAILED' ORDER BY timestamp DESC")
    suspend fun getFailedLoans(): List<PendingLoanEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPendingLoan(loan: PendingLoanEntity)
    
    @Update
    suspend fun updatePendingLoan(loan: PendingLoanEntity)

    @Delete
    suspend fun deletePendingLoan(loan: PendingLoanEntity)
    
    @Query("DELETE FROM pending_loan WHERE id = :id")
    suspend fun deleteById(id: String)
    
    @Query("DELETE FROM pending_loan")
    suspend fun clearAll()
    
    // Update status by ID
    @Query("UPDATE pending_loan SET status = :status, retryCount = :retryCount, lastAttemptAt = :lastAttemptAt, errorMessage = :errorMessage WHERE id = :id")
    suspend fun updateStatus(id: String, status: String, retryCount: Int, lastAttemptAt: Long?, errorMessage: String?)

    // Count pending submissions by type for duplicate prevention
    @Query("SELECT COUNT(*) FROM pending_loan WHERE type = :type AND status = 'PENDING'")
    suspend fun countPendingByType(type: String): Int
}
