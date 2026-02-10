package com.finprov.plapofy.data.remote.dto

import com.google.gson.annotations.SerializedName

data class ForgotPasswordRequest(
    @SerializedName("email")
    val email: String
)
