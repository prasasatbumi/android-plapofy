package com.finprov.plapofy.data.remote.dto

data class SetPinRequest(
    val currentPassword: String,
    val pin: String,
    val confirmPin: String
)

data class ChangePinRequest(
    val currentPin: String,
    val newPin: String,
    val confirmPin: String
)

data class VerifyPinRequest(
    val pin: String
)

data class PinStatusResponse(
    val pinSet: Boolean
)

data class VerifyPinResponse(
    val valid: Boolean,
    val message: String
)
