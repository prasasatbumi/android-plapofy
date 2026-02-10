package com.finprov.plapofy.domain.repository

import com.finprov.plapofy.data.remote.dto.ResetPasswordRequest
import com.finprov.plapofy.domain.model.User

interface AuthRepository {
    suspend fun login(username: String, password: String, fcmToken: String? = null): Result<User>
    suspend fun register(
        username: String,
        password: String,
        email: String,
        name: String,
        phoneNumber: String
    ): Result<User>
    suspend fun logout()
    suspend fun isLoggedIn(): Boolean
    suspend fun getCurrentUser(): User?
    suspend fun changePassword(oldPassword: String, newPassword: String): Result<Unit>
    suspend fun resetPassword(request: ResetPasswordRequest): Result<Any>
    suspend fun forgotPassword(email: String): Result<Any>
    suspend fun signInWithGoogle(idToken: String, fcmToken: String? = null): Result<User>
    suspend fun updateFcmToken(fcmToken: String): Result<Unit>
}
