package com.finprov.plapofy.domain.model

data class LoanSimulation(
    val plafondId: Long,
    val amount: Double,
    val tenor: Int,
    val interestRate: Double,
    val monthlyInstallment: Double
)
