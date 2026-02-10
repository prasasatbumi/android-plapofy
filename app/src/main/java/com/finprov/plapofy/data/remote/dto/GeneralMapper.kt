package com.finprov.plapofy.data.remote.dto

import com.finprov.plapofy.data.local.entity.BranchEntity
import com.finprov.plapofy.data.local.entity.PlafondEntity
import com.finprov.plapofy.domain.model.ProductInterest
import com.google.gson.Gson

fun PlafondDto.toEntity(): PlafondEntity {
    val interests = interests?.map { 
        ProductInterest(it.id, it.tenor, it.interestRate) 
    } ?: emptyList()

    return PlafondEntity(
        id = id,
        code = code,
        name = name,
        description = description ?: "",
        minAmount = minAmount,
        maxAmount = maxAmount,
        interestsJson = Gson().toJson(interests)
    )
}

fun BranchDto.toEntity(): BranchEntity {
    return BranchEntity(
        id = id,
        name = name,
        location = location
    )
}
