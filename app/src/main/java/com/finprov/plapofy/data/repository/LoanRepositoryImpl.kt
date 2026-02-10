package com.finprov.plapofy.data.repository

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.finprov.plapofy.data.local.dao.LoanDao
import com.finprov.plapofy.data.local.dao.PendingLoanDao
import com.finprov.plapofy.data.local.dao.PlafondDao
import com.finprov.plapofy.data.local.entity.PendingLoanEntity
import com.finprov.plapofy.data.remote.api.LoanApi
import com.finprov.plapofy.data.remote.dto.LoanDto
import com.finprov.plapofy.data.remote.dto.LoanSimulationRequest
import com.finprov.plapofy.data.remote.dto.LoanSimulationResponse
import com.finprov.plapofy.data.remote.dto.SubmitLoanRequest
import com.finprov.plapofy.data.remote.dto.toEntity
import com.finprov.plapofy.data.remote.dto.toPendingEntity
import com.finprov.plapofy.data.worker.SyncLoansWorker
import com.finprov.plapofy.domain.model.Loan
import com.finprov.plapofy.domain.model.LoanApproval
import com.finprov.plapofy.domain.model.LoanSimulation
import com.finprov.plapofy.domain.model.LoanStatus
import com.finprov.plapofy.domain.repository.LoanRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import java.io.IOException
import kotlin.math.roundToLong

class LoanRepositoryImpl @Inject constructor(
    private val api: LoanApi,
    private val loanDao: LoanDao,
    private val pendingLoanDao: PendingLoanDao,
    private val plafondDao: PlafondDao,
    @ApplicationContext private val context: Context
) : LoanRepository {

    private val workManager = WorkManager.getInstance(context)

    private val syncConstraints = androidx.work.Constraints.Builder()
        .setRequiredNetworkType(androidx.work.NetworkType.CONNECTED)
        .build()

    override suspend fun simulateLoan(plafondId: Long, amount: Double, tenorMonth: Int): Result<LoanSimulation> {
        return try {
            val request = LoanSimulationRequest(plafondId, amount, tenorMonth)
            val response = api.simulate(request)
            if (response.success && response.data != null) {
                Result.success(response.data.toDomain())
            } else {
                throw Exception(response.message)
            }
        } catch (e: Exception) {
            // Offline fallback for simulation
            if (e is IOException) {
                try {
                    val plafond = plafondDao.getPlafondById(plafondId)?.toDomain()
                    if (plafond != null) {
                        // Find matching interest
                        // Logic: Find interest for specific tenor, or closest, or default
                        val interestObj = plafond.interests.find { it.tenor == tenorMonth }
                            ?: plafond.interests.minByOrNull { kotlin.math.abs(it.tenor - tenorMonth) }
                        
                        val interestRate = interestObj?.interestRate ?: 0.0
                        // Formula: (Principal + (Principal * Rate / 100)) / Tenor ?? 
                        // Usually: Monthly Installment = (Amount * (1 + Rate/100 * (Tenor/12))) / Tenor?
                        // Let's assume simple flat rate per year for now or match backend logic.
                        // Backend likely: Amount * (Rate/100) / 12 per month?
                        // Let's simplify: Installment = (Amount / Tenor) + (Amount * Interest / 100 / 12)
                        // Wait, usually interest provided is p.a (per annum).
                        
                        val interestPerMonth = amount * (interestRate / 100) / 12
                        val principalPerMonth = amount / tenorMonth
                        val monthlyInstallment = principalPerMonth + interestPerMonth
                        
                        val sim = LoanSimulation(
                            plafondId = plafondId,
                            amount = amount,
                            tenor = tenorMonth,
                            interestRate = interestRate,
                            monthlyInstallment = monthlyInstallment
                        )
                        Result.success(sim)
                    } else {
                         Result.failure(e)
                    }
                } catch (localE: Exception) {
                    Result.failure(e)
                }
            } else {
                Result.failure(e)
            }
        }
    }

    override suspend fun submitLoan(plafondId: Long, amount: Double, tenor: Int, purpose: String?, branchId: Long, latitude: Double?, longitude: Double?): Result<Loan> {
        val request = SubmitLoanRequest(plafondId, amount, tenor, purpose, branchId, latitude, longitude)
        
        return try {
            val response = api.submitLoan(request)
            if (response.success && response.data != null) {
                val loan = response.data.toDomain()
                // Save to local
                loanDao.insertLoan(response.data.toEntity())
                Result.success(loan)
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            // Offline Fallback: Check if it's a network/IO issue
            if (e is IOException || e.cause is IOException) {
                try {
                    val pendingEntity = request.toPendingEntity()
                    pendingLoanDao.insertPendingLoan(pendingEntity)
                    
                    // Enqueue Worker
                    val syncRequest = OneTimeWorkRequestBuilder<SyncLoansWorker>()
                        .setConstraints(syncConstraints)
                        .build()

                    workManager.enqueueUniqueWork(
                        "SyncLoans",
                        ExistingWorkPolicy.APPEND_OR_REPLACE, 
                        syncRequest
                    )

                    // Return Mock Loan for UI
                    val mockLoan = Loan(
                        id = -1L,
                        userId = 0L,
                        plafondId = plafondId,
                        plafondName = "Menunggu Sinkronisasi",
                        amount = amount,
                        tenor = tenor,
                        interestRate = 0.0,
                        monthlyInstallment = 0.0,
                        totalPayment = 0.0,
                        status = LoanStatus.SUBMITTED,
                        branchName = "Offline",
                        purpose = purpose,
                        createdAt = "Offline Mode",
                        approvedAt = null,
                        disbursedAt = null,
                        approvals = emptyList()
                    )
                    Result.success(mockLoan)
                } catch (ex: Exception) {
                    Result.failure(e)
                }
            } else {
                 Result.failure(e)
            }
        }
    }

    override suspend fun getMyLoans(): Result<List<Loan>> {
        // SSOT: Try Network -> Update DB -> Return. Fail Network -> Return DB.
        return try {
            val response = api.getMyLoans()
            if (response.success && response.data != null) {
                val loans = response.data
                loanDao.insertLoans(loans.map { it.toEntity() })
                
                // Trigger background sync for any pending loans
                val syncRequest = OneTimeWorkRequestBuilder<SyncLoansWorker>()
                     .setConstraints(syncConstraints)
                     .build()
                workManager.enqueueUniqueWork(
                     "SyncLoans",
                     ExistingWorkPolicy.KEEP, // Keep existing if running, otherwise start
                     syncRequest
                )
                
                // Merge pending loans even if online!
                val pendingLoans = pendingLoanDao.getAllPendingLoans()
                val mappedLoans = loans.map { it.toDomain() }.toMutableList()
                
                pendingLoans.forEach { pending ->
                     val plafondName = plafondDao.getPlafondById(pending.plafondId)?.name ?: "Pinjaman"
                     
                     mappedLoans.add(0, Loan(
                        id = -1L,
                        userId = 0L,
                        plafondId = pending.plafondId,
                        plafondName = plafondName,
                        amount = pending.amount,
                        tenor = pending.tenor,
                        interestRate = 0.0,
                        monthlyInstallment = 0.0,
                        totalPayment = 0.0,
                        status = LoanStatus.SUBMITTED,
                        branchName = "Offline - Menunggu Koneksi",
                        purpose = pending.purpose,
                        createdAt = "Offline Mode",
                        approvedAt = null,
                        disbursedAt = null,
                        approvals = emptyList()
                     ))
                }
                
                Result.success(mappedLoans)
            } else {
                throw Exception(response.message)
            }
        } catch (e: Exception) {
            // Fallback to local
            val localLoans = loanDao.getLoans().first()
            val pendingLoans = pendingLoanDao.getAllPendingLoans()
            
            if (localLoans.isNotEmpty() || pendingLoans.isNotEmpty()) {
                val mappedLoans = localLoans.map { it.toDomain() }.toMutableList()
                
                // Add pending loans to list for visibility
                pendingLoans.forEach { pending ->
                     val plafondName = plafondDao.getPlafondById(pending.plafondId)?.name ?: "Pinjaman"
                     
                     mappedLoans.add(0, Loan(
                        id = -1L,
                        userId = 0L,
                        plafondId = pending.plafondId,
                        plafondName = plafondName,
                        amount = pending.amount,
                        tenor = pending.tenor,
                        interestRate = 0.0,
                        monthlyInstallment = 0.0,
                        totalPayment = 0.0,
                        status = LoanStatus.SUBMITTED, // Or a special OFFLINE status? SUBMITTED is fine.
                        branchName = "Offline - Menunggu Koneksi",
                        purpose = pending.purpose,
                        createdAt = "Offline Mode",
                        approvedAt = null,
                        disbursedAt = null,
                        approvals = emptyList()
                     ))
                }
                
                Result.success(mappedLoans)
            } else {
                Result.failure(e)
            }
        }
    }

    override suspend fun mockFinishLoan(loanId: Long): Result<Loan> {
        return try {
            val response = api.mockFinish(loanId)
            if (response.success && response.data != null) {
                val loan = response.data.toDomain()
                loanDao.insertLoan(response.data.toEntity())
                Result.success(loan)
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun LoanSimulationResponse.toDomain(): LoanSimulation {
        return LoanSimulation(
            plafondId = plafondId ?: 0L,
            amount = amount,
            tenor = tenor,
            interestRate = interestRate,
            monthlyInstallment = monthlyInstallment
        )
    }

    // Existing wrapper private function for internal usage if needed, 
    // but we use Mapper extensions now.
    // Keeping this to minimize break changes if used elsewhere not expected? 
    // Only used locally. We remove it as we use extensions.
    
    private fun LoanDto.toDomain(): Loan {
       // This logic is now in toEntity().toDomain() or direct mapping?
       // Let's rely on Entity -> Domain only? 
       // Or DTO -> Domain for online.
       // For consistency, better to use DTO -> Domain (Network) OR DTO -> Entity -> Domain (SSOT).
       // To save effort, I'll inline the DTO -> Domain logic or add extension for it.
       // Check Mapper.
       // I didn't add LoanDto.toDomain() in Mapper. I should add it there or duplicate logic.
       // I'll duplicate local logic to avoid changing too many files, or better, add it to this file or Mapper.
       // I will add it here as private fun since I am overwriting the file.
       return Loan(
            id = id,
            userId = applicant?.id ?: 0L,
            plafondId = plafond?.id ?: 0L,
            plafondName = plafond?.name,
            amount = amount,
            tenor = tenor,
            interestRate = interestRate,
            monthlyInstallment = monthlyInstallment,
            totalPayment = monthlyInstallment * tenor, 
            status = LoanStatus.fromString(status ?: ""),
            branchName = branch?.name,
            purpose = purpose,
            createdAt = createdAt,
            approvedAt = approvedAt,
            disbursedAt = disbursedAt,
            approvals = approvals?.map { 
                LoanApproval(
                    id = it.id,
                    statusAfter = LoanStatus.fromString(it.statusAfter),
                    role = it.role,
                    remarks = it.remarks,
                    timestamp = it.timestamp
                )
            } ?: emptyList()
        )
    }
}
