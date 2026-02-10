package com.finprov.plapofy.data.remote.api

import com.finprov.plapofy.data.remote.dto.ApiResponse
import com.finprov.plapofy.data.remote.dto.ApplyCreditLineRequest
import com.finprov.plapofy.data.remote.dto.CreditDisbursementDto
import com.finprov.plapofy.data.remote.dto.CreditLineDto
import com.finprov.plapofy.data.remote.dto.DisburseRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path


interface CreditLineApi {
    @POST("credit-lines")
    suspend fun applyForCreditLine(@Body request: ApplyCreditLineRequest): ApiResponse<CreditLineDto>

    @GET("credit-lines/my")
    suspend fun getMyCreditLines(): ApiResponse<List<CreditLineDto>>

    @GET("credit-lines/active")
    suspend fun getActiveCreditLine(): ApiResponse<CreditLineDto>

    @GET("credit-lines/{id}")
    suspend fun getCreditLineById(@Path("id") id: Long): ApiResponse<CreditLineDto>

    @POST("credit-lines/{id}/disburse")
    suspend fun disburse(
        @Path("id") id: Long,
        @Body request: DisburseRequest
    ): ApiResponse<CreditDisbursementDto>

    @POST("credit-lines/disbursements/{id}/mock-pay")
    suspend fun mockPayOff(@Path("id") disbursementId: Long): ApiResponse<CreditDisbursementDto>

    @POST("credit-lines/mock-reset")
    suspend fun mockResetCreditLines(): ApiResponse<Unit>
}
