package com.finprov.plapofy.domain.repository

import com.finprov.plapofy.domain.model.User

import java.io.File

interface ProfileRepository {
    suspend fun getProfile(): Result<User>
    suspend fun updateProfile(
        name: String?,
        email: String?,
        phoneNumber: String?,
        nik: String?,
        npwp: String?,
        address: String?,
        occupation: String?,
        monthlyIncome: Double?,
        bankName: String?,
        bankAccountNumber: String?
    ): Result<User>
    suspend fun checkProfileComplete(): Result<ProfileCompletenessResult>
    suspend fun submitKyc(ktpFile: File, selfieFile: File): Result<Unit>
    suspend fun uploadProfilePicture(imageFile: File): Result<String>
}

data class ProfileCompletenessResult(
    val isComplete: Boolean,
    val missingFields: List<String>
)

