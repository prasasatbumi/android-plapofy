package com.finprov.plapofy.di

import android.content.Context

import com.finprov.plapofy.BuildConfig
import com.finprov.plapofy.data.local.TokenManager
import com.finprov.plapofy.data.remote.api.AuthApi
import com.finprov.plapofy.data.remote.api.BranchApi
import com.finprov.plapofy.data.remote.api.CreditLineApi
import com.finprov.plapofy.data.remote.api.LoanApi
import com.finprov.plapofy.data.remote.api.PlafondApi
import com.finprov.plapofy.data.remote.api.ProfileApi
import com.finprov.plapofy.data.remote.interceptor.AuthInterceptor
import com.finprov.plapofy.data.remote.interceptor.SessionExpiredInterceptor
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import okhttp3.CertificatePinner
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    /**
     * SSL Pinning Configuration
     * 
     * IMPORTANT: Replace the placeholder pins with your production server's certificate pins.
     * To get the SHA-256 pin from your server, run:
     * openssl s_client -servername YOUR_DOMAIN -connect YOUR_DOMAIN:443 | openssl x509 -pubkey -noout | openssl pkey -pubin -outform der | openssl dgst -sha256 -binary | openssl enc -base64
     * 
     * The pin format is: "sha256/BASE64_ENCODED_HASH"
     */
    @Provides
    @Singleton
    fun provideCertificatePinner(): CertificatePinner {
        // TODO: Replace with your production server's certificate pins
        // For development/localhost, SSL pinning is disabled (empty pinner)
        // For production, uncomment and configure:
        /*
        return CertificatePinner.Builder()
            .add("api.yourdomain.com", "sha256/AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=")
            .add("api.yourdomain.com", "sha256/BBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB=") // Backup pin
            .build()
        */
        return CertificatePinner.Builder().build() // Empty for development
    }

    @Provides
    @Singleton
    fun provideAuthInterceptor(tokenManager: TokenManager): AuthInterceptor {
        return AuthInterceptor(tokenManager)
    }



    @Provides
    @Singleton
    fun provideSessionExpiredInterceptor(
        tokenManager: TokenManager,
        sessionManager: com.finprov.plapofy.domain.session.SessionManager
    ): SessionExpiredInterceptor {
        return SessionExpiredInterceptor(tokenManager, sessionManager)
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(
        authInterceptor: AuthInterceptor,
        sessionExpiredInterceptor: SessionExpiredInterceptor,
        authInterceptor: AuthInterceptor,
        sessionExpiredInterceptor: SessionExpiredInterceptor,
        certificatePinner: CertificatePinner,
        @ApplicationContext context: Context
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .certificatePinner(certificatePinner)
            .addInterceptor(authInterceptor)
            .addInterceptor(sessionExpiredInterceptor)
            .addInterceptor(com.chuckerteam.chucker.api.ChuckerInterceptor.Builder(@ApplicationContext context).build())
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        val baseUrl = BuildConfig.BASE_URL

        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideAuthApi(retrofit: Retrofit): AuthApi {
        return retrofit.create(AuthApi::class.java)
    }

    @Provides
    @Singleton
    fun providePlafondApi(retrofit: Retrofit): PlafondApi {
        return retrofit.create(PlafondApi::class.java)
    }

    @Provides
    @Singleton
    fun provideLoanApi(retrofit: Retrofit): LoanApi {
        return retrofit.create(LoanApi::class.java)
    }

    @Provides
    @Singleton
    fun provideProfileApi(retrofit: Retrofit): ProfileApi {
        return retrofit.create(ProfileApi::class.java)
    }

    @Provides
    @Singleton
    fun provideBranchApi(retrofit: Retrofit): BranchApi {
        return retrofit.create(BranchApi::class.java)
    }

    @Provides
    @Singleton
    fun provideCreditLineApi(retrofit: Retrofit): CreditLineApi {
        return retrofit.create(CreditLineApi::class.java)
    }

    @Provides
    @Singleton
    fun providePinApi(retrofit: Retrofit): com.finprov.plapofy.data.remote.api.PinApi {
        return retrofit.create(com.finprov.plapofy.data.remote.api.PinApi::class.java)
    }
}
