package com.finprov.plapofy.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.finprov.plapofy.data.local.entity.PlafondEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PlafondDao {
    @Query("SELECT * FROM plafond")
    fun getPlafonds(): Flow<List<PlafondEntity>>
    
    @Query("SELECT * FROM plafond WHERE id = :id")
    suspend fun getPlafondById(id: Long): PlafondEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlafonds(plafonds: List<PlafondEntity>)

    @Query("DELETE FROM plafond")
    suspend fun clearPlafonds()
}
