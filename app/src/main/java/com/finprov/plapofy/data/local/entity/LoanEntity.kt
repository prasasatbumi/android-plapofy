package com.finprov.plapofy.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.finprov.plapofy.domain.model.Loan
import com.finprov.plapofy.domain.model.LoanApproval
import com.finprov.plapofy.domain.model.LoanStatus
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

@Entity(tableName = "loan")
data class LoanEntity(
    @PrimaryKey
    val id: Long,
    val userId: Long,
    val plafondId: Long,
    val plafondName: String?,
    val amount: Double,
    val tenor: Int,
    val interestRate: Double,
    val monthlyInstallment: Double,
    val totalPayment: Double,
    val status: String,
    val branchName: String?,
    val purpose: String?,
    val createdAt: String?,
    val approvedAt: String?,
    val disbursedAt: String?,
    val approvalsJson: String
) {
    fun toDomain(): Loan {
        val approvalsType = object : TypeToken<List<LoanApproval>>() {}.type
        val approvals: List<LoanApproval> = try {
            Gson().fromJson(approvalsJson, approvalsType) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }

        return Loan(
            id = id,
            userId = userId,
            plafondId = plafondId,
            plafondName = plafondName,
            amount = amount,
            tenor = tenor,
            interestRate = interestRate,
            monthlyInstallment = monthlyInstallment,
            totalPayment = totalPayment,
            status = LoanStatus.fromString(status),
            branchName = branchName,
            purpose = purpose,
            createdAt = createdAt,
            approvedAt = approvedAt,
            disbursedAt = disbursedAt,
            approvals = approvals
        )
    }
}

fun Loan.toEntity(): LoanEntity {
    return LoanEntity(
        id = id,
        userId = userId,
        plafondId = plafondId,
        plafondName = plafondName,
        amount = amount,
        tenor = tenor,
        interestRate = interestRate,
        monthlyInstallment = monthlyInstallment,
        totalPayment = totalPayment,
        status = status.name,
        branchName = branchName,
        purpose = purpose,
        createdAt = createdAt,
        approvedAt = approvedAt,
        disbursedAt = disbursedAt,
        approvalsJson = Gson().toJson(approvals)
    )
}
