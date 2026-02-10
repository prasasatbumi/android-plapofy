package com.finprov.plapofy.data.remote.api

import com.finprov.plapofy.data.remote.dto.ApiResponse
import com.finprov.plapofy.data.remote.dto.ChangePasswordRequest
import com.finprov.plapofy.data.remote.dto.GoogleSignInRequest
import com.finprov.plapofy.data.remote.dto.LoginRequest
import com.finprov.plapofy.data.remote.dto.LoginResponse
import com.finprov.plapofy.data.remote.dto.RegisterRequest
import com.finprov.plapofy.data.remote.dto.RegisterResponse
import com.finprov.plapofy.data.remote.dto.ResetPasswordRequest
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): ApiResponse<LoginResponse>
    
    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): ApiResponse<RegisterResponse>
    
    @POST("auth/logout")
    suspend fun logout(): ApiResponse<Unit>
    
    @POST("auth/change-password")
    suspend fun changePassword(@Body request: ChangePasswordRequest): ApiResponse<Unit>
    

    @POST("auth/reset-password")
    suspend fun resetPassword(
        @Body request: ResetPasswordRequest
    ): ApiResponse<Any>

    @POST("auth/forgot-password")
    suspend fun forgotPassword(
        @Body request: com.finprov.plapofy.data.remote.dto.ForgotPasswordRequest
    ): ApiResponse<Any>
    
    @POST("auth/google-signin")
    suspend fun googleSignIn(@Body request: GoogleSignInRequest): ApiResponse<LoginResponse>

    @POST("auth/fcm-token")
    suspend fun updateFcmToken(@Body request: Map<String, String>): ApiResponse<Unit>
}
