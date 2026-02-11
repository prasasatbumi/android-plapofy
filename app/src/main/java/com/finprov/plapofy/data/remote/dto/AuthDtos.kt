package com.finprov.plapofy.data.remote.dto

import com.google.gson.annotations.SerializedName

// Login Request
data class LoginRequest(
    @SerializedName("username") val username: String,
    @SerializedName("password") val password: String,
    @SerializedName("fcmToken") val fcmToken: String? = null
)

// Register Request
data class RegisterRequest(
    @SerializedName("username") val username: String,
    @SerializedName("password") val password: String,
    @SerializedName("email") val email: String,
    @SerializedName("name") val name: String,
    @SerializedName("phoneNumber") val phoneNumber: String
)

// Login Response
data class LoginResponse(
    @SerializedName("token") val token: String,
    @SerializedName("userId") val userId: Long,
    @SerializedName("username") val username: String,
    @SerializedName("role") val role: String?,
    @SerializedName("roles") val roles: List<String>?,
    @SerializedName("pinSet") val pinSet: Boolean = false
)

// Register Response
data class RegisterResponse(
    @SerializedName("id") val id: Long,
    @SerializedName("username") val username: String,
    @SerializedName("email") val email: String,
    @SerializedName("name") val name: String
)

// User DTO
data class UserDto(
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
    @SerializedName("isGoogleUser") val isGoogleUser: Boolean = false,
    @SerializedName("pinSet") val pinSet: Boolean = false
)
