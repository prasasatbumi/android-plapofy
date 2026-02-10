package com.finprov.plapofy.presentation.pin

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.finprov.plapofy.domain.repository.PinRepository
import com.finprov.plapofy.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PinViewModel @Inject constructor(
    private val pinRepository: PinRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _hasPin = MutableStateFlow<Boolean?>(null)
    val hasPin = _hasPin.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage = _errorMessage.asStateFlow()

    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage = _successMessage.asStateFlow()
    
    // For Verification Result
    private val _isPinVerified = MutableStateFlow(false)
    val isPinVerified = _isPinVerified.asStateFlow()

    private val _isGoogleUser = MutableStateFlow(false)
    val isGoogleUser = _isGoogleUser.asStateFlow()

    init {
        checkUserType()
    }

    private fun checkUserType() {
        viewModelScope.launch {
             val user = authRepository.getCurrentUser()
             _isGoogleUser.value = user?.isGoogleUser == true
        }
    }
    
    fun verifyPassword(password: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            
            val user = authRepository.getCurrentUser()
            if (user?.username != null) {
                authRepository.login(user.username, password)
                    .onSuccess {
                        _isLoading.value = false
                        onSuccess()
                    }
                    .onFailure {
                        _isLoading.value = false
                        _errorMessage.value = "Kata sandi salah"
                    }
            } else {
                _isLoading.value = false
                _errorMessage.value = "Sesi tidak valid, login ulang"
            }
        }
    }

    fun checkPinStatus() {
        viewModelScope.launch {
            _isLoading.value = true
            pinRepository.getPinStatus()
                .onSuccess { status ->
                    _hasPin.value = status
                    _isLoading.value = false
                }
                .onFailure {
                    _isLoading.value = false
                    // Don't show error for status check, default to false
                    _hasPin.value = false
                }
        }
    }

    fun setPin(password: String, pin: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            pinRepository.setPin(password, pin)
                .onSuccess {
                    _isLoading.value = false
                    _successMessage.value = "PIN berhasil dibuat"
                    _hasPin.value = true
                    onSuccess()
                }
                .onFailure { e ->
                    _isLoading.value = false
                    _errorMessage.value = e.message ?: "Gagal membuat PIN"
                }
        }
    }

    fun changePin(oldPin: String, newPin: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            pinRepository.changePin(oldPin, newPin)
                .onSuccess {
                    _isLoading.value = false
                    _successMessage.value = "PIN berhasil diubah"
                    onSuccess()
                }
                .onFailure { e ->
                    _isLoading.value = false
                    _errorMessage.value = e.message ?: "Gagal mengubah PIN"
                }
        }
    }

    fun verifyPin(pin: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            pinRepository.verifyPin(pin)
                .onSuccess { valid ->
                    _isLoading.value = false
                    if (valid) {
                        _isPinVerified.value = true
                        onSuccess()
                    } else {
                        _errorMessage.value = "PIN salah"
                        _isPinVerified.value = false
                    }
                }
                .onFailure { e ->
                    _isLoading.value = false
                    _errorMessage.value = e.message ?: "Gagal memverifikasi PIN"
                }
        }
    }
    
    fun resetState() {
        _errorMessage.value = null
        _successMessage.value = null
        _isPinVerified.value = false
    }
}
