package com.finprov.plapofy.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

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
    val timestamp: Long = System.currentTimeMillis()
)
