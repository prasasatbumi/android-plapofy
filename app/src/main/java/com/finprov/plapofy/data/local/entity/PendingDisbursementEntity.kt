package com.finprov.plapofy.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "pending_disbursement")
data class PendingDisbursementEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val creditLineId: Long,
    val amount: Double,
    val tenor: Int,
    val status: String = "PENDING", // PENDING, SENDING, FAILED
    val retryCount: Int = 0,
    val lastAttemptAt: Long? = null,
    val errorMessage: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)
