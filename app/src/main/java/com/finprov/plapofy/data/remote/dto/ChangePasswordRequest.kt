package com.finprov.plapofy.data.remote.dto

data class ChangePasswordRequest(
    val oldPassword: String,
    val newPassword: String
)
