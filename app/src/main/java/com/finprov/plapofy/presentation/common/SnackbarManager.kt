package com.finprov.plapofy.presentation.common

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

object SnackbarManager {
    private val _messages = MutableSharedFlow<String>()
    val messages: SharedFlow<String> = _messages.asSharedFlow()

    suspend fun showMessage(message: String) {
        _messages.emit(message)
    }
}
