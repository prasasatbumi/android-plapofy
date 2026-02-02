package com.finprov.plapofy.data.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.finprov.plapofy.data.local.dao.LoanDao
import com.finprov.plapofy.data.local.dao.PendingLoanDao
import com.finprov.plapofy.data.local.entity.toEntity
import com.finprov.plapofy.data.remote.api.LoanApi
import com.finprov.plapofy.data.remote.dto.SubmitLoanRequest
import com.finprov.plapofy.data.remote.dto.toEntity
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@HiltWorker
class SyncLoansWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val pendingLoanDao: PendingLoanDao,
    private val loanDao: LoanDao,
    private val loanApi: LoanApi
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        android.util.Log.d("SyncLoansWorker", "Starting sync work...")
        try {
            val pendingLoans = pendingLoanDao.getAllPendingLoans()
            
            if (pendingLoans.isEmpty()) {
                // No pending loans, but continue to sync remote loans
            }

            var successCount = 0
            
            for (loan in pendingLoans) {
                try {
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
                        // Insert the new loan into local cache
                        response.data.let { dto ->
                            loanDao.insertLoan(dto.toEntity()) 
                        }
                        
                        // Remove from pending
                        pendingLoanDao.deletePendingLoan(loan)
                        successCount++
                    } else {
                        // Logic for failure (success = false)
                        // Assuming valid response from server but business error (e.g. validation)
                        // allowing us to discard the pending submission.
                        pendingLoanDao.deletePendingLoan(loan)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    // Continue to next loan
                }
            }

            // Sync latest loans from server to keep UI fresh
            try {
                val loansResponse = loanApi.getMyLoans()
                if (loansResponse.success && loansResponse.data != null) {
                     loansResponse.data.let { dtos ->
                         loanDao.insertLoans(dtos.map { it.toEntity() })
                     }
                }
            } catch (e: Exception) {
                // Ignore failure to fetch list, at least we pushed data
            }

            if (pendingLoans.isEmpty()) {
                // No pending loans to submit, but proceed to sync latest loans
                Result.success() // If no pending loans, and we reached here, it's a success for submission part
            } else if (successCount == pendingLoans.size) {
                Result.success()
            } else {
                Result.retry()
            }
        } catch (e: Exception) {
            Result.failure()
        }
    }
}
