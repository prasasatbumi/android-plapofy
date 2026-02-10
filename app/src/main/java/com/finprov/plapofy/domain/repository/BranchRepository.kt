package com.finprov.plapofy.domain.repository

import com.finprov.plapofy.domain.model.Branch

interface BranchRepository {
    suspend fun getBranches(): Result<List<Branch>>
}
