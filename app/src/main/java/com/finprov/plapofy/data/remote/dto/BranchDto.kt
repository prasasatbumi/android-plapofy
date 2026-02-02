package com.finprov.plapofy.data.remote.dto

import com.google.gson.annotations.SerializedName

data class BranchDto(
    @SerializedName("id") val id: Long,
    @SerializedName("name") val name: String,
    @SerializedName("location") val location: String?
)
