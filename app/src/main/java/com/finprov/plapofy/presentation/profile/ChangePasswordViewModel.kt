package com.finprov.plapofy.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.finprov.plapofy.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ChangePasswordState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class ChangePasswordViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ChangePasswordState())
    val state: StateFlow<ChangePasswordState> = _state.asStateFlow()

    fun changePassword(oldPassword: String, newPassword: String, confirmPassword: String) {
        // Validate inputs
        if (oldPassword.isBlank()) {
            _state.value = ChangePasswordState(error = "Password lama tidak boleh kosong")
            return
        }
        if (newPassword.isBlank()) {
            _state.value = ChangePasswordState(error = "Password baru tidak boleh kosong")
            return
        }
        if (newPassword.length < 6) {
            _state.value = ChangePasswordState(error = "Password baru minimal 6 karakter")
            return
        }
        if (newPassword != confirmPassword) {
            _state.value = ChangePasswordState(error = "Konfirmasi password tidak cocok")
            return
        }
        if (oldPassword == newPassword) {
            _state.value = ChangePasswordState(error = "Password baru harus berbeda dari password lama")
            return
        }

        viewModelScope.launch {
            _state.value = ChangePasswordState(isLoading = true)
            
            authRepository.changePassword(oldPassword, newPassword)
                .onSuccess {
                    _state.value = ChangePasswordState(isSuccess = true)
                }
                .onFailure { exception ->
                    val errorMessage = when {
                        exception.message?.contains("incorrect", ignoreCase = true) == true -> 
                            "Password lama tidak sesuai"
                        exception.message?.contains("401") == true -> 
                            "Sesi telah berakhir. Silakan login kembali"
                        else -> 
                            exception.message ?: "Gagal mengubah password"
                    }
                    _state.value = ChangePasswordState(error = errorMessage)
                }
        }
    }

    fun clearError() {
        _state.value = _state.value.copy(error = null)
    }
}
