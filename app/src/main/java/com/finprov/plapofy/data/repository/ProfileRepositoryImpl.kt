package com.finprov.plapofy.data.repository

import com.finprov.plapofy.data.local.TokenManager
import com.finprov.plapofy.data.local.dao.UserDao
import com.finprov.plapofy.data.remote.api.ProfileApi
import com.finprov.plapofy.data.remote.dto.ProfileDto
import com.finprov.plapofy.data.remote.dto.UpdateProfileRequest
import com.finprov.plapofy.data.remote.dto.toEntity
import com.finprov.plapofy.domain.model.User
import com.finprov.plapofy.domain.repository.ProfileCompletenessResult
import com.finprov.plapofy.domain.repository.ProfileRepository
import kotlinx.coroutines.flow.first
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import javax.inject.Inject

class ProfileRepositoryImpl @Inject constructor(
    private val api: ProfileApi,
    private val tokenManager: TokenManager,
    private val userDao: UserDao
) : ProfileRepository {

    override suspend fun submitKyc(ktpFile: File, selfieFile: File): Result<Unit> {
        return try {
            val ktpRequestBody = ktpFile.asRequestBody("image/*".toMediaTypeOrNull())
            val ktpPart = MultipartBody.Part.createFormData("ktpImage", ktpFile.name, ktpRequestBody)

            val selfieRequestBody = selfieFile.asRequestBody("image/*".toMediaTypeOrNull())
            val selfiePart = MultipartBody.Part.createFormData("selfieImage", selfieFile.name, selfieRequestBody)

            val response = api.submitKyc(ktpPart, selfiePart)
            if (response.success) {
                // Refresh profile to update cache with new image paths and status
                getProfile()
                Result.success(Unit)
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getProfile(): Result<User> {
        return try {
            val response = api.getProfile()
            if (response.success) {
                if (response.data != null) {
                    // Cache it
                    userDao.insertUser(response.data.toEntity())
                    
                    // Update PIN status in TokenManager
                    tokenManager.setPinSet(response.data.pinSet)
                    
                    val user = response.data.toDomain()
                    Result.success(user)
                } else {
                    // If no data, return empty user (but no cache update logic for empty?)
                    // Maybe clear cache?
                    Result.success(getEmptyUser())
                }
            } else {
                throw Exception(response.message)
            }
        } catch (e: Exception) {
            // Fallback
            try {
                val cached = userDao.getUser().first()
                if (cached != null) {
                    Result.success(cached.toDomain())
                } else {
                     // Determine if it was network error or other.
                     // If network error, ideally return cached. If null, return failure.
                     Result.failure(e)
                }
            } catch (localE: Exception) {
                Result.failure(e)
            }
        }
    }

    override suspend fun updateProfile(
        name: String?,
        email: String?,
        phoneNumber: String?,
        nik: String?,
        npwp: String?,
        address: String?,
        occupation: String?,
        monthlyIncome: Double?,
        bankName: String?,
        bankAccountNumber: String?
    ): Result<User> {
        return try {
            val request = UpdateProfileRequest(
                name = name,
                email = email,
                phoneNumber = phoneNumber,
                nik = nik,
                npwp = npwp,
                address = address,
                occupation = occupation,
                monthlyIncome = monthlyIncome,
                bankName = bankName,
                bankAccountNumber = bankAccountNumber
            )
            val response = api.updateProfile(request)
            if (response.success && response.data != null) {
                // Update cache
                userDao.insertUser(response.data.toEntity())
                
                // Update PIN status in TokenManager
                tokenManager.setPinSet(response.data.pinSet)
                
                val user = response.data.toDomain()
                Result.success(user)
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: retrofit2.HttpException) {
            if (e.code() == 409) {
                Result.failure(Exception("NIK atau Data sudah terdaftar. Gunakan NIK lain."))
            } else {
                Result.failure(Exception(e.message() ?: "Terjadi kesalahan server"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun checkProfileComplete(): Result<ProfileCompletenessResult> {
        return try {
            val response = api.checkProfileComplete()
            if (response.success && response.data != null) {
                Result.success(
                    ProfileCompletenessResult(
                        isComplete = response.data.isComplete,
                        missingFields = response.data.missingFields ?: emptyList()
                    )
                )
            } else {
                throw Exception(response.message)
            }
        } catch (e: Exception) {
            // Network error or other issues
            try {
                // Try to get from cache
                val cached = userDao.getUser().first()
                
                if (cached != null) {
                    val user = cached.toDomain()
                    val isComplete = user.isProfileComplete()
                    val missing = user.getMissingFields()
                    
                    // If cached user exists, return success with local check result
                    Result.success(ProfileCompletenessResult(isComplete, missing))
                } else {
                    // No cache, return original error
                    Result.failure(e)
                }
            } catch (localE: Exception) {
                // If local check fails, return original error
                Result.failure(e)
            }
        }
    }
    
    override suspend fun uploadProfilePicture(imageFile: File): Result<String> {
        return try {
            val requestBody = imageFile.asRequestBody("image/*".toMediaTypeOrNull())
            val imagePart = MultipartBody.Part.createFormData("profilePicture", imageFile.name, requestBody)

            val response = api.uploadProfilePicture(imagePart)
            if (response.success) {
                // Refresh profile to update cache with new profile picture path
                getProfile()
                Result.success("Profile picture uploaded successfully")
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Keep toDomain here or use mapped one.
    // Similar to LoanRepo, avoiding too many changes.
    private fun ProfileDto.toDomain(): User {
        return User(
            id = id,
            username = username,
            email = email,
            name = name,
            phoneNumber = phoneNumber,
            nik = nik,
            npwp = npwp,
            address = address,
            occupation = occupation,
            monthlyIncome = monthlyIncome,
            bankName = bankName,
            bankAccountNumber = bankAccountNumber,
            isActive = isActive,
            kycStatus = kycStatus,
            ktpImagePath = ktpImagePath,
            selfieImagePath = selfieImagePath,
            isGoogleUser = isGoogleUser,
            pinSet = pinSet
        )
    }

    private fun getEmptyUser(): User {
         return User(
            id = 0,
            username = "",
            email = null,
            name = null,
            phoneNumber = null,
            nik = null,
            npwp = null,
            address = null,
            occupation = null,
            monthlyIncome = null,
            bankName = null,
            bankAccountNumber = null,
            isGoogleUser = false,
            pinSet = false
        )
    }
}
