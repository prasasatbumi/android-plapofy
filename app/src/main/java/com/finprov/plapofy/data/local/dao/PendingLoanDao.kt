package com.finprov.plapofy.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.finprov.plapofy.data.local.entity.PendingLoanEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PendingLoanDao {
    @Query("SELECT * FROM pending_loan ORDER BY timestamp ASC")
    fun getAllPendingLoansFlow(): Flow<List<PendingLoanEntity>>
    
    @Query("SELECT * FROM pending_loan ORDER BY timestamp ASC")
    suspend fun getAllPendingLoans(): List<PendingLoanEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPendingLoan(loan: PendingLoanEntity)

    @Delete
    suspend fun deletePendingLoan(loan: PendingLoanEntity)
    
    @Query("DELETE FROM pending_loan")
    suspend fun clearAll()
}
