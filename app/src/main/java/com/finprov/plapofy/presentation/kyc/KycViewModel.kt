package com.finprov.plapofy.presentation.kyc

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.finprov.plapofy.domain.repository.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

data class KycState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null,
    val ktpFile: File? = null,
    val selfieFile: File? = null,
    val ktpUrl: String? = null,
    val selfieUrl: String? = null,
    val kycStatus: String? = null
)

@HiltViewModel
class KycViewModel @Inject constructor(
    private val profileRepository: ProfileRepository
) : ViewModel() {

    private val _state = MutableStateFlow(KycState())
    val state: StateFlow<KycState> = _state.asStateFlow()

    init {
        loadCurrentKycData()
    }

    private fun loadCurrentKycData() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            profileRepository.getProfile()
                .onSuccess { user ->
                    _state.value = _state.value.copy(
                        isLoading = false,
                        ktpUrl = user.ktpImagePath,
                        selfieUrl = user.selfieImagePath,
                        kycStatus = user.kycStatus
                    )
                }
                .onFailure {
                    _state.value = _state.value.copy(isLoading = false)
                }
        }
    }

    fun onKtpSelected(file: File) {
        _state.value = _state.value.copy(ktpFile = file, error = null)
    }

    fun onSelfieSelected(file: File) {
        _state.value = _state.value.copy(selfieFile = file, error = null)
    }

    fun submitKyc() {
        val currentState = _state.value
        if (currentState.ktpFile == null || currentState.selfieFile == null) {
            _state.value = currentState.copy(error = "KTP dan Selfie wajib diisi")
            return
        }

        viewModelScope.launch {
            _state.value = currentState.copy(isLoading = true, error = null)
            
            profileRepository.submitKyc(currentState.ktpFile, currentState.selfieFile)
                .onSuccess {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        isSuccess = true
                    )
                }
                .onFailure { exception ->
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = exception.message ?: "Gagal mengirim data KYC"
                    )
                }
        }
    }

    fun clearError() {
        _state.value = _state.value.copy(error = null)
    }
}
