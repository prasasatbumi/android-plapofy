package com.finprov.plapofy.data.remote.interceptor

import com.finprov.plapofy.data.local.TokenManager
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

/**
 * Interceptor that adds JWT token to authenticated API requests.
 * Token is retrieved from DataStore and added as Authorization header.
 */
class AuthInterceptor @Inject constructor(
    private val tokenManager: TokenManager
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        
        // Get token from DataStore (blocking call - necessary for interceptor)
        val token = runBlocking { tokenManager.getToken() }
        
        // If no token, proceed with original request
        if (token.isNullOrEmpty()) {
            return chain.proceed(originalRequest)
        }

        // Add Authorization header
        val authenticatedRequest = originalRequest.newBuilder()
            .header("Authorization", "Bearer $token")
            .build()

        return chain.proceed(authenticatedRequest)
    }
}
