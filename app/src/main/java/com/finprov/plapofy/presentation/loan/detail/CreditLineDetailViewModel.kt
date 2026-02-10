package com.finprov.plapofy.presentation.loan.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.finprov.plapofy.domain.model.CreditLine
import com.finprov.plapofy.domain.repository.CreditLineRepository
import com.finprov.plapofy.data.local.dao.PendingDisbursementDao
import com.finprov.plapofy.data.local.entity.PendingDisbursementEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CreditLineDetailState(
    val isLoading: Boolean = false,
    val creditLine: CreditLine? = null,
    val pendingDisbursements: List<PendingDisbursementEntity> = emptyList(),
    val error: String? = null
)

@HiltViewModel
class CreditLineDetailViewModel @Inject constructor(
    private val creditLineRepository: CreditLineRepository,
    private val pendingDisbursementDao: PendingDisbursementDao
) : ViewModel() {

    private val _state = MutableStateFlow(CreditLineDetailState())
    val state: StateFlow<CreditLineDetailState> = _state.asStateFlow()

    fun loadCreditLine(id: Long) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            
            creditLineRepository.getCreditLineById(id)
                .onSuccess { creditLine ->
                    _state.value = _state.value.copy(
                        isLoading = false,
                        creditLine = creditLine
                    )
                }
                .onFailure { exception ->
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = exception.message ?: "Gagal memuat data"
                    )
                }
        }
        
        // Load pending disbursements for this credit line
        viewModelScope.launch {
            pendingDisbursementDao.getAllPendingFlow().collect { pending ->
                val filtered = pending.filter { it.creditLineId == id }
                _state.value = _state.value.copy(pendingDisbursements = filtered)
            }
        }
    }
}
