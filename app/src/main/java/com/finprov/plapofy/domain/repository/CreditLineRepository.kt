package com.finprov.plapofy.domain.repository

import com.finprov.plapofy.domain.model.CreditLine
import com.finprov.plapofy.domain.model.CreditDisbursement
import com.finprov.plapofy.data.remote.dto.ApplyCreditLineRequest
import com.finprov.plapofy.data.remote.dto.DisburseRequest
import kotlinx.coroutines.flow.Flow

interface CreditLineRepository {
    fun getMyCreditLines(): Flow<List<CreditLine>>
    fun getActiveCreditLine(): Flow<CreditLine?>
    suspend fun getCreditLineById(id: Long): Result<CreditLine>
    suspend fun applyForCreditLine(request: ApplyCreditLineRequest): Result<CreditLine>
    suspend fun disburse(creditLineId: Long, request: DisburseRequest): Result<CreditDisbursement>
    suspend fun mockPayOff(disbursementId: Long): Result<CreditDisbursement>
    suspend fun mockResetCreditLines(): Result<Unit>
}
