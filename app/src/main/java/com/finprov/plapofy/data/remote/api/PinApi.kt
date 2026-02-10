package com.finprov.plapofy.data.remote.api

import com.finprov.plapofy.data.remote.dto.ChangePinRequest
import com.finprov.plapofy.data.remote.dto.PinStatusResponse
import com.finprov.plapofy.data.remote.dto.SetPinRequest
import com.finprov.plapofy.data.remote.dto.VerifyPinRequest
import com.finprov.plapofy.data.remote.dto.VerifyPinResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface PinApi {
    
    @POST("pin/set")
    suspend fun setPin(@Body request: SetPinRequest): Response<Map<String, String>>
    
    @POST("pin/change")
    suspend fun changePin(@Body request: ChangePinRequest): Response<Map<String, String>>
    
    @POST("pin/verify")
    suspend fun verifyPin(@Body request: VerifyPinRequest): Response<VerifyPinResponse>
    
    @GET("pin/status")
    suspend fun getPinStatus(): Response<PinStatusResponse>
}
