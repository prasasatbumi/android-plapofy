package com.finprov.plapofy.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.finprov.plapofy.data.local.entity.BranchEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BranchDao {
    @Query("SELECT * FROM branch")
    fun getBranches(): Flow<List<BranchEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBranches(branches: List<BranchEntity>)

    @Query("DELETE FROM branch")
    suspend fun clearBranches()
}
