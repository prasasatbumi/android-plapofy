package com.finprov.plapofy.data.remote.dto

import com.google.gson.annotations.SerializedName
import com.finprov.plapofy.domain.model.CreditDisbursement
import com.finprov.plapofy.domain.model.CreditLine
import com.finprov.plapofy.domain.model.CreditLineHistory
import com.finprov.plapofy.domain.model.CreditLineStatus

data class CreditLineDto(
    @SerializedName("id") val id: Long,
    @SerializedName("customer") val customer: UserDto?,
    @SerializedName("plafond") val plafond: PlafondDto?,
    @SerializedName("branch") val branch: BranchDto?,
    @SerializedName("tier") val tier: String,
    @SerializedName("requestedAmount") val requestedAmount: Double,
    @SerializedName("approvedLimit") val approvedLimit: Double?,
    @SerializedName("availableBalance") val availableBalance: Double?,
    @SerializedName("interestRate") val interestRate: Double?,
    @SerializedName("status") val status: String,
    @SerializedName("validFrom") val validFrom: String?,
    @SerializedName("validUntil") val validUntil: String?,
    @SerializedName("renewedFrom") val renewedFrom: Long?,
    @SerializedName("createdAt") val createdAt: String,
    @SerializedName("disbursements") val disbursements: List<CreditDisbursementDto>?,
    @SerializedName("approvals") val history: List<CreditLineHistoryDto>?
)

data class CreditLineHistoryDto(
    @SerializedName("id") val id: Long,
    @SerializedName("statusAfter") val statusAfter: String,
    @SerializedName("role") val role: String?,
    @SerializedName("timestamp") val timestamp: String,
    @SerializedName("remarks") val remarks: String?
)

data class CreditDisbursementDto(
    @SerializedName("id") val id: Long,
    @SerializedName("amount") val amount: Double,
    @SerializedName("tenor") val tenor: Int,
    @SerializedName("interestRate") val interestRate: Double,
    @SerializedName("monthlyInstallment") val monthlyInstallment: Double,
    @SerializedName("disbursedAt") val disbursedAt: String,
    @SerializedName("paidAt") val paidAt: String?,
    @SerializedName("status") val status: String
)

data class ApplyCreditLineRequest(
    @SerializedName("plafondId") val plafondId: Long,
    @SerializedName("branchId") val branchId: Long,
    @SerializedName("requestedAmount") val requestedAmount: Double,
    @SerializedName("purpose") val purpose: String?,
    @SerializedName("latitude") val latitude: Double?,
    @SerializedName("longitude") val longitude: Double?
)

data class DisburseRequest(
    @SerializedName("amount") val amount: Double,
    @SerializedName("tenor") val tenor: Int
)

// Mappers
fun CreditLineDto.toDomain(): CreditLine {
    return CreditLine(
        id = id,
        plafondName = plafond?.name,
        plafondMaxAmount = plafond?.maxAmount ?: 0.0,
        tier = tier,
        requestedAmount = requestedAmount,
        approvedLimit = approvedLimit ?: 0.0,
        availableBalance = availableBalance ?: 0.0,
        interestRate = interestRate ?: 0.0,
        status = CreditLineStatus.fromString(status),
        validUntil = validUntil,
        createdAt = createdAt,
        disbursements = disbursements?.map { it.toDomain() } ?: emptyList(),
        history = history?.map { it.toDomain() } ?: emptyList(),
        branchName = branch?.name
    )
}

fun CreditDisbursementDto.toDomain(): CreditDisbursement {
    return CreditDisbursement(
        id = id,
        amount = amount,
        tenor = tenor,
        monthlyInstallment = monthlyInstallment,
        disbursedAt = disbursedAt,
        status = status
    )
}

fun CreditLineHistoryDto.toDomain(): CreditLineHistory {
    return CreditLineHistory(
        id = id,
        statusAfter = statusAfter,
        role = role,
        timestamp = timestamp,
        remarks = remarks
    )
}
