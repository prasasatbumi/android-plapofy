package com.finprov.plapofy.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.finprov.plapofy.data.local.TokenManager
import com.finprov.plapofy.domain.model.CreditLine
import com.finprov.plapofy.domain.model.Plafond
import com.finprov.plapofy.domain.repository.CreditLineRepository
import com.finprov.plapofy.domain.repository.BranchRepository
import com.finprov.plapofy.domain.repository.ProfileRepository
import com.finprov.plapofy.domain.repository.PlafondRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.launch

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val plafondRepository: PlafondRepository,
    private val creditLineRepository: CreditLineRepository,
    private val tokenManager: TokenManager,
    private val profileRepository: ProfileRepository,
    private val branchRepository: BranchRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _activeCreditLine = MutableStateFlow<CreditLine?>(null)
    val activeCreditLine: StateFlow<CreditLine?> = _activeCreditLine.asStateFlow()

    val isLoggedIn: StateFlow<Boolean> = tokenManager.isLoggedIn
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    init {
        loadPlafonds()
        loadActiveCreditLine()
        prefetchData()
    }

    private fun prefetchData() {
        viewModelScope.launch {
             tokenManager.isLoggedIn.collect { loggedIn ->
                 if (loggedIn) {
                     // Prefetch Profile and Branches for offline cache
                     launch { profileRepository.getProfile() }
                     launch { branchRepository.getBranches() }
                 }
             }
        }
    }

    fun loadPlafonds() {
        viewModelScope.launch {
            _uiState.value = HomeUiState.Loading
            plafondRepository.getPlafonds()
                .onSuccess { plafonds ->
                    _uiState.value = HomeUiState.Success(plafonds)
                }
                .onFailure { error ->
                    _uiState.value = HomeUiState.Error(error.message ?: "Unknown error")
                }
        }
    }

    fun loadActiveCreditLine() {
        viewModelScope.launch {
            // Only load if user is logged in
            tokenManager.isLoggedIn.collect { loggedIn ->
                if (loggedIn) {
                    creditLineRepository.getMyCreditLines()
                        .collect { creditLines ->
                            // Find the first credit line that is relevant (not rejected, expired, or cancelled)
                            // Priority: ACTIVE > APPROVED > REVIEWED > APPLIED > PENDING
                            // Actually, just find the most recent one that is "in progress" or "active"
                            val current = creditLines
                                .sortedByDescending { it.id } // Assume higher ID is newer
                                .firstOrNull { 
                                    it.status != com.finprov.plapofy.domain.model.CreditLineStatus.REJECTED &&
                                    it.status != com.finprov.plapofy.domain.model.CreditLineStatus.EXPIRED
                                }
                            _activeCreditLine.value = current
                        }
                } else {
                    _activeCreditLine.value = null
                }
            }
        }
    }

    fun hasActiveCreditLine(): Boolean = _activeCreditLine.value != null
}

sealed class HomeUiState {
    object Loading : HomeUiState()
    data class Success(val plafonds: List<Plafond>) : HomeUiState()
    data class Error(val message: String) : HomeUiState()
}

