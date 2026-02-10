package com.finprov.plapofy.data.worker

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.finprov.plapofy.data.local.dao.PendingDisbursementDao
import com.finprov.plapofy.data.remote.api.CreditLineApi
import com.finprov.plapofy.data.remote.dto.DisburseRequest
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException

private const val TAG = "SyncDisbursementsWorker"

@HiltWorker
class SyncDisbursementsWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val pendingDao: PendingDisbursementDao,
    private val creditLineApi: CreditLineApi
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        Log.d(TAG, "Starting disbursement sync...")
        try {
            val pendings = pendingDao.getPendingForSync()
            if (pendings.isEmpty()) {
                Log.d(TAG, "No pending disbursements")
                return@withContext Result.success()
            }

            var hasNetworkError = false

            for (item in pendings) {
                // Mark as SENDING
                pendingDao.updateStatus(
                    id = item.id,
                    status = "SENDING",
                    retryCount = item.retryCount + 1,
                    lastAttemptAt = System.currentTimeMillis(),
                    errorMessage = null
                )

                try {
                    val response = creditLineApi.disburse(
                        item.creditLineId,
                        DisburseRequest(item.amount, item.tenor)
                    )
                    
                    if (response.success) {
                        pendingDao.deleteById(item.id)
                        Log.d(TAG, "Disbursement ${item.id} synced")
                    } else {
                        // Server logic rejection -> Permanent Fail
                        Log.w(TAG, "Server rejected ${item.id}: ${response.message}")
                        pendingDao.updateStatus(
                            id = item.id,
                            status = "FAILED",
                            retryCount = item.retryCount + 1,
                            lastAttemptAt = System.currentTimeMillis(),
                            errorMessage = response.message ?: "Server rejected"
                        )
                    }
                } catch (e: Exception) {
                    if (e is IOException) {
                        // Network error -> Retry later
                        Log.e(TAG, "Network error syncing ${item.id}", e)
                        hasNetworkError = true
                        
                        // Reset to PENDING
                        pendingDao.updateStatus(
                            id = item.id,
                            status = "PENDING",
                            retryCount = item.retryCount,
                            lastAttemptAt = System.currentTimeMillis(),
                            errorMessage = e.message
                        )
                    } else {
                        // Fatal error -> Permanent Fail
                        Log.e(TAG, "Fatal error syncing ${item.id}", e)
                        pendingDao.updateStatus(
                            id = item.id,
                            status = "FAILED",
                            retryCount = item.retryCount + 1,
                            lastAttemptAt = System.currentTimeMillis(),
                            errorMessage = e.message ?: "Unknown error"
                        )
                    }
                }
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
}
