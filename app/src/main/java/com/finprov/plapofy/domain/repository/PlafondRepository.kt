package com.finprov.plapofy.domain.repository

import com.finprov.plapofy.domain.model.Plafond

interface PlafondRepository {
    suspend fun getPlafonds(): Result<List<Plafond>>
}
