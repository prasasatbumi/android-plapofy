package com.finprov.plapofy.data.remote.api

import com.finprov.plapofy.data.remote.dto.ApiResponse
import com.finprov.plapofy.data.remote.dto.PlafondDto
import retrofit2.http.GET

interface PlafondApi {
    @GET("plafonds")
    suspend fun getPlafonds(): ApiResponse<List<PlafondDto>>
}
