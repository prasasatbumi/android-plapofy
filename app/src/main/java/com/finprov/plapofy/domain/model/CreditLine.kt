package com.finprov.plapofy.domain.model

data class CreditLine(
    val id: Long,
    val plafondName: String?,
    val plafondMaxAmount: Double,
    val tier: String,
    val requestedAmount: Double,
    val approvedLimit: Double,
    val availableBalance: Double,
    val interestRate: Double,
    val status: CreditLineStatus,
    val validUntil: String?,
    val createdAt: String?,
    val disbursements: List<CreditDisbursement>,
    val history: List<CreditLineHistory> = emptyList(),
    val branchName: String? = null
)

data class CreditDisbursement(
    val id: Long,
    val amount: Double,
    val tenor: Int,
    val monthlyInstallment: Double,
    val disbursedAt: String,
    val status: String
)

data class CreditLineHistory(
    val id: Long,
    val statusAfter: String,
    val role: String?,
    val timestamp: String,
    val remarks: String?
)

enum class CreditLineStatus(val value: String) {
    APPLIED("APPLIED"),
    REVIEWED("REVIEWED"),
    APPROVED("APPROVED"),
    ACTIVE("ACTIVE"),
    DISBURSED("DISBURSED"),
    EXPIRED("EXPIRED"),
    REJECTED("REJECTED"),
    UNKNOWN("UNKNOWN");

    companion object {
        fun fromString(status: String?): CreditLineStatus {
            return entries.find { it.value.equals(status, ignoreCase = true) } ?: UNKNOWN
        }
    }
}
