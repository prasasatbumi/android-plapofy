package com.finprov.plapofy.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.finprov.plapofy.domain.model.User
import com.finprov.plapofy.domain.repository.ProfileCompletenessResult
import com.finprov.plapofy.domain.repository.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale
import javax.inject.Inject

data class ProfileState(
    val isLoading: Boolean = false,
    val user: User? = null,
    val error: String? = null,
    val isSaving: Boolean = false,
    val saveSuccess: Boolean = false
)

data class ProfileFormState(
    val name: String = "",
    val email: String = "",
    val phoneNumber: String = "",
    val nik: String = "",
    val npwp: String = "",
    val address: String = "",
    val occupation: String = "",
    val monthlyIncome: String = "",
    val bankName: String = "",
    val bankAccountNumber: String = "",
    val kycStatus: String? = null,
    val isGoogleUser: Boolean = false
)

@HiltViewModel
class UserProfileViewModel @Inject constructor(
    private val profileRepository: ProfileRepository,
    private val authRepository: com.finprov.plapofy.domain.repository.AuthRepository
) : ViewModel() {

    private val _profileState = MutableStateFlow(ProfileState())
    val profileState: StateFlow<ProfileState> = _profileState.asStateFlow()

    private val _formState = MutableStateFlow(ProfileFormState())
    val formState: StateFlow<ProfileFormState> = _formState.asStateFlow()

    private val _completenessState = MutableStateFlow<ProfileCompletenessResult?>(null)
    val completenessState: StateFlow<ProfileCompletenessResult?> = _completenessState.asStateFlow()

    private val _isUserLoggedIn = MutableStateFlow<Boolean?>(null)
    val isUserLoggedIn: StateFlow<Boolean?> = _isUserLoggedIn.asStateFlow()

    init {
        checkLoginAndLoad()
    }

    private fun checkLoginAndLoad() {
        viewModelScope.launch {
            if (authRepository.isLoggedIn()) {
                _isUserLoggedIn.value = true
                loadProfile()
            } else {
                _isUserLoggedIn.value = false
            }
        }
    }

    fun loadProfile() {
        viewModelScope.launch {
            _profileState.value = ProfileState(isLoading = true)
            
            profileRepository.getProfile()
                .onSuccess { user ->
                    _profileState.value = ProfileState(user = user)
                    // Store raw value for editing, but format for display
                    val incomeForDisplay = user.monthlyIncome?.let { formatIncomeForDisplay(it) } ?: ""
                    _formState.value = ProfileFormState(
                        name = user.name ?: "",
                        email = user.email ?: "",
                        phoneNumber = user.phoneNumber ?: "",
                        nik = user.nik ?: "",
                        npwp = user.npwp ?: "",
                        address = user.address ?: "",
                        occupation = user.occupation ?: "",
                        monthlyIncome = incomeForDisplay,
                        bankName = user.bankName ?: "",
                        bankAccountNumber = user.bankAccountNumber ?: "",
                        kycStatus = user.kycStatus,
                        isGoogleUser = user.isGoogleUser
                    )
                }
                .onFailure { exception ->
                    _profileState.value = ProfileState(
                        error = exception.message ?: "Failed to load profile"
                    )
                }
        }
    }

    fun updateFormField(field: ProfileField, value: String) {
        _formState.value = when (field) {
            ProfileField.NAME -> _formState.value.copy(name = value)
            ProfileField.EMAIL -> _formState.value.copy(email = value)
            ProfileField.PHONE -> _formState.value.copy(phoneNumber = value)
            ProfileField.NIK -> _formState.value.copy(nik = value)
            ProfileField.NPWP -> _formState.value.copy(npwp = value)
            ProfileField.ADDRESS -> _formState.value.copy(address = value)
            ProfileField.OCCUPATION -> _formState.value.copy(occupation = value)
            ProfileField.INCOME -> _formState.value.copy(monthlyIncome = value)
            ProfileField.BANK_NAME -> _formState.value.copy(bankName = value)
            ProfileField.BANK_ACCOUNT -> _formState.value.copy(bankAccountNumber = value)
        }
    }

    fun saveProfile() {
        val form = _formState.value
        
        viewModelScope.launch {
            _profileState.value = _profileState.value.copy(isSaving = true, error = null)
            
            profileRepository.updateProfile(
                name = form.name.ifBlank { null },
                email = form.email.ifBlank { null },
                phoneNumber = form.phoneNumber.ifBlank { null },
                nik = form.nik.ifBlank { null },
                npwp = form.npwp.ifBlank { null },
                address = form.address.ifBlank { null },
                occupation = form.occupation.ifBlank { null },
                monthlyIncome = form.monthlyIncome.toDoubleOrNull(),
                bankName = form.bankName.ifBlank { null },
                bankAccountNumber = form.bankAccountNumber.ifBlank { null }
            )
                .onSuccess { user ->
                    _profileState.value = ProfileState(
                        user = user,
                        saveSuccess = true
                    )
                }
                .onFailure { exception ->
                    _profileState.value = _profileState.value.copy(
                        isSaving = false,
                        error = exception.message ?: "Failed to save profile"
                    )
                }
        }
    }

    fun checkProfileCompleteness() {
        viewModelScope.launch {
            profileRepository.checkProfileComplete()
                .onSuccess { result ->
                    _completenessState.value = result
                }
                .onFailure {
                    // If API fails, check locally
                    val user = _profileState.value.user
                    if (user != null) {
                        _completenessState.value = ProfileCompletenessResult(
                            isComplete = user.isProfileComplete(),
                            missingFields = user.getMissingFields()
                        )
                    }
                }
        }
    }

    fun clearSaveSuccess() {
        _profileState.value = _profileState.value.copy(saveSuccess = false)
    }

    fun clearError() {
        _profileState.value = _profileState.value.copy(error = null)
    }

    private fun formatIncomeForDisplay(amount: Double): String {
        val format = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
        return format.format(amount)
    }
}

enum class ProfileField {
    NAME, EMAIL, PHONE, NIK, NPWP, ADDRESS, OCCUPATION, INCOME, BANK_NAME, BANK_ACCOUNT
}
