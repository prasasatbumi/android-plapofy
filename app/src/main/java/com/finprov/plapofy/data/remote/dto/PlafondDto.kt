package com.finprov.plapofy.data.remote.dto

data class PlafondDto(
    val id: Long,
    val code: String,
    val name: String,
    val description: String,
    val minAmount: Double,
    val maxAmount: Double,
    val interests: List<ProductInterestDto>? = null
)

data class ProductInterestDto(
    val id: Long,
    val tenor: Int,
    val interestRate: Double
)
