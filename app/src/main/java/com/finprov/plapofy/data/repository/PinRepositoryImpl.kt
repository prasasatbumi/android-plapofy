package com.finprov.plapofy.data.repository

import com.finprov.plapofy.data.remote.api.PinApi
import com.finprov.plapofy.data.remote.dto.ChangePinRequest
import com.finprov.plapofy.data.remote.dto.SetPinRequest
import com.finprov.plapofy.data.remote.dto.VerifyPinRequest
import com.finprov.plapofy.domain.repository.PinRepository
import javax.inject.Inject

class PinRepositoryImpl @Inject constructor(
    private val api: PinApi,
    private val tokenManager: com.finprov.plapofy.data.local.TokenManager
) : PinRepository {

    override suspend fun setPin(password: String, pin: String): Result<Unit> {
        return try {
            val response = api.setPin(SetPinRequest(password, pin, pin))
            if (response.isSuccessful) {
                tokenManager.setPinSet(true)
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to set PIN: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun changePin(oldPin: String, newPin: String): Result<Unit> {
        return try {
            val response = api.changePin(ChangePinRequest(oldPin, newPin, newPin))
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to change PIN: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun verifyPin(pin: String): Result<Boolean> {
        return try {
            val response = api.verifyPin(VerifyPinRequest(pin))
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.valid)
            } else {
                Result.failure(Exception("Failed to verify PIN: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getPinStatus(): Result<Boolean> {
        return try {
            val response = api.getPinStatus()
            if (response.isSuccessful && response.body() != null) {
                val isSet = response.body()!!.pinSet
                tokenManager.setPinSet(isSet)
                Result.success(isSet)
            } else {
                Result.failure(Exception("Failed to get PIN status"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
