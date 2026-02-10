package com.finprov.plapofy.domain.usecase

import com.finprov.plapofy.domain.model.CreditLine
import com.finprov.plapofy.domain.repository.CreditLineRepository
import com.finprov.plapofy.domain.model.Plafond
import com.finprov.plapofy.domain.repository.PlafondRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class CreditLineInformationUseCase @Inject constructor(
    private val creditLineRepository: CreditLineRepository,
    private val plafondRepository: PlafondRepository
) {
    fun getActiveCreditLine(): Flow<CreditLine?> {
        return creditLineRepository.getActiveCreditLine()
    }
    
    fun getMyCreditLines(): Flow<List<CreditLine>> {
        return creditLineRepository.getMyCreditLines()
    }
    
    // Helper to get Plafonds if no active credit line
    suspend fun getAvailablePlafonds(): Result<List<Plafond>> {
        return plafondRepository.getPlafonds()
    }
}
