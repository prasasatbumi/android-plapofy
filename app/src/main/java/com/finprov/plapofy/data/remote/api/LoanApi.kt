package com.finprov.plapofy.data.remote.api

import com.finprov.plapofy.data.remote.dto.ApiResponse
import com.finprov.plapofy.data.remote.dto.LoanDto
import com.finprov.plapofy.data.remote.dto.LoanSimulationRequest
import com.finprov.plapofy.data.remote.dto.LoanSimulationResponse
import com.finprov.plapofy.data.remote.dto.SubmitLoanRequest
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface LoanApi {
    @POST("loans/simulate")
    suspend fun simulate(@Body request: LoanSimulationRequest): ApiResponse<LoanSimulationResponse>
    
    @POST("loans")
    suspend fun submitLoan(@Body request: SubmitLoanRequest): ApiResponse<LoanDto>
    
    @GET("loans")
    suspend fun getMyLoans(): ApiResponse<List<LoanDto>>
    
    @POST("loans/{id}/mock-finish")
    suspend fun mockFinish(@retrofit2.http.Path("id") id: Long): ApiResponse<LoanDto>
}
