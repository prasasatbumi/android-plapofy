package com.finprov.plapofy.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * DTO for loan data from API
 */
data class LoanDto(
    @SerializedName("id") val id: Long,
    @SerializedName("applicant") val applicant: LoanApplicantDto?,
    @SerializedName("plafond") val plafond: PlafondDto?,
    @SerializedName("amount") val amount: Double,
    @SerializedName("tenor") val tenor: Int,
    @SerializedName("interestRate") val interestRate: Double,
    @SerializedName("monthlyInstallment") val monthlyInstallment: Double,
    @SerializedName("currentStatus") val status: String?,
    @SerializedName("purpose") val purpose: String?,
    @SerializedName("createdAt") val createdAt: String?,
    @SerializedName("approvedAt") val approvedAt: String?,
    @SerializedName("disbursedAt") val disbursedAt: String?,
    @SerializedName("approvals") val approvals: List<LoanApprovalDto>?,
    @SerializedName("branch") val branch: BranchDto?
)

data class LoanApprovalDto(
    @SerializedName("id") val id: Long,
    @SerializedName("statusAfter") val statusAfter: String,
    @SerializedName("role") val role: String,
    @SerializedName("remarks") val remarks: String?,
    @SerializedName("timestamp") val timestamp: String?
)

data class LoanApplicantDto(
    @SerializedName("id") val id: Long,
    @SerializedName("username") val username: String?
)

/**
 * Request DTO for submitting loan application
 */
data class SubmitLoanRequest(
    @SerializedName("plafondId") val plafondId: Long,
    @SerializedName("amount") val amount: Double,
    @SerializedName("tenor") val tenor: Int,
    @SerializedName("purpose") val purpose: String?,
    @SerializedName("branchId") val branchId: Long,
    @SerializedName("latitude") val latitude: Double?,
    @SerializedName("longitude") val longitude: Double?
)
