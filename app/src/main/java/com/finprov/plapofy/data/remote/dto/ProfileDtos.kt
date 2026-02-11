package com.finprov.plapofy.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * DTO for user profile data from API
 */
data class ProfileDto(
    @SerializedName("id") val id: Long,
    @SerializedName("username") val username: String,
    @SerializedName("email") val email: String?,
    @SerializedName("name") val name: String?,
    @SerializedName("phoneNumber") val phoneNumber: String?,
    @SerializedName("nik") val nik: String?,
    @SerializedName("npwp") val npwp: String?,
    @SerializedName("address") val address: String?,
    @SerializedName("occupation") val occupation: String?,
    @SerializedName("monthlyIncome") val monthlyIncome: Double?,
    @SerializedName("bankName") val bankName: String?,
    @SerializedName("bankAccountNumber") val bankAccountNumber: String?,
    @SerializedName("isActive") val isActive: Boolean = true,
    @SerializedName("kycStatus") val kycStatus: String?,
    @SerializedName("ktpImagePath") val ktpImagePath: String?,
    @SerializedName("selfieImagePath") val selfieImagePath: String?,
    @SerializedName("isGoogleUser") val isGoogleUser: Boolean = false,
    @SerializedName("pinSet") val pinSet: Boolean = false
)

/**
 * Request DTO for updating profile
 */
data class UpdateProfileRequest(
    @SerializedName("name") val name: String?,
    @SerializedName("email") val email: String?,
    @SerializedName("phoneNumber") val phoneNumber: String?,
    @SerializedName("nik") val nik: String?,
    @SerializedName("npwp") val npwp: String?,
    @SerializedName("address") val address: String?,
    @SerializedName("occupation") val occupation: String?,
    @SerializedName("monthlyIncome") val monthlyIncome: Double?,
    @SerializedName("bankName") val bankName: String?,
    @SerializedName("bankAccountNumber") val bankAccountNumber: String?
)

/**
 * Response for profile completeness check
 */
data class ProfileCheckResponse(
    @SerializedName("isComplete") val isComplete: Boolean,
    @SerializedName("missingFields") val missingFields: List<String>?
)
