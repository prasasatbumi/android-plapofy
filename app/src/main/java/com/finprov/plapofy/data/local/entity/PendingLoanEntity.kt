package com.finprov.plapofy.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * Status values:
 * - PENDING: Waiting to be synced
 * - SENDING: Currently being sent
 * - FAILED: Failed after max retries
 */
@Entity(tableName = "pending_loan")
data class PendingLoanEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val plafondId: Long,
    val amount: Double,
    val tenor: Int,
    val purpose: String?,
    val branchId: Long,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val type: String = "LOAN", // "LOAN" or "CREDIT_LINE"
    val status: String = "PENDING", // PENDING, SENDING, FAILED
    val retryCount: Int = 0,
    val lastAttemptAt: Long? = null,
    val errorMessage: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)
