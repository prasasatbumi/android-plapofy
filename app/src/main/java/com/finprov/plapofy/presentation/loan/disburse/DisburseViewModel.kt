package com.finprov.plapofy.presentation.loan.disburse

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.finprov.plapofy.domain.model.CreditDisbursement
import com.finprov.plapofy.domain.model.CreditLine
import com.finprov.plapofy.domain.repository.CreditLineRepository
import com.finprov.plapofy.data.local.dao.PendingDisbursementDao
import com.finprov.plapofy.data.local.entity.PendingDisbursementEntity
import com.finprov.plapofy.data.remote.dto.DisburseRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DisburseState(
    val isLoading: Boolean = false,
    val creditLine: CreditLine? = null,
    val pendingDisbursements: List<PendingDisbursementEntity> = emptyList(),
    val isSubmitting: Boolean = false,
    val success: Boolean = false,
    val isPendingSync: Boolean = false,
    val disbursement: CreditDisbursement? = null,
    val error: String? = null
)

@HiltViewModel
class DisburseViewModel @Inject constructor(
    private val creditLineRepository: CreditLineRepository,
    private val pendingDisbursementDao: PendingDisbursementDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(DisburseState())
    val uiState: StateFlow<DisburseState> = _uiState.asStateFlow()

    init {
        loadData()
        loadPendingDisbursements()
    }

    fun loadData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            creditLineRepository.getActiveCreditLine()
                .collect { creditLine ->
                    if (creditLine != null) {
                         _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            creditLine = creditLine
                        )
                    } else {
                         _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = "Tidak ada kredit aktif"
                        )
                    }
                }
        }
    }

    private fun loadPendingDisbursements() {
        viewModelScope.launch {
            pendingDisbursementDao.getAllPendingFlow().collect { pending ->
                _uiState.value = _uiState.value.copy(pendingDisbursements = pending)
            }
        }
    }

    fun submitDisbursement(amount: Double, tenor: Int) {
        val creditLine = _uiState.value.creditLine ?: return
        
        if (amount > creditLine.availableBalance) {
            _uiState.value = _uiState.value.copy(error = "Saldo tidak mencukupi")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSubmitting = true, error = null)
            
            creditLineRepository.disburse(creditLine.id, DisburseRequest(amount, tenor))
                .onSuccess { disbursement ->
                    val isPending = disbursement.status == "PENDING_SYNC"
                    _uiState.value = _uiState.value.copy(
                        isSubmitting = false,
                        success = true,
                        isPendingSync = isPending,
                        disbursement = disbursement,
                        creditLine = if (isPending) {
                            creditLine.copy(availableBalance = kotlin.math.max(0.0, creditLine.availableBalance - amount))
                        } else creditLine
                    )
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isSubmitting = false,
                        error = error.message ?: "Gagal mencairkan dana"
                    )
                }
        }
    }
    
    fun clearError() {
         _uiState.value = _uiState.value.copy(error = null)
    }
}
