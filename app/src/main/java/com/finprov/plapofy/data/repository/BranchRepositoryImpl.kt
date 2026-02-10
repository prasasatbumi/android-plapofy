package com.finprov.plapofy.data.repository

import com.finprov.plapofy.data.local.dao.BranchDao
import com.finprov.plapofy.data.remote.api.BranchApi
import com.finprov.plapofy.data.remote.dto.toEntity
import com.finprov.plapofy.domain.model.Branch
import com.finprov.plapofy.domain.repository.BranchRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class BranchRepositoryImpl @Inject constructor(
    private val api: BranchApi,
    private val dao: BranchDao
) : BranchRepository {
    override suspend fun getBranches(): Result<List<Branch>> {
        return try {
            val response = api.getBranches()
            if (response.success && response.data != null) {
                dao.insertBranches(response.data.map { it.toEntity() })
                
                val branches = response.data.map { it.toEntity().toDomain() }
                Result.success(branches)
            } else {
                throw Exception(response.message)
            }
        } catch (e: Exception) {
             try {
                val cached = dao.getBranches().first()
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
