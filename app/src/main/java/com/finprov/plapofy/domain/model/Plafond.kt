package com.finprov.plapofy.domain.model

data class Plafond(
    val id: Long,
    val code: String,
    val name: String,
    val description: String,
    val minAmount: Double,
    val maxAmount: Double,
    val interests: List<ProductInterest> = emptyList()
) {
    // Helper properties derived from interests
    val interestRate: Double
        get() = interests.firstOrNull()?.interestRate ?: 0.0
    
    val minTenor: Int
        get() = interests.minOfOrNull { it.tenor } ?: 1
    
    val maxTenor: Int
        get() = interests.maxOfOrNull { it.tenor } ?: 12
}

data class ProductInterest(
    val id: Long,
    val tenor: Int,
    val interestRate: Double
)
