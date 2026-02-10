package com.finprov.plapofy.data.repository

import com.finprov.plapofy.data.local.dao.PlafondDao
import com.finprov.plapofy.data.remote.api.PlafondApi
import com.finprov.plapofy.data.remote.dto.toEntity
import com.finprov.plapofy.domain.model.Plafond
import com.finprov.plapofy.domain.repository.PlafondRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class PlafondRepositoryImpl @Inject constructor(
    private val api: PlafondApi,
    private val dao: PlafondDao
) : PlafondRepository {

    override suspend fun getPlafonds(): Result<List<Plafond>> {
        return try {
            val response = api.getPlafonds()
            if (response.success && response.data != null) {
                // Save to local
                dao.insertPlafonds(response.data.map { it.toEntity() })
                
                // Return result
                // We can map from API DTO or read from DB. For simplicity now:
                // Map from DTO via Entity logic or duplicate matching logic.
                // Best: Use caching DAO
                val domainList = response.data.map { it.toEntity().toDomain() }
                Result.success(domainList)
            } else {
                throw Exception(response.message)
            }
        } catch (e: Exception) {
            // Fallback
            try {
                val cached = dao.getPlafonds().first()
                if (cached.isNotEmpty()) {
                    Result.success(cached.map { it.toDomain() })
                } else {
                    Result.failure(e)
                }
            } catch (localE: Exception) {
                Result.failure(e)
            }
        }
    }
}
