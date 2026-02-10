package com.finprov.plapofy.data.remote.dto

import com.google.gson.annotations.SerializedName

data class ResetPasswordRequest(
    @SerializedName("username")
    val username: String? = null,
    
    @SerializedName("newPassword")
    val newPassword: String,
    
    @SerializedName("email")
    val email: String? = null,
    
    @SerializedName("token")
    val token: String? = null
)
