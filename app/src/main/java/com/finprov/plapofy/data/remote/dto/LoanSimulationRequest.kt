package com.finprov.plapofy.data.remote.dto

import com.google.gson.annotations.SerializedName

data class LoanSimulationRequest(
    val plafondId: Long,
    val amount: Double,
    val tenorMonth: Int
)

data class LoanSimulationResponse(
    val plafondId: Long? = null, // Backend doesn't strictly return this in the response DTO shown, but it might be consistent.
    @SerializedName("requestedAmount") val amount: Double,
    @SerializedName("tenorMonth") val tenor: Int,
    val interestRate: Double,
    val monthlyInstallment: Double
)
