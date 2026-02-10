package com.finprov.plapofy.data.remote.interceptor

import com.finprov.plapofy.data.local.TokenManager
import com.finprov.plapofy.domain.session.SessionManager
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

/**
 * Interceptor that detects 401 Unauthorized responses and triggers session expiration.
 * When a 401 is received, it clears the token and emits a session expired event.
 */
class SessionExpiredInterceptor @Inject constructor(
    private val tokenManager: TokenManager,
    private val sessionManager: SessionManager
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val response = chain.proceed(chain.request())
        
        if (response.code == 401) {
            // Token expired or invalid
            runBlocking {
                tokenManager.clearAuthData()
                sessionManager.emitSessionExpired("Session habis, silahkan login ulang")
            }
        }
        
        return response
    }
}
