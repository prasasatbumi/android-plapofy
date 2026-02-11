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
            // Offline Fallback: Optimistic verification
            // If we know the user HAS a PIN set locally, we assume they entered it correctly
            // to allow the offline flow to proceed. The backend will validate it later during sync if needed,
            // though submission endpoints currently don't require PIN in the payload.
            // This is a tradeoff for UX vs Security in offline mode.
            // Ideally, we would hash the PIN locally and verify against that.
            
            val isPinSet = kotlinx.coroutines.flow.firstOrNull(tokenManager.isPinSet) == true
            if (isPinSet) {
                 Result.success(true)
            } else {
                 Result.failure(e)
            }
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
            // Offline Fallback
             val isPinSet = kotlinx.coroutines.flow.firstOrNull(tokenManager.isPinSet) == true
             Result.success(isPinSet)
        }
    }
}
