package com.finprov.plapofy.data.remote.dto

import com.finprov.plapofy.data.local.entity.LoanEntity
import com.finprov.plapofy.data.local.entity.PendingLoanEntity
import com.finprov.plapofy.domain.model.Loan
import com.finprov.plapofy.domain.model.LoanApproval
import com.finprov.plapofy.domain.model.LoanStatus
import com.finprov.plapofy.domain.model.User
import com.google.gson.Gson

fun LoanDto.toEntity(): LoanEntity {
    return LoanEntity(
        id = id,
        userId = applicant?.id ?: 0L,
        plafondId = plafond?.id ?: 0L,
        plafondName = plafond?.name,
        amount = amount,
        tenor = tenor,
        interestRate = interestRate,
        monthlyInstallment = monthlyInstallment,
        totalPayment = monthlyInstallment * tenor,
        status = status ?: "PENDING",
        branchName = branch?.name,
        purpose = purpose,
        createdAt = createdAt,
        approvedAt = approvedAt,
        disbursedAt = disbursedAt,
        approvalsJson = Gson().toJson(approvals?.map { it.toDomain() } ?: emptyList<LoanApproval>())
    )
}

fun LoanApprovalDto.toDomain(): LoanApproval {
    return LoanApproval(
        id = id,
        statusAfter = LoanStatus.fromString(statusAfter),
        role = role,
        remarks = remarks,
        timestamp = timestamp
    )
}

fun SubmitLoanRequest.toPendingEntity(): PendingLoanEntity {
    return PendingLoanEntity(
        plafondId = plafondId,
        amount = amount,
        tenor = tenor,
        purpose = purpose,
        branchId = branchId,
        latitude = latitude,
        longitude = longitude
    )
}
