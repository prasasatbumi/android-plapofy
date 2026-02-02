package com.finprov.plapofy.domain.repository

import com.finprov.plapofy.domain.model.Loan
import com.finprov.plapofy.domain.model.LoanSimulation

interface LoanRepository {
    suspend fun simulateLoan(plafondId: Long, amount: Double, tenorMonth: Int): Result<LoanSimulation>
    suspend fun submitLoan(plafondId: Long, amount: Double, tenor: Int, purpose: String?, branchId: Long, latitude: Double?, longitude: Double?): Result<Loan>
    suspend fun getMyLoans(): Result<List<Loan>>
    suspend fun mockFinishLoan(loanId: Long): Result<Loan>
}
