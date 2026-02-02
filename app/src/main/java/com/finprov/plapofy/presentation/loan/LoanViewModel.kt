package com.finprov.plapofy.presentation.loan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.finprov.plapofy.data.analytics.AnalyticsHelper
import com.finprov.plapofy.domain.model.Branch
import com.finprov.plapofy.domain.model.Loan
import com.finprov.plapofy.domain.model.Plafond
import com.finprov.plapofy.domain.repository.BranchRepository
import com.finprov.plapofy.domain.repository.LoanRepository
import com.finprov.plapofy.domain.repository.PlafondRepository
import com.finprov.plapofy.domain.repository.ProfileCompletenessResult
import com.finprov.plapofy.domain.repository.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ApplyLoanState(
    val isLoading: Boolean = false,
    val plafond: Plafond? = null,
    val branches: List<Branch> = emptyList(),
    val profileCheck: ProfileCompletenessResult? = null,
    val isSubmitting: Boolean = false,
    val submitSuccess: Boolean = false,
    val submittedLoan: Loan? = null,
    val error: String? = null,
    val requiresLogin: Boolean = false
)

data class MyLoansState(
    val isLoading: Boolean = false,
    val loans: List<Loan> = emptyList(),
    val error: String? = null
)

@HiltViewModel
class LoanViewModel @Inject constructor(
    private val loanRepository: LoanRepository,
    private val plafondRepository: PlafondRepository,
    private val profileRepository: ProfileRepository,
    private val branchRepository: BranchRepository
) : ViewModel() {

    private val _applyLoanState = MutableStateFlow(ApplyLoanState())
    val applyLoanState: StateFlow<ApplyLoanState> = _applyLoanState.asStateFlow()

    private val _myLoansState = MutableStateFlow(MyLoansState())
    val myLoansState: StateFlow<MyLoansState> = _myLoansState.asStateFlow()

    fun loadPlafondForApplication(plafondId: Long) {
        viewModelScope.launch {
            _applyLoanState.value = ApplyLoanState(isLoading = true)
            
            // Load branches
            val branchResult = branchRepository.getBranches()
            val branches = branchResult.getOrDefault(emptyList())

            // Load plafond details
            plafondRepository.getPlafonds()
                .onSuccess { plafonds ->
                    val plafond = plafonds.find { it.id == plafondId }
                    if (plafond != null) {
                        _applyLoanState.value = _applyLoanState.value.copy(
                            isLoading = false,
                            plafond = plafond,
                            branches = branches
                        )
                        // Check profile completeness
                        checkProfileCompleteness()
                    } else {
                        _applyLoanState.value = ApplyLoanState(
                            error = "Produk tidak ditemukan"
                        )
                    }
                }
                .onFailure { exception ->
                    _applyLoanState.value = ApplyLoanState(
                        error = exception.message ?: "Gagal memuat data produk"
                    )
                }
        }
    }

    private fun checkProfileCompleteness() {
        viewModelScope.launch {
            profileRepository.checkProfileComplete()
                .onSuccess { result ->
                    _applyLoanState.value = _applyLoanState.value.copy(
                        profileCheck = result
                    )
                }
                .onFailure { exception ->
                    // Check if it's an auth failure (401/403)
                    val isAuthError = exception.message?.contains("401") == true ||
                                      exception.message?.contains("403") == true ||
                                      exception.message?.contains("Unauthorized", ignoreCase = true) == true ||
                                      exception.message?.contains("Forbidden", ignoreCase = true) == true
                    
                    if (isAuthError) {
                        _applyLoanState.value = _applyLoanState.value.copy(
                            requiresLogin = true
                        )
                    } else {
                        // Default to incomplete if check fails
                        _applyLoanState.value = _applyLoanState.value.copy(
                            profileCheck = ProfileCompletenessResult(
                                isComplete = false,
                                missingFields = listOf(exception.message ?: "Gagal memeriksa kelengkapan profil")
                            )
                        )
                    }
                }
        }
    }

    fun submitLoan(amount: Double, tenor: Int, purpose: String?, branchId: Long, latitude: Double?, longitude: Double?) {
        val plafond = _applyLoanState.value.plafond ?: return
        
        // Validate amount
        if (amount < plafond.minAmount || amount > plafond.maxAmount) {
            _applyLoanState.value = _applyLoanState.value.copy(
                error = "Jumlah pinjaman harus antara ${formatCurrency(plafond.minAmount)} - ${formatCurrency(plafond.maxAmount)}"
            )
            return
        }

        // Validate tenor
        if (tenor < plafond.minTenor || tenor > plafond.maxTenor) {
            _applyLoanState.value = _applyLoanState.value.copy(
                error = "Tenor harus antara ${plafond.minTenor} - ${plafond.maxTenor} bulan"
            )
            return
        }

        viewModelScope.launch {
            _applyLoanState.value = _applyLoanState.value.copy(
                isSubmitting = true,
                error = null
            )

            loanRepository.submitLoan(
                plafondId = plafond.id,
                amount = amount,
                tenor = tenor,
                purpose = purpose,
                branchId = branchId,
                latitude = latitude,
                longitude = longitude
            )
                .onSuccess { loan ->
                    AnalyticsHelper.logLoanSubmit(
                        plafondId = plafond.id,
                        plafondName = plafond.name,
                        amount = amount,
                        tenor = tenor,
                        success = true
                    )
                    _applyLoanState.value = _applyLoanState.value.copy(
                        isSubmitting = false,
                        submitSuccess = true,
                        submittedLoan = loan
                    )
                }
                .onFailure { exception ->
                    AnalyticsHelper.logLoanSubmit(
                        plafondId = plafond.id,
                        plafondName = plafond.name,
                        amount = amount,
                        tenor = tenor,
                        success = false
                    )
                    _applyLoanState.value = _applyLoanState.value.copy(
                        isSubmitting = false,
                        error = exception.message ?: "Gagal mengajukan pinjaman"
                    )
                }
        }
    }

    fun loadMyLoans() {
        viewModelScope.launch {
            _myLoansState.value = MyLoansState(isLoading = true)

            loanRepository.getMyLoans()
                .onSuccess { loans ->
                    _myLoansState.value = MyLoansState(loans = loans)
                }
                .onFailure { exception ->
                    _myLoansState.value = MyLoansState(
                        error = exception.message ?: "Gagal memuat daftar pinjaman"
                    )
                }
        }
    }

    fun clearError() {
        _applyLoanState.value = _applyLoanState.value.copy(error = null)
    }

    fun resetApplyState() {
        _applyLoanState.value = ApplyLoanState()
    }

    fun finishMockLoan(loanId: Long) {
        viewModelScope.launch {
            _myLoansState.value = _myLoansState.value.copy(isLoading = true)
            loanRepository.mockFinishLoan(loanId)
                .onSuccess {
                    loadMyLoans() // Reload list
                }
                .onFailure {
                    _myLoansState.value = _myLoansState.value.copy(
                        isLoading = false,
                        error = it.message ?: "Gagal menyelesaikan pinjaman"
                    )
                }
        }
    }

    private fun formatCurrency(amount: Double): String {
        return "Rp ${String.format("%,.0f", amount)}"
    }
}
