package com.finprov.plapofy.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.finprov.plapofy.data.local.entity.CreditLineEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CreditLineDao {
    @Query("SELECT * FROM credit_lines")
    fun getAllCreditLines(): Flow<List<CreditLineEntity>>

    @Query("SELECT * FROM credit_lines WHERE id = :id")
    suspend fun getCreditLineById(id: Long): CreditLineEntity?

    @Query("SELECT * FROM credit_lines WHERE status IN ('APPROVED', 'ACTIVE') LIMIT 1")
    fun getActiveCreditLine(): Flow<CreditLineEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(creditLines: List<CreditLineEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(creditLine: CreditLineEntity)

    @Query("DELETE FROM credit_lines")
    suspend fun deleteAll()
}
