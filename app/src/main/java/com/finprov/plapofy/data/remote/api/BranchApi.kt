package com.finprov.plapofy.data.remote.api

import com.finprov.plapofy.data.remote.dto.ApiResponse
import com.finprov.plapofy.data.remote.dto.BranchDto
import retrofit2.http.GET

interface BranchApi {
    @GET("branches")
    suspend fun getBranches(): ApiResponse<List<BranchDto>>
}
