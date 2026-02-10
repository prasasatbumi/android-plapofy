package com.finprov.plapofy.data.repository

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.finprov.plapofy.data.local.dao.PendingLoanDao
import com.finprov.plapofy.data.local.dao.PendingDisbursementDao
import com.finprov.plapofy.data.local.dao.PlafondDao
import com.finprov.plapofy.data.local.dao.CreditLineDao
import com.finprov.plapofy.data.local.entity.PendingLoanEntity
import com.finprov.plapofy.data.local.entity.PendingDisbursementEntity
import com.finprov.plapofy.data.local.entity.toEntity
import kotlinx.coroutines.flow.first
import com.finprov.plapofy.data.remote.api.CreditLineApi
import com.finprov.plapofy.data.remote.dto.ApplyCreditLineRequest
import com.finprov.plapofy.data.remote.dto.DisburseRequest
import com.finprov.plapofy.data.remote.dto.toDomain
import com.finprov.plapofy.data.worker.SyncLoansWorker
import com.finprov.plapofy.data.worker.SyncDisbursementsWorker
import com.finprov.plapofy.domain.model.CreditDisbursement
import com.finprov.plapofy.domain.model.CreditLine
import com.finprov.plapofy.domain.model.CreditLineStatus
import com.finprov.plapofy.domain.repository.CreditLineRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.io.IOException
import javax.inject.Inject

class CreditLineRepositoryImpl @Inject constructor(
    private val api: CreditLineApi,
    private val pendingLoanDao: PendingLoanDao,
    private val pendingDisbursementDao: PendingDisbursementDao,
    private val plafondDao: PlafondDao,
    private val creditLineDao: CreditLineDao,
    @ApplicationContext private val context: Context
) : CreditLineRepository {

    private val workManager = WorkManager.getInstance(context)

    private val syncConstraints = androidx.work.Constraints.Builder()
        .setRequiredNetworkType(androidx.work.NetworkType.CONNECTED)
        .build()

    override fun getMyCreditLines(): Flow<List<CreditLine>> = flow {
        // 1. Emit Local Cache Immediately
        val localData = creditLineDao.getAllCreditLines().first()
        emit(localData.map { it.toDomain() })

        // 2. Fetch from API and Update Cache
        try {
            val response = api.getMyCreditLines()
            if (response.success && response.data != null) {
                val entities = response.data.map { it.toDomain().toEntity() }
                creditLineDao.deleteAll()
                creditLineDao.insertAll(entities)
                emit(response.data.map { it.toDomain() })
            }
        } catch (e: Exception) {
            // Error fetching remote, keep emitting local (already done)
             if (localData.isEmpty()) {
                emit(emptyList())
            }
        }
    }

    override fun getActiveCreditLine(): Flow<CreditLine?> = flow {
        // 1. Emit Local Cache
        val local = creditLineDao.getActiveCreditLine().first()
        emit(local?.toDomain())

        // 2. Fetch Remote
         try {
            val response = api.getActiveCreditLine()
            if (response.success && response.data != null) {
                // We don't necessarily save solitary active credit line to a separate table, 
                // but since we update the whole list in getMyCreditLines, this might be fine.
                // Or we can query the list again effectively.
                // Ideally we should upsert this specific one.
                val entity = response.data.toDomain().toEntity()
                creditLineDao.insert(entity)
                emit(response.data.toDomain())
            } else {
                 emit(null)
            }
        } catch (e: Exception) {
            // Keep local
        }
    }

    override suspend fun applyForCreditLine(request: ApplyCreditLineRequest): Result<CreditLine> {
        return try {
            val response = api.applyForCreditLine(request)
            if (response.success && response.data != null) {
                val domain = response.data.toDomain()
                creditLineDao.insert(domain.toEntity()) // Cache it
                Result.success(domain)
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            // Offline Fallback
            if (e is IOException) {
                try {
                    // Check for duplicate pending credit line application
                    val pendingCount = pendingLoanDao.countPendingByType("CREDIT_LINE")
                    if (pendingCount > 0) {
                        return Result.failure(Exception("DUPLICATE_PENDING:Anda sudah memiliki pengajuan limit yang menunggu dikirim"))
                    }

                    // Save to Pending DB
                    val pendingEntity = PendingLoanEntity(
                        plafondId = request.plafondId,
                        amount = request.requestedAmount,
                        tenor = 0, 
                        purpose = request.purpose,
                        branchId = request.branchId,
                        latitude = request.latitude,
                        longitude = request.longitude,
                        type = "CREDIT_LINE"
                    )
                    pendingLoanDao.insertPendingLoan(pendingEntity)
                    
                    // Schedule Sync
                    val syncRequest = OneTimeWorkRequestBuilder<SyncLoansWorker>()
                        .setConstraints(syncConstraints)
                        .build()

                    workManager.enqueueUniqueWork(
                        "SyncLoans",
                        ExistingWorkPolicy.APPEND_OR_REPLACE, 
                        syncRequest
                    )
                    
                    // Return Mock Credit Line for UI with PENDING_SYNC status
                    val plafond = try {
                        plafondDao.getPlafondById(request.plafondId)
                    } catch (ex: Exception) { null }

                    val mockCreditLine = CreditLine(
                         id = -1L,
                         plafondName = plafond?.name ?: "Limit Kredit",
                         plafondMaxAmount = plafond?.maxAmount ?: request.requestedAmount,
                         tier = "STARTER",
                         requestedAmount = request.requestedAmount,
                         approvedLimit = 0.0,
                         availableBalance = 0.0,
                         interestRate = 0.0,
                         status = CreditLineStatus.APPLIED, 
                         validUntil = null,
                         createdAt = "PENDING_SYNC",
                         disbursements = emptyList(),
                         history = emptyList(),
                         branchName = "Menunggu Kirim"
                    )
                    Result.success(mockCreditLine)
                } catch (localEx: Exception) {
                    Result.failure(e)
                }
            } else {
                Result.failure(e)
            }
        }
    }

    override suspend fun getCreditLineById(id: Long): Result<CreditLine> {
        // Try local first
        try {
            val local = creditLineDao.getCreditLineById(id)
            if (local != null) {
                return Result.success(local.toDomain())
            }
        } catch (e: Exception) { /* ignore */ }

        return try {
            val response = api.getCreditLineById(id)
            if (response.success && response.data != null) {
                 val domain = response.data.toDomain()
                 creditLineDao.insert(domain.toEntity())
                Result.success(domain)
            } else {
                Result.failure(Exception(response.message ?: "Credit line not found"))
            }
        } catch (e: Exception) {
             if (id == -1L) {
                  Result.failure(Exception("Offline credit line details not available"))
             } else {
                  Result.failure(e)
             }
        }
    }

    override suspend fun disburse(creditLineId: Long, request: DisburseRequest): Result<CreditDisbursement> {
        return try {
            val response = api.disburse(creditLineId, request)
            if (response.success && response.data != null) {
                 // Refresh credit lines to update balance/disbursements
                 try {
                     val updatedLines = api.getMyCreditLines()
                     if (updatedLines.success && updatedLines.data != null) {
                         creditLineDao.deleteAll()
                         creditLineDao.insertAll(updatedLines.data.map { it.toDomain().toEntity() })
                     }
                 } catch (e: Exception) { /* best effort */ }
                 
                Result.success(response.data.toDomain())
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            if (e is IOException) {
                try {
                    // Save pending disbursement - multiple allowed as long as balance sufficient
                    val pending = PendingDisbursementEntity(
                        creditLineId = creditLineId,
                        amount = request.amount,
                        tenor = request.tenor
                    )
                    pendingDisbursementDao.insert(pending)

                    // Deduct local balance
                    val currentCreditLine = creditLineDao.getCreditLineById(creditLineId)
                    if (currentCreditLine != null) {
                        val updatedBalance = kotlin.math.max(0.0, currentCreditLine.availableBalance - request.amount)
                        creditLineDao.insert(currentCreditLine.copy(availableBalance = updatedBalance))
                    }

                    val syncRequest = OneTimeWorkRequestBuilder<SyncDisbursementsWorker>()
                        .setConstraints(syncConstraints)
                        .build()

                    workManager.enqueueUniqueWork(
                        "SyncDisbursements",
                        ExistingWorkPolicy.APPEND_OR_REPLACE,
                        syncRequest
                    )

                    // Return with PENDING_SYNC status to indicate offline submission
                    val mock = CreditDisbursement(
                        id = -1L,
                        amount = request.amount,
                        tenor = request.tenor,
                        monthlyInstallment = 0.0,
                        disbursedAt = "PENDING_SYNC",
                        status = "PENDING_SYNC"
                    )
                    Result.success(mock)
                } catch (localEx: Exception) {
                    Result.failure(e)
                }
            } else {
                Result.failure(e)
            }
        }
    }

    override suspend fun mockPayOff(disbursementId: Long): Result<CreditDisbursement> {
         return try {
            val response = api.mockPayOff(disbursementId)
            if (response.success && response.data != null) {
                Result.success(response.data.toDomain())
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun mockResetCreditLines(): Result<Unit> {
        return try {
            val response = api.mockResetCreditLines()
            if (response.success) {
                creditLineDao.deleteAll() // Clear local cache
                Result.success(Unit)
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
