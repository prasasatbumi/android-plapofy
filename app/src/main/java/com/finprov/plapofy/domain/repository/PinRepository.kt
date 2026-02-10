package com.finprov.plapofy.domain.repository



interface PinRepository {
    suspend fun setPin(password: String, pin: String): Result<Unit>
    suspend fun changePin(oldPin: String, newPin: String): Result<Unit>
    suspend fun verifyPin(pin: String): Result<Boolean>
    suspend fun getPinStatus(): Result<Boolean>
}
