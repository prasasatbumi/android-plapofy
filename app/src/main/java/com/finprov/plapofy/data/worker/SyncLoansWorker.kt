package com.finprov.plapofy.data.worker

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.finprov.plapofy.data.local.dao.LoanDao
import com.finprov.plapofy.data.local.dao.PendingLoanDao
import com.finprov.plapofy.data.remote.api.CreditLineApi
import com.finprov.plapofy.data.remote.api.LoanApi
import com.finprov.plapofy.data.remote.dto.ApplyCreditLineRequest
import com.finprov.plapofy.data.remote.dto.SubmitLoanRequest
import com.finprov.plapofy.data.remote.dto.toEntity
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException

private const val TAG = "SyncLoansWorker"

@HiltWorker
class SyncLoansWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val pendingLoanDao: PendingLoanDao,
    private val loanDao: LoanDao,
    private val loanApi: LoanApi,
    private val creditLineApi: CreditLineApi
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        Log.d(TAG, "Starting sync work...")
        
        try {
            // Get pending items (status = PENDING), latest first
            val pendingLoans = pendingLoanDao.getPendingForSync()
            
            if (pendingLoans.isEmpty()) {
                Log.d(TAG, "No pending loans to sync")
                return@withContext Result.success()
            }

            var hasNetworkError = false

            for (loan in pendingLoans) {
                // Update status to SENDING
                pendingLoanDao.updateStatus(
                    id = loan.id,
                    status = "SENDING",
                    retryCount = loan.retryCount + 1,
                    lastAttemptAt = System.currentTimeMillis(),
                    errorMessage = null
                )

                try {
                    val success = if (loan.type == "CREDIT_LINE") {
                        submitCreditLine(loan)
                    } else {
                        submitLoan(loan)
                    }

                    if (success) {
                        Log.d(TAG, "Successfully synced ${loan.id}")
                        // Delete from pending after successful sync
                        pendingLoanDao.deleteById(loan.id)
                    } else {
                        // Server returned success=false (Logic Error) -> Permanent Fail
                        Log.w(TAG, "Server rejected ${loan.id}")
                        pendingLoanDao.updateStatus(
                            id = loan.id,
                            status = "FAILED",
                            retryCount = loan.retryCount + 1,
                            lastAttemptAt = System.currentTimeMillis(),
                            errorMessage = "Server rejected the request"
                        )
                    }
                } catch (e: Exception) {
                    if (e is IOException) {
                        // Network error -> Retry later via WorkManager
                        Log.e(TAG, "Network error syncing ${loan.id}", e)
                        hasNetworkError = true
                        
                        // Reset to PENDING so it's picked up next time
                        pendingLoanDao.updateStatus(
                            id = loan.id,
                            status = "PENDING",
                            retryCount = loan.retryCount,
                            lastAttemptAt = System.currentTimeMillis(),
                            errorMessage = e.message
                        )
                    } else {
                        // Other error (Parsing, Crash) -> Permanent Fail
                        Log.e(TAG, "Fatal error syncing ${loan.id}", e)
                        pendingLoanDao.updateStatus(
                            id = loan.id,
                            status = "FAILED",
                            retryCount = loan.retryCount + 1,
                            lastAttemptAt = System.currentTimeMillis(),
                            errorMessage = e.message ?: "Unknown error"
                        )
                    }
                }
            }

            // After syncing, refresh loan list from server
            if (!hasNetworkError) {
                syncLoansFromServer()
            }

            if (hasNetworkError) {
                Result.retry()
            } else {
                Result.success()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Worker failed", e)
            Result.retry()
        }
    }

    private suspend fun submitCreditLine(loan: com.finprov.plapofy.data.local.entity.PendingLoanEntity): Boolean {
        val request = ApplyCreditLineRequest(
            plafondId = loan.plafondId,
            requestedAmount = loan.amount,
            branchId = loan.branchId,
            purpose = loan.purpose,
            latitude = loan.latitude,
            longitude = loan.longitude
        )
        val response = creditLineApi.applyForCreditLine(request)
        return response.success
    }

    private suspend fun submitLoan(loan: com.finprov.plapofy.data.local.entity.PendingLoanEntity): Boolean {
        val request = SubmitLoanRequest(
            plafondId = loan.plafondId,
            amount = loan.amount,
            tenor = loan.tenor,
            purpose = loan.purpose,
            branchId = loan.branchId,
            latitude = loan.latitude,
            longitude = loan.longitude
        )
        val response = loanApi.submitLoan(request)
        if (response.success && response.data != null) {
            // Save to local DB
            loanDao.insertLoan(response.data.toEntity())
        }
        return response.success
    }

    private suspend fun syncLoansFromServer() {
        try {
            val loansResponse = loanApi.getMyLoans()
            if (loansResponse.success && loansResponse.data != null) {
                loanDao.insertLoans(loansResponse.data.map { it.toEntity() })
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to sync loans from server", e)
        }
    }
}
