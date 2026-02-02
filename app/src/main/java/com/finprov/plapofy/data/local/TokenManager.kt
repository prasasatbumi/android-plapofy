package com.finprov.plapofy.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "plapofy_prefs")

@Singleton
class TokenManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private val TOKEN_KEY = stringPreferencesKey("jwt_token")
        private val USER_ID_KEY = longPreferencesKey("user_id")
        private val USERNAME_KEY = stringPreferencesKey("username")
        private val USER_NAME_KEY = stringPreferencesKey("user_name")
    }

    // Token Flow
    val token: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[TOKEN_KEY]
    }

    // User ID Flow
    val userId: Flow<Long?> = context.dataStore.data.map { preferences ->
        preferences[USER_ID_KEY]
    }

    // Username Flow
    val username: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[USERNAME_KEY]
    }

    // User Name Flow
    val userName: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[USER_NAME_KEY]
    }

    // Check if user is logged in
    val isLoggedIn: Flow<Boolean> = context.dataStore.data.map { preferences ->
        !preferences[TOKEN_KEY].isNullOrEmpty()
    }

    // Save token and user info
    suspend fun saveAuthData(token: String, userId: Long, username: String, userName: String?) {
        context.dataStore.edit { preferences ->
            preferences[TOKEN_KEY] = token
            preferences[USER_ID_KEY] = userId
            preferences[USERNAME_KEY] = username
            userName?.let { preferences[USER_NAME_KEY] = it }
        }
    }

    // Get token synchronously (for interceptor)
    suspend fun getToken(): String? {
        return context.dataStore.data.first()[TOKEN_KEY]
    }

    // Clear all auth data (logout)
    suspend fun clearAuthData() {
        context.dataStore.edit { preferences ->
            preferences.remove(TOKEN_KEY)
            preferences.remove(USER_ID_KEY)
            preferences.remove(USERNAME_KEY)
            preferences.remove(USER_NAME_KEY)
        }
    }
}
