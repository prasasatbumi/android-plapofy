package com.finprov.plapofy.data.remote.dto

import com.finprov.plapofy.data.local.entity.UserEntity

fun ProfileDto.toEntity(): UserEntity {
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
        isActive = true, // default
        kycStatus = kycStatus ?: "NOT_VERIFIED",
        ktpImagePath = ktpImagePath,
        selfieImagePath = selfieImagePath,
        isGoogleUser = isGoogleUser,
        pinSet = pinSet
    )
}

fun UserDto.toEntity(): UserEntity {
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
        kycStatus = null, // UserDto might not have KYC status, default to null or fetch separately
        ktpImagePath = null,
        selfieImagePath = null,
        isGoogleUser = isGoogleUser,
        pinSet = pinSet
    )
}
