package com.finprov.plapofy.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.finprov.plapofy.data.local.dao.BranchDao
import com.finprov.plapofy.data.local.dao.LoanDao
import com.finprov.plapofy.data.local.dao.PendingLoanDao
import com.finprov.plapofy.data.local.dao.PendingDisbursementDao
import com.finprov.plapofy.data.local.dao.PlafondDao
import com.finprov.plapofy.data.local.dao.UserDao
import com.finprov.plapofy.data.local.dao.CreditLineDao
import com.finprov.plapofy.data.local.entity.BranchEntity
import com.finprov.plapofy.data.local.entity.LoanEntity
import com.finprov.plapofy.data.local.entity.PendingLoanEntity
import com.finprov.plapofy.data.local.entity.PendingDisbursementEntity
import com.finprov.plapofy.data.local.entity.PlafondEntity
import com.finprov.plapofy.data.local.entity.UserEntity
import com.finprov.plapofy.data.local.entity.CreditLineEntity

@Database(
    entities = [
        UserEntity::class,
        PlafondEntity::class,
        BranchEntity::class,
        LoanEntity::class,
        PendingLoanEntity::class,
        PendingDisbursementEntity::class,
        CreditLineEntity::class
    ],
    version = 5,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun plafondDao(): PlafondDao
    abstract fun branchDao(): BranchDao
    abstract fun loanDao(): LoanDao
    abstract fun pendingLoanDao(): PendingLoanDao
    abstract fun pendingDisbursementDao(): PendingDisbursementDao
    abstract fun creditLineDao(): CreditLineDao
}
