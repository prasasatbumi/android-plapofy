package com.finprov.plapofy.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.finprov.plapofy.data.local.entity.LoanEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LoanDao {
    @Query("SELECT * FROM loan ORDER BY id DESC")
    fun getLoans(): Flow<List<LoanEntity>>
    
    @Query("SELECT * FROM loan WHERE id = :id")
    fun getLoanById(id: Long): Flow<LoanEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLoans(loans: List<LoanEntity>)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLoan(loan: LoanEntity)

    @Query("DELETE FROM loan")
    suspend fun clearLoans()
}
