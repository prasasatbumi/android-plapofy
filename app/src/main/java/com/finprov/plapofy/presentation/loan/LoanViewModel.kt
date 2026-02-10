package com.finprov.plapofy.presentation.loan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.finprov.plapofy.data.analytics.AnalyticsHelper
import com.finprov.plapofy.data.local.dao.PendingLoanDao
import com.finprov.plapofy.data.local.dao.PlafondDao
import com.finprov.plapofy.data.local.entity.PendingLoanEntity
import com.finprov.plapofy.domain.model.Branch
import com.finprov.plapofy.domain.model.Loan
import com.finprov.plapofy.domain.model.LoanStatus
import com.finprov.plapofy.domain.model.Plafond
import com.finprov.plapofy.domain.repository.BranchRepository
import com.finprov.plapofy.domain.repository.CreditLineRepository
import com.finprov.plapofy.domain.repository.LoanRepository
import com.finprov.plapofy.domain.repository.PlafondRepository
import com.finprov.plapofy.domain.model.CreditLine
import com.finprov.plapofy.domain.repository.ProfileCompletenessResult
import com.finprov.plapofy.domain.repository.ProfileRepository
import com.finprov.plapofy.presentation.common.ErrorMessageMapper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
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
    val pendingCount: Int = 0,
    val failedCount: Int = 0,
    val error: String? = null
)

@HiltViewModel
class LoanViewModel @Inject constructor(
    private val loanRepository: LoanRepository,
    private val plafondRepository: PlafondRepository,
    private val profileRepository: ProfileRepository,
    private val branchRepository: BranchRepository,
    private val creditLineRepository: CreditLineRepository,
    private val pendingLoanDao: PendingLoanDao,
    private val plafondDao: PlafondDao
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
                        error = ErrorMessageMapper.parse(exception)
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
                                missingFields = listOf(ErrorMessageMapper.parse(exception))
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
                        error = ErrorMessageMapper.parse(exception)
                    )
                }
        }
    }

    fun loadMyLoans() {
        viewModelScope.launch {
            _myLoansState.value = MyLoansState(isLoading = true)

            // 1. Fetch Pending Loans from local DB (for offline display)
            val pendingLoans = try {
                pendingLoanDao.getPendingForDisplay().first()
            } catch (e: Exception) {
                emptyList()
            }
            
            val pendingLoanItems = pendingLoans.mapNotNull { mapPendingToLoan(it) }
            val pendingCount = pendingLoans.count { it.status == "PENDING" || it.status == "SENDING" }
            val failedCount = pendingLoans.count { it.status == "FAILED" }

            // 2. Fetch Loans from API
            val loanResult = loanRepository.getMyLoans()
            val loans = loanResult.getOrDefault(emptyList())

            // 3. Fetch CreditLines from API
            try {
                creditLineRepository.getMyCreditLines().collect { creditLines ->
                    val creditLineLoans = creditLines.map { mapCreditLineToLoan(it) }
                    
                    // 4. Merge and Sort: Pending first, then by date
                    val allLoans = (pendingLoanItems + loans + creditLineLoans)
                        .distinctBy { it.id } // Avoid duplicates
                        .sortedWith(compareByDescending<Loan> { 
                            // Pending items first
                            it.status == LoanStatus.PENDING 
                        }.thenByDescending { 
                            it.createdAt 
                        })
                    
                    _myLoansState.value = MyLoansState(
                        loans = allLoans,
                        pendingCount = pendingCount,
                        failedCount = failedCount
                    )
                }
            } catch (e: Exception) {
                // If credit lines fail, still show what we have
                val allLoans = (pendingLoanItems + loans)
                    .distinctBy { it.id }
                    .sortedWith(compareByDescending<Loan> { 
                        it.status == LoanStatus.PENDING 
                    }.thenByDescending { 
                        it.createdAt 
                    })
                
                _myLoansState.value = MyLoansState(
                    loans = allLoans,
                    pendingCount = pendingCount,
                    failedCount = failedCount,
                    error = if (loanResult.isFailure && loans.isEmpty() && pendingLoanItems.isEmpty()) {
                        ErrorMessageMapper.parse(loanResult.exceptionOrNull())
                    } else null
                )
            }
        }
    }
    
    private suspend fun mapPendingToLoan(pending: PendingLoanEntity): Loan? {
        // Get plafond name from local cache
        val plafondName = try {
            plafondDao.getPlafondById(pending.plafondId)?.name ?: "Produk Pinjaman"
        } catch (e: Exception) {
            "Produk Pinjaman"
        }
        
        // Map status to display status
        val displayStatus = when (pending.status) {
            "PENDING", "SENDING" -> LoanStatus.PENDING
            "FAILED" -> LoanStatus.CANCELLED // Show failed as cancelled
            else -> LoanStatus.PENDING
        }
        
        // Create purpose text based on status
        val purposeText = when (pending.status) {
            "PENDING" -> "⏳ Menunggu koneksi..."
            "SENDING" -> "⏳ Sedang mengirim..."
            "FAILED" -> "❌ ${pending.errorMessage ?: "Gagal kirim"}"
            else -> pending.purpose ?: ""
        }
        
        return Loan(
            id = pending.id.hashCode().toLong(), // Use hash as unique ID
            userId = 0,
            plafondId = pending.plafondId,
            plafondName = if (pending.type == "CREDIT_LINE") "Limit Kredit - $plafondName" else plafondName,
            amount = pending.amount,
            tenor = pending.tenor,
            interestRate = 0.0,
            monthlyInstallment = 0.0,
            totalPayment = 0.0,
            status = displayStatus,
            branchName = null,
            purpose = purposeText,
            createdAt = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.getDefault())
                .format(java.util.Date(pending.timestamp)),
            approvedAt = null,
            disbursedAt = null,
            approvals = emptyList()
        )
    }

    private fun mapCreditLineToLoan(creditLine: CreditLine): Loan {
        // Map Status
        val loanStatus = when (creditLine.status.name) {
            "APPLIED" -> com.finprov.plapofy.domain.model.LoanStatus.SUBMITTED
            "REVIEWED" -> com.finprov.plapofy.domain.model.LoanStatus.REVIEWED
            "APPROVED" -> com.finprov.plapofy.domain.model.LoanStatus.APPROVED
            "ACTIVE" -> com.finprov.plapofy.domain.model.LoanStatus.APPROVED
            "REJECTED" -> com.finprov.plapofy.domain.model.LoanStatus.REJECTED
            "EXPIRED" -> com.finprov.plapofy.domain.model.LoanStatus.CANCELLED
            else -> com.finprov.plapofy.domain.model.LoanStatus.PENDING
        }

        // Determine amount to show (Approved if available, otherwise Requested)
        val amount = if (creditLine.approvedLimit > 0) creditLine.approvedLimit else creditLine.requestedAmount

        return Loan(
            // Use negative ID to distinguish CreditLine from Loan
            id = -creditLine.id, 
            userId = 0, // Not needed for list
            plafondId = 0, // Not needed
            plafondName = creditLine.plafondName ?: "Credit Line",
            amount = amount,
            tenor = 0, // Credit Line doesn't have fixed tenor until disbursement
            interestRate = creditLine.interestRate,
            monthlyInstallment = 0.0,
            totalPayment = 0.0,
            status = loanStatus,
            branchName = null,
            purpose = "Pengajuan Limit Kredit",
            createdAt = creditLine.createdAt ?: creditLine.validUntil, // Fallback
            approvedAt = null,
            disbursedAt = null,
            approvals = emptyList()
        )
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
                        error = ErrorMessageMapper.parse(it)
                    )
                }
        }
    }

    fun resetPlafond() {
        viewModelScope.launch {
            _myLoansState.value = _myLoansState.value.copy(isLoading = true)
            creditLineRepository.mockResetCreditLines()
                .onSuccess {
                    _myLoansState.value = _myLoansState.value.copy(isLoading = false)
                    // Optionally reload loans or show success
                }
                .onFailure {
                    _myLoansState.value = _myLoansState.value.copy(
                        isLoading = false,
                        error = ErrorMessageMapper.parse(it)
                    )
                }
        }
    }

    private fun formatCurrency(amount: Double): String {
        return "Rp ${String.format("%,.0f", amount)}"
    }
}
