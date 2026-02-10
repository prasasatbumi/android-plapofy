package com.finprov.plapofy.presentation.loan.apply

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.finprov.plapofy.domain.model.Branch
import com.finprov.plapofy.domain.model.CreditLine
import com.finprov.plapofy.domain.model.Plafond
import com.finprov.plapofy.domain.repository.BranchRepository
import com.finprov.plapofy.domain.repository.CreditLineRepository
import com.finprov.plapofy.domain.repository.PlafondRepository
import com.finprov.plapofy.presentation.common.ErrorMessageMapper
import com.finprov.plapofy.domain.repository.ProfileCompletenessResult
import com.finprov.plapofy.domain.repository.ProfileRepository
import com.finprov.plapofy.data.remote.dto.ApplyCreditLineRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ApplyCreditState(
    val isLoading: Boolean = false,
    val plafond: Plafond? = null,
    val branches: List<Branch> = emptyList(),
    val profileCheck: ProfileCompletenessResult? = null,
    val isSubmitting: Boolean = false,
    val submitSuccess: Boolean = false,
    val isPendingSync: Boolean = false,  // True when submitted offline
    val submittedCreditLine: CreditLine? = null,
    val error: String? = null,
    val requiresLogin: Boolean = false
)

@HiltViewModel
class ApplyCreditViewModel @Inject constructor(
    private val creditLineRepository: CreditLineRepository,
    private val plafondRepository: PlafondRepository,
    private val profileRepository: ProfileRepository,
    private val branchRepository: BranchRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ApplyCreditState())
    val uiState: StateFlow<ApplyCreditState> = _uiState.asStateFlow()

    fun loadData(plafondId: Long) {
        viewModelScope.launch {
            _uiState.value = ApplyCreditState(isLoading = true)

            // Load branches
            val branchResult = branchRepository.getBranches()
            val branches = branchResult.getOrDefault(emptyList())

            // Load plafond
            plafondRepository.getPlafonds()
                .onSuccess { plafonds ->
                    val plafond = plafonds.find { it.id == plafondId }
                    if (plafond != null) {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            plafond = plafond,
                            branches = branches
                        )
                        checkProfile()
                    } else {
                        _uiState.value = ApplyCreditState(error = "Produk tidak ditemukan")
                    }
                }
                .onFailure { error ->
                    _uiState.value = ApplyCreditState(error = error.message ?: "Gagal memuat data")
                }
        }
    }

    private fun checkProfile() {
        viewModelScope.launch {
            profileRepository.checkProfileComplete()
                .onSuccess { result ->
                     _uiState.value = _uiState.value.copy(profileCheck = result)
                }
                .onFailure { error ->
                    val isAuthError = error.message?.contains("401") == true || error.message?.contains("403") == true
                    if (isAuthError) {
                        _uiState.value = _uiState.value.copy(requiresLogin = true)
                    } else {
                        _uiState.value = _uiState.value.copy(
                            profileCheck = ProfileCompletenessResult(
                                isComplete = false,
                                missingFields = listOf(error.message ?: "Gagal cek profil")
                            )
                        )
                    }
                }
        }
    }

    fun submitApplication(amount: Double, purpose: String?, branchId: Long, latitude: Double?, longitude: Double?) {
        val plafond = _uiState.value.plafond ?: return

        if (amount <= 0 || amount > plafond.maxAmount) {
             _uiState.value = _uiState.value.copy(error = "Jumlah pengajuan tidak valid (Max: ${plafond.maxAmount})")
             return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSubmitting = true, error = null)
            
            val request = ApplyCreditLineRequest(
                plafondId = plafond.id,
                branchId = branchId,
                requestedAmount = amount,
                purpose = purpose,
                latitude = latitude,
                longitude = longitude
            )
            
            creditLineRepository.applyForCreditLine(request)
                .onSuccess { creditLine ->
                    // Check if this is a pending sync (offline submission)
                    val isPending = creditLine.createdAt == "PENDING_SYNC"
                    _uiState.value = _uiState.value.copy(
                        isSubmitting = false,
                        submitSuccess = true,
                        isPendingSync = isPending,
                        submittedCreditLine = creditLine
                    )
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isSubmitting = false,
                        error = ErrorMessageMapper.parse(error)
                    )
                }
        }
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
