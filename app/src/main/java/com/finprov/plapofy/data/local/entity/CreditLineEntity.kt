package com.finprov.plapofy.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.finprov.plapofy.domain.model.CreditLine
import com.finprov.plapofy.domain.model.CreditDisbursement
import com.finprov.plapofy.domain.model.CreditLineHistory
import com.finprov.plapofy.domain.model.CreditLineStatus
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

@Entity(tableName = "credit_lines")
data class CreditLineEntity(
    @PrimaryKey
    val id: Long,
    val plafondName: String?,
    val plafondMaxAmount: Double,
    val tier: String,
    val requestedAmount: Double,
    val approvedLimit: Double,
    val availableBalance: Double,
    val interestRate: Double,
    val status: String,
    val validUntil: String?,
    val createdAt: String?,
    val branchName: String?,
    // Store complex lists as JSON strings
    val disbursementsJson: String,
    val historyJson: String
) {
    fun toDomain(): CreditLine {
        val gson = Gson()
        val disbursementType = object : TypeToken<List<CreditDisbursement>>() {}.type
        val historyType = object : TypeToken<List<CreditLineHistory>>() {}.type

        val disbursements: List<CreditDisbursement> = try {
            gson.fromJson(disbursementsJson, disbursementType) ?: emptyList()
        } catch (e: Exception) { emptyList() }

        val history: List<CreditLineHistory> = try {
            gson.fromJson(historyJson, historyType) ?: emptyList()
        } catch (e: Exception) { emptyList() }

        return CreditLine(
            id = id,
            plafondName = plafondName,
            plafondMaxAmount = plafondMaxAmount,
            tier = tier,
            requestedAmount = requestedAmount,
            approvedLimit = approvedLimit,
            availableBalance = availableBalance,
            interestRate = interestRate,
            status = try { CreditLineStatus.valueOf(status) } catch (e: Exception) { CreditLineStatus.APPLIED },
            validUntil = validUntil,
            createdAt = createdAt,
            disbursements = disbursements,
            history = history,
            branchName = branchName
        )
    }
}

fun CreditLine.toEntity(): CreditLineEntity {
    val gson = Gson()
    return CreditLineEntity(
        id = id,
        plafondName = plafondName,
        plafondMaxAmount = plafondMaxAmount,
        tier = tier,
        requestedAmount = requestedAmount,
        approvedLimit = approvedLimit,
        availableBalance = availableBalance,
        interestRate = interestRate,
        status = status.name,
        validUntil = validUntil,
        createdAt = createdAt,
        branchName = branchName,
        disbursementsJson = gson.toJson(disbursements),
        historyJson = gson.toJson(history)
    )
}
