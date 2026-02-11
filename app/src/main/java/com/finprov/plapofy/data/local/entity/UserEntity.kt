package com.finprov.plapofy.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.finprov.plapofy.domain.model.User
import com.google.gson.annotations.SerializedName

@Entity(tableName = "user")
data class UserEntity(
    @PrimaryKey
    val id: Long,
    val username: String,
    val email: String?,
    val name: String?,
    val phoneNumber: String?,
    val nik: String?,
    val npwp: String?,
    val address: String?,
    val occupation: String?,
    val monthlyIncome: Double?,
    val bankName: String?,
    val bankAccountNumber: String?,
    val isActive: Boolean = true,
    val kycStatus: String? = null,
    @SerializedName("ktpImagePath") val ktpImagePath: String? = null,
    @SerializedName("selfieImagePath") val selfieImagePath: String? = null,
    @SerializedName("isGoogleUser") val isGoogleUser: Boolean = false,
    @SerializedName("pinSet") val pinSet: Boolean = false
) {
    fun toDomain(): User {
        return User(
            id = id,
            username = username,
            email = email,
            name = name,
            phoneNumber = phoneNumber,
            nik = nik,
            npwp = npwp,
            address = address,
            occupation = occupation,
            monthlyIncome = monthlyIncome,
            bankName = bankName,
            bankAccountNumber = bankAccountNumber,
            isActive = isActive,
            kycStatus = kycStatus,
            ktpImagePath = ktpImagePath,
            selfieImagePath = selfieImagePath,
            isGoogleUser = isGoogleUser,
            pinSet = pinSet
        )
    }
}

fun User.toEntity(): UserEntity {
    return UserEntity(
        id = id,
        username = username,
        email = email,
        name = name,
        phoneNumber = phoneNumber,
        nik = nik,
        npwp = npwp,
        address = address,
        occupation = occupation,
        monthlyIncome = monthlyIncome,
        bankName = bankName,
        bankAccountNumber = bankAccountNumber,
        isActive = isActive,
        kycStatus = kycStatus,
        ktpImagePath = ktpImagePath,
        selfieImagePath = selfieImagePath,
        isGoogleUser = isGoogleUser,
        pinSet = pinSet
    )
}
