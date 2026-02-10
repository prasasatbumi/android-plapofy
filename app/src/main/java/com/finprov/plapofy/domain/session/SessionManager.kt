package com.finprov.plapofy.domain.session

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * SessionManager handles session-related events like token expiration.
 * Uses SharedFlow to emit session events that can be observed from UI.
 */
@Singleton
class SessionManager @Inject constructor() {
    
    private val _sessionExpiredEvent = MutableSharedFlow<SessionExpiredEvent>(
        replay = 0,
        extraBufferCapacity = 1
    )
    val sessionExpiredEvent: SharedFlow<SessionExpiredEvent> = _sessionExpiredEvent.asSharedFlow()
    
    suspend fun emitSessionExpired(message: String = "Session habis, silahkan login ulang") {
        _sessionExpiredEvent.emit(SessionExpiredEvent(message))
    }
}

data class SessionExpiredEvent(val message: String)
