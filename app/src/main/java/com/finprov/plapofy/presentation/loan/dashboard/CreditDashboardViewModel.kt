package com.finprov.plapofy.presentation.loan.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.finprov.plapofy.domain.model.CreditDisbursement
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

data class CreditDashboardState(
    val isLoading: Boolean = false,
    val creditLine: CreditLine? = null,
    val pendingDisbursements: List<PendingDisbursementEntity> = emptyList(),
    val filteredDisbursements: List<CreditDisbursement> = emptyList(),
    val statusFilter: String = "Semua", // Semua, Aktif, Lunas
    val error: String? = null,
    val paySuccessId: Long? = null
)

@HiltViewModel
class CreditDashboardViewModel @Inject constructor(
    private val creditLineRepository: CreditLineRepository,
    private val pendingDisbursementDao: PendingDisbursementDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreditDashboardState())
    val uiState: StateFlow<CreditDashboardState> = _uiState.asStateFlow()

    init {
        loadData()
        loadPendingDisbursements()
    }

    fun loadData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            creditLineRepository.getActiveCreditLine()
                .collect { creditLine ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        creditLine = creditLine,
                        filteredDisbursements = creditLine?.disbursements ?: emptyList()
                    )
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

    fun setStatusFilter(filter: String) {
        _uiState.value = _uiState.value.copy(statusFilter = filter)
        applyFilters()
    }
    
    fun setDateFilter(month: Int?, year: Int?) {
        // Implementation can be expanded for full date range picker
    }

    private fun applyFilters() {
        val currentState = _uiState.value
        val allItems = currentState.creditLine?.disbursements ?: emptyList()
        
        val filtered = allItems.filter { item ->
            val statusMatch = when (currentState.statusFilter) {
                "Semua" -> true
                "Aktif" -> item.status != "PAID_OFF"
                "Lunas" -> item.status == "PAID_OFF"
                else -> true
            }
            statusMatch
        }
        
        _uiState.value = currentState.copy(filteredDisbursements = filtered)
    }

    fun mockPayOff(disbursementId: Long) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            creditLineRepository.mockPayOff(disbursementId)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        paySuccessId = disbursementId
                    )
                    loadData()
                }
                .onFailure {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = it.message ?: "Gagal pembayaran"
                    )
                }
        }
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}

