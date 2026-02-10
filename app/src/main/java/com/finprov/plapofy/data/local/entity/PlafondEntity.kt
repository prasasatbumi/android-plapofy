package com.finprov.plapofy.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.finprov.plapofy.domain.model.Plafond
import com.finprov.plapofy.domain.model.ProductInterest
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

@Entity(tableName = "plafond")
data class PlafondEntity(
    @PrimaryKey
    val id: Long,
    val code: String,
    val name: String,
    val description: String,
    val minAmount: Double,
    val maxAmount: Double,
    val interestsJson: String
) {
    fun toDomain(): Plafond {
        val interestListType = object : TypeToken<List<ProductInterest>>() {}.type
        val interests: List<ProductInterest> = try {
            Gson().fromJson(interestsJson, interestListType) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
        
        return Plafond(
            id = id,
            code = code,
            name = name,
            description = description,
            minAmount = minAmount,
            maxAmount = maxAmount,
            interests = interests
        )
    }
}

fun Plafond.toEntity(): PlafondEntity {
    return PlafondEntity(
        id = id,
        code = code,
        name = name,
        description = description,
        minAmount = minAmount,
        maxAmount = maxAmount,
        interestsJson = Gson().toJson(interests)
    )
}
