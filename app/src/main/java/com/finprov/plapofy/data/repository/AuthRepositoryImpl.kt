package com.finprov.plapofy.data.repository

import com.finprov.plapofy.data.local.TokenManager
import com.finprov.plapofy.data.remote.api.AuthApi
import com.finprov.plapofy.data.remote.dto.ResetPasswordRequest 
import com.finprov.plapofy.data.remote.dto.ForgotPasswordRequest
import com.finprov.plapofy.data.remote.dto.ChangePasswordRequest
import com.finprov.plapofy.data.remote.dto.GoogleSignInRequest
import com.finprov.plapofy.data.remote.dto.LoginRequest
import com.finprov.plapofy.data.remote.dto.RegisterRequest
import com.finprov.plapofy.data.remote.dto.UserDto
import com.finprov.plapofy.data.remote.dto.toEntity
import com.finprov.plapofy.domain.model.User
import com.finprov.plapofy.domain.repository.AuthRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val api: AuthApi,
    private val profileApi: com.finprov.plapofy.data.remote.api.ProfileApi,
    private val tokenManager: TokenManager,
    private val database: com.finprov.plapofy.data.local.AppDatabase
) : AuthRepository {

    override suspend fun login(username: String, password: String, fcmToken: String?): Result<User> {
        return try {
            val response = api.login(LoginRequest(username, password, fcmToken))
            if (response.success && response.data != null) {
                val loginData = response.data // Flattened LoginResponse
                
                // Save token and basic info
                tokenManager.saveAuthData(
                    token = loginData.token,
                    userId = loginData.userId,
                    username = loginData.username,
                    userName = loginData.username // Start with username as name, will update with profile
                )
                
                // CRITICAL: Fetch full profile immediately to cache for offline usage
                try {
                    val profileResponse = profileApi.getProfile()
                    if (profileResponse.success && profileResponse.data != null) {
                        val fullUser = profileResponse.data.toEntity()
                        database.userDao().insertUser(fullUser)
                        return Result.success(fullUser.toDomain())
                    }
                } catch (e: Exception) {
                    android.util.Log.e("AuthRepo", "Failed to fetch profile after login: ${e.message}")
                    // Proceed with basic user if profile fetch fails
                }
                
                val basicUser = User(
                    id = loginData.userId,
                    username = loginData.username,
                    email = null,
                    name = null,
                    phoneNumber = null,
                    nik = null,
                    npwp = null,
                    address = null,
                    occupation = null,
                    monthlyIncome = null,
                    bankName = null,
                    bankAccountNumber = null
                )
                
                Result.success(basicUser)
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun register(
        username: String,
        password: String,
        email: String,
        name: String,
        phoneNumber: String
    ): Result<User> {
        return try {
            val response = api.register(
                RegisterRequest(
                    username = username,
                    password = password,
                    email = email,
                    name = name,
                    phoneNumber = phoneNumber
                )
            )
            if (response.success && response.data != null) {
                val registerData = response.data
                val user = User(
                    id = registerData.id,
                    username = registerData.username,
                    email = registerData.email,
                    name = registerData.name,
                    phoneNumber = phoneNumber,
                    nik = null,
                    npwp = null,
                    address = null,
                    occupation = null,
                    monthlyIncome = null,
                    bankName = null,
                    bankAccountNumber = null
                )
                Result.success(user)
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun logout() {
        try {
            api.logout()
        } catch (e: Exception) {
             android.util.Log.w("AuthRepository", "Failed to logout from backend: ${e.message}")
        }
        tokenManager.clearAuthData()
        database.clearAllTables()
    }

    override suspend fun isLoggedIn(): Boolean {
        return tokenManager.isLoggedIn.first()
    }

    override suspend fun getCurrentUser(): User? {
        val userId = tokenManager.userId.first()
        val username = tokenManager.username.first()
        val userName = tokenManager.userName.first()
        
        return if (userId != null && username != null) {
            User(
                id = userId,
                username = username,
                email = null,
                name = userName,
                phoneNumber = null,
                nik = null,
                npwp = null,
                address = null,
                occupation = null,
                monthlyIncome = null,
                bankName = null,
                bankAccountNumber = null
            )
        } else null
    }

    override suspend fun changePassword(oldPassword: String, newPassword: String): Result<Unit> {
        return try {
            val response = api.changePassword(
                ChangePasswordRequest(
                    oldPassword = oldPassword,
                    newPassword = newPassword
                )
            )
            if (response.success) {
                Result.success(Unit)
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    override suspend fun resetPassword(request: ResetPasswordRequest): Result<Unit> {
        return try {
            val response = api.resetPassword(request)
             if (response.success) {
                Result.success(Unit)
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun forgotPassword(email: String): Result<Unit> {
        return try {
            val request = ForgotPasswordRequest(email)
            val response = api.forgotPassword(request)
            if (response.success) {
                Result.success(Unit)
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    override suspend fun signInWithGoogle(idToken: String, fcmToken: String?): Result<User> {
        return try {
            val response = api.googleSignIn(
                GoogleSignInRequest(idToken = idToken, fcmToken = fcmToken)
            )
            if (response.success && response.data != null) {
                val loginData = response.data // Flattened LoginResponse
                
                tokenManager.saveAuthData(
                    token = loginData.token,
                    userId = loginData.userId,
                    username = loginData.username,
                    userName = loginData.username // Temporary
                )
                
                // CRITICAL: Fetch full profile immediately
                try {
                    val profileResponse = profileApi.getProfile()
                    if (profileResponse.success && profileResponse.data != null) {
                        val fullUser = profileResponse.data.toEntity()
                        database.userDao().insertUser(fullUser)
                        return Result.success(fullUser.toDomain())
                    }
                } catch (e: Exception) {
                     android.util.Log.e("AuthRepo", "Failed to fetch profile after google login: ${e.message}")
                }
                
                val basicUser = User(
                    id = loginData.userId,
                    username = loginData.username,
                    email = null,
                    name = null,
                    phoneNumber = null,
                    nik = null,
                    npwp = null,
                    address = null,
                    occupation = null,
                    monthlyIncome = null,
                    bankName = null,
                    bankAccountNumber = null,
                    isActive = true
                )

                Result.success(basicUser)
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


}

