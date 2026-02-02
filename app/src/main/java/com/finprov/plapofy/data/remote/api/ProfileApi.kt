package com.finprov.plapofy.data.remote.api

import com.finprov.plapofy.data.remote.dto.ApiResponse
import com.finprov.plapofy.data.remote.dto.ProfileCheckResponse
import com.finprov.plapofy.data.remote.dto.ProfileDto
import com.finprov.plapofy.data.remote.dto.UpdateProfileRequest
import okhttp3.MultipartBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part

interface ProfileApi {
    @GET("profile")
    suspend fun getProfile(): ApiResponse<ProfileDto>
    
    @PUT("profile")
    suspend fun updateProfile(@Body request: UpdateProfileRequest): ApiResponse<ProfileDto>
    
    @GET("profile/check-complete")
    suspend fun checkProfileComplete(): ApiResponse<ProfileCheckResponse>

    @Multipart
    @POST("customers/kyc")
    suspend fun submitKyc(
        @Part ktpImage: MultipartBody.Part,
        @Part selfieImage: MultipartBody.Part
    ): ApiResponse<Any>

    @Multipart
    @POST("customers/profile-picture")
    suspend fun uploadProfilePicture(
        @Part profilePicture: MultipartBody.Part
    ): ApiResponse<Any>
}

