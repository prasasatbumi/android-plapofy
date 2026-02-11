package com.finprov.plapofy.data.repository

import com.finprov.plapofy.data.remote.api.PinApi
import com.finprov.plapofy.data.remote.dto.ChangePinRequest
import com.finprov.plapofy.data.remote.dto.SetPinRequest
import com.finprov.plapofy.data.remote.dto.VerifyPinRequest
import com.finprov.plapofy.domain.repository.PinRepository
import javax.inject.Inject

class PinRepositoryImpl @Inject constructor(
    private val api: PinApi,
    private val userDao: com.finprov.plapofy.data.local.dao.UserDao
) : PinRepository {

    override suspend fun setPin(password: String, pin: String): Result<Unit> {
        return try {
            val response = api.setPin(SetPinRequest(password, pin, pin))
            if (response.isSuccessful) {
                try {
                    val localUser = userDao.getUser().kotlinx.coroutines.flow.firstOrNull()
                    if (localUser != null) {
                        // Generate local hash using BCrypt
                        val hashedPin = org.mindrot.jbcrypt.BCrypt.hashpw(pin, org.mindrot.jbcrypt.BCrypt.gensalt())
                        userDao.insertUser(localUser.copy(isPinSet = true, pinHash = hashedPin))
                    }
                } catch (e: Exception) {}
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
                try {
                    val localUser = userDao.getUser().kotlinx.coroutines.flow.firstOrNull()
                    if (localUser != null) {
                        // Generate local hash using BCrypt
                        val hashedPin = org.mindrot.jbcrypt.BCrypt.hashpw(newPin, org.mindrot.jbcrypt.BCrypt.gensalt())
                        userDao.insertUser(localUser.copy(isPinSet = true, pinHash = hashedPin))
                    }
                } catch (e: Exception) {}
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to change PIN: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun verifyPin(pin: String): Result<Boolean> {
        // 1. Check local DB first for hash
        try {
            val localUser = userDao.getUser().kotlinx.coroutines.flow.firstOrNull()
            if (localUser != null && !localUser.pinHash.isNullOrEmpty()) {
                // Use BCrypt to verify
                try {
                    val isValid = org.mindrot.jbcrypt.BCrypt.checkpw(pin, localUser.pinHash)
                    if (isValid) return Result.success(true)
                    // If local check fails (wrong PIN), return false immediately regardless of online status
                    return Result.success(false)
                } catch (e: Exception) {
                    // unexpected bcrypt error, fallback to remote
                }
            }
        } catch (e: Exception) {
            // Ignore local error, fallback to remote
        }

        // 2. Fallback to Remote
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
        // 1. Check local DB first
        try {
            val localUser = userDao.getUser().kotlinx.coroutines.flow.firstOrNull()
            if (localUser != null && localUser.isPinSet) {
                return Result.success(true)
            }
        } catch (e: Exception) {
            // Ignore local error, try remote
        }

        // 2. Fallback to Remote
        return try {
            val response = api.getPinStatus()
            if (response.isSuccessful && response.body() != null) {
                val isSet = response.body()!!.pinSet
                
                // 3. Update local DB if needed
                try {
                    val localUser = userDao.getUser().kotlinx.coroutines.flow.firstOrNull()
                    if (localUser != null && localUser.isPinSet != isSet) {
                        userDao.insertUser(localUser.copy(isPinSet = isSet))
                    }
                } catch (e: Exception) {
                    // Ignore cache update error
                }
                
                Result.success(isSet)
            } else {
                Result.failure(Exception("Failed to get PIN status"))
            }
        } catch (e: Exception) {
            // If offline and check above failed (or was false), verify if user exists locally
             try {
                val localUser = userDao.getUser().kotlinx.coroutines.flow.firstOrNull()
                if (localUser != null) {
                    // Return local status (which might be false, but at least we checked)
                    Result.success(localUser.isPinSet)
                } else {
                    Result.failure(e)
                }
            } catch (localE: Exception) {
                Result.failure(e)
            }
        }
    }
}
