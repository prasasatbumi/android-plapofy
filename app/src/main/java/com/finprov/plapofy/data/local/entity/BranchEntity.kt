package com.finprov.plapofy.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.finprov.plapofy.domain.model.Branch

@Entity(tableName = "branch")
data class BranchEntity(
    @PrimaryKey
    val id: Long,
    val name: String,
    val location: String?
) {
    fun toDomain(): Branch {
        return Branch(
            id = id,
            name = name,
            location = location
        )
    }
}

fun Branch.toEntity(): BranchEntity {
    return BranchEntity(
        id = id,
        name = name,
        location = location
    )
}
