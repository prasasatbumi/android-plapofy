package com.finprov.plapofy.presentation.simulation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.finprov.plapofy.domain.model.LoanSimulation
import com.finprov.plapofy.domain.model.Plafond
import com.finprov.plapofy.domain.repository.LoanRepository
import com.finprov.plapofy.domain.repository.PlafondRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SimulationState(
    val isLoading: Boolean = false,
    val plafond: Plafond? = null,
    val simulationResult: LoanSimulation? = null,
    val error: String? = null
)

@HiltViewModel
class SimulationViewModel @Inject constructor(
    private val loanRepository: LoanRepository,
    private val plafondRepository: PlafondRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SimulationState())
    val uiState: StateFlow<SimulationState> = _uiState.asStateFlow()

    fun loadPlafond(plafondId: Long) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            plafondRepository.getPlafonds()
                .onSuccess { plafonds ->
                    val plafond = plafonds.find { it.id == plafondId }
                    if (plafond != null) {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            plafond = plafond
                        )
                    } else {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = "Produk tidak ditemukan"
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message ?: "Gagal memuat data produk"
                    )
                }
        }
    }

    fun simulate(plafondId: Long, amount: Double, tenor: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null, simulationResult = null)
            loanRepository.simulateLoan(plafondId, amount, tenor)
                .onSuccess { result ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        simulationResult = result
                    )
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message ?: "Simulasi gagal"
                    )
                }
        }
    }
    
    fun reset() {
        _uiState.value = SimulationState(plafond = _uiState.value.plafond)
    }
}
