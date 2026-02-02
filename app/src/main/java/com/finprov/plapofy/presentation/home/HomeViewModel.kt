package com.finprov.plapofy.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.finprov.plapofy.data.local.TokenManager
import com.finprov.plapofy.domain.model.Plafond
import com.finprov.plapofy.domain.repository.PlafondRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val plafondRepository: PlafondRepository,
    tokenManager: TokenManager
) : ViewModel() {

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    val isLoggedIn: StateFlow<Boolean> = tokenManager.isLoggedIn
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    init {
        loadPlafonds()
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
}

sealed class HomeUiState {
    object Loading : HomeUiState()
    data class Success(val plafonds: List<Plafond>) : HomeUiState()
    data class Error(val message: String) : HomeUiState()
}
