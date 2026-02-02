package com.finprov.plapofy.data.remote.dto

import com.google.gson.annotations.SerializedName

data class GoogleSignInRequest(
    @SerializedName("idToken") val idToken: String,
    @SerializedName("fcmToken") val fcmToken: String? = null
)
