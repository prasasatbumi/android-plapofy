package com.finprov.plapofy.di

import com.finprov.plapofy.data.repository.AuthRepositoryImpl
import com.finprov.plapofy.data.repository.BranchRepositoryImpl
import com.finprov.plapofy.data.repository.CreditLineRepositoryImpl
import com.finprov.plapofy.data.repository.LoanRepositoryImpl
import com.finprov.plapofy.data.repository.PlafondRepositoryImpl
import com.finprov.plapofy.data.repository.ProfileRepositoryImpl
import com.finprov.plapofy.domain.repository.AuthRepository
import com.finprov.plapofy.domain.repository.BranchRepository
import com.finprov.plapofy.domain.repository.CreditLineRepository
import com.finprov.plapofy.domain.repository.LoanRepository
import com.finprov.plapofy.domain.repository.PlafondRepository
import com.finprov.plapofy.domain.repository.ProfileRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        impl: AuthRepositoryImpl
    ): AuthRepository

    @Binds
    @Singleton
    abstract fun bindPlafondRepository(
        impl: PlafondRepositoryImpl
    ): PlafondRepository

    @Binds
    @Singleton
    abstract fun bindLoanRepository(
        impl: LoanRepositoryImpl
    ): LoanRepository

    @Binds
    @Singleton
    abstract fun bindProfileRepository(
        impl: ProfileRepositoryImpl
    ): ProfileRepository

    @Binds
    @Singleton
    abstract fun bindBranchRepository(
        impl: BranchRepositoryImpl
    ): BranchRepository

    @Binds
    @Singleton
    abstract fun bindCreditLineRepository(
        impl: CreditLineRepositoryImpl
    ): CreditLineRepository

    @Binds
    @Singleton
    abstract fun bindPinRepository(
        impl: com.finprov.plapofy.data.repository.PinRepositoryImpl
    ): com.finprov.plapofy.domain.repository.PinRepository

}
