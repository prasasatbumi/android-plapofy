package com.finprov.plapofy.domain.model

data class Loan(
    val id: Long,
    val userId: Long,
    val plafondId: Long,
    val plafondName: String?,
    val amount: Double,
    val tenor: Int,
    val interestRate: Double,
    val monthlyInstallment: Double,
    val totalPayment: Double,
    val status: LoanStatus,
    val branchName: String?,
    val purpose: String?,
    val createdAt: String?,
    val approvedAt: String?,
    val disbursedAt: String?,
    val approvals: List<LoanApproval> = emptyList()
)

data class LoanApproval(
    val id: Long,
    val statusAfter: LoanStatus,
    val role: String,
    val remarks: String?,
    val timestamp: String?
)

enum class LoanStatus(val displayName: String, val colorName: String) {
    DRAFT("Draft", "secondary"),
    SUBMITTED("Terkirim", "info"),
    PENDING("Menunggu", "warning"),
    REVIEWED("Sedang Direview", "info"),
    APPROVED("Disetujui", "success"),
    REJECTED("Ditolak", "error"),
    DISBURSED("Dicairkan", "primary"),
    PAID("Lunas", "success"),
    COMPLETED("Selesai", "secondary"),
    CANCELLED("Dibatalkan", "error");

    companion object {
        fun fromString(status: String): LoanStatus {
            return entries.find { it.name.equals(status, ignoreCase = true) } ?: PENDING
        }
    }
}
