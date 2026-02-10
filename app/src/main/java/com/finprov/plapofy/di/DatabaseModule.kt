package com.finprov.plapofy.di

import android.content.Context
import androidx.room.Room
import com.finprov.plapofy.data.local.AppDatabase
import com.finprov.plapofy.data.local.dao.BranchDao
import com.finprov.plapofy.data.local.dao.CreditLineDao
import com.finprov.plapofy.data.local.dao.LoanDao
import com.finprov.plapofy.data.local.dao.PendingLoanDao
import com.finprov.plapofy.data.local.dao.PendingDisbursementDao
import com.finprov.plapofy.data.local.dao.PlafondDao
import com.finprov.plapofy.data.local.dao.UserDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "plapofy_db"
        )
        .fallbackToDestructiveMigration() // For development simplicity
        .build()
    }

    @Provides
    fun provideUserDao(database: AppDatabase): UserDao = database.userDao()

    @Provides
    fun providePlafondDao(database: AppDatabase): PlafondDao = database.plafondDao()

    @Provides
    fun provideBranchDao(database: AppDatabase): BranchDao = database.branchDao()

    @Provides
    fun provideLoanDao(database: AppDatabase): LoanDao = database.loanDao()

    @Provides
    fun providePendingLoanDao(database: AppDatabase): PendingLoanDao = database.pendingLoanDao()

    @Provides
    fun providePendingDisbursementDao(database: AppDatabase): PendingDisbursementDao = database.pendingDisbursementDao()

    @Provides
    fun provideCreditLineDao(database: AppDatabase): CreditLineDao = database.creditLineDao()
}
