package com.finprov.plapofy.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.finprov.plapofy.data.analytics.AnalyticsHelper
import com.finprov.plapofy.data.remote.dto.ResetPasswordRequest 
import com.finprov.plapofy.domain.model.User
import com.finprov.plapofy.domain.repository.AuthRepository
import com.finprov.plapofy.presentation.common.ErrorMessageMapper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

data class LoginState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null
)

data class ForgotPasswordState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null
)

data class ResetPasswordState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null
)

data class AuthState(
    val isLoading: Boolean = false,
    val user: User? = null,
    val error: String? = null,
    val isSuccess: Boolean = false
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _loginState = MutableStateFlow(LoginState())
    val loginState: StateFlow<LoginState> = _loginState.asStateFlow()

    private val _forgotPasswordState = MutableStateFlow(ForgotPasswordState())
    val forgotPasswordState: StateFlow<ForgotPasswordState> = _forgotPasswordState.asStateFlow()

    private val _resetPasswordState = MutableStateFlow(ResetPasswordState())
    val resetPasswordState: StateFlow<ResetPasswordState> = _resetPasswordState.asStateFlow()

    private val _registerState = MutableStateFlow(AuthState())
    val registerState: StateFlow<AuthState> = _registerState.asStateFlow()

    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    init {
        checkLoginStatus()
    }

    private fun checkLoginStatus() {
        viewModelScope.launch {
            _isLoggedIn.value = authRepository.isLoggedIn()
        }
    }

    fun login(username: String, password: String, fcmToken: String? = null) {
        if (username.isBlank() || password.isBlank()) {
            _loginState.value = LoginState(error = "Username dan password tidak boleh kosong")
            return
        }

        viewModelScope.launch {
            _loginState.value = LoginState(isLoading = true)
            
            authRepository.login(username, password, fcmToken)
                .onSuccess { user ->
                    AnalyticsHelper.logLogin("email")
                    AnalyticsHelper.setUserId(user.id.toString())
                    // We don't store user in LoginState as UI doesn't allow accessing it, 
                    // and Repository already caches it in TokenManager/CurrentUser
                    _loginState.value = LoginState(
                        isSuccess = true
                    )
                    _isLoggedIn.value = true
                }
                .onFailure { exception ->
                    _loginState.value = LoginState(
                        error = ErrorMessageMapper.parse(exception)
                    )
                }
        }
    }

    fun register(
        username: String,
        password: String,
        confirmPassword: String,
        email: String,
        name: String,
        phoneNumber: String
    ) {
        // Validation
        when {
            username.isBlank() -> {
                _registerState.value = AuthState(error = "Username tidak boleh kosong")
                return
            }
            password.isBlank() -> {
                _registerState.value = AuthState(error = "Password tidak boleh kosong")
                return
            }
            password.length < 6 -> {
                _registerState.value = AuthState(error = "Password minimal 6 karakter")
                return
            }
            password != confirmPassword -> {
                _registerState.value = AuthState(error = "Password tidak cocok")
                return
            }
            email.isBlank() -> {
                _registerState.value = AuthState(error = "Email tidak boleh kosong")
                return
            }
            !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                _registerState.value = AuthState(error = "Format email tidak valid")
                return
            }
            name.isBlank() -> {
                _registerState.value = AuthState(error = "Nama tidak boleh kosong")
                return
            }
            phoneNumber.isBlank() -> {
                _registerState.value = AuthState(error = "Nomor telepon tidak boleh kosong")
                return
            }
        }

        viewModelScope.launch {
            _registerState.value = AuthState(isLoading = true)
            
            authRepository.register(username, password, email, name, phoneNumber)
                .onSuccess { user ->
                    AnalyticsHelper.logSignUp("email")
                    _registerState.value = AuthState(
                        user = user,
                        isSuccess = true
                    )
                }
                .onFailure { exception ->
                    _registerState.value = AuthState(
                        error = ErrorMessageMapper.parse(exception)
                    )
                }
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
            _isLoggedIn.value = false
            _loginState.value = LoginState()
        }
    }

    fun clearLoginError() {
        _loginState.value = _loginState.value.copy(error = null)
    }

    fun clearRegisterError() {
        _registerState.value = _registerState.value.copy(error = null)
    }

    fun resetLoginState() {
        _loginState.value = LoginState()
    }

    fun resetRegisterState() {
        _registerState.value = AuthState()
    }

    fun resetForgotPasswordState() {
        _forgotPasswordState.value = ForgotPasswordState()
    }

    fun resetResetPasswordState() {
        _resetPasswordState.value = ResetPasswordState()
    }

    fun forgotPassword(email: String) {
        viewModelScope.launch {
            _forgotPasswordState.value = ForgotPasswordState(isLoading = true)
            val result = authRepository.forgotPassword(email)
            if (result.isSuccess) {
                _forgotPasswordState.value = ForgotPasswordState(isSuccess = true)
            } else {
                _forgotPasswordState.value = ForgotPasswordState(error = ErrorMessageMapper.parse(result.exceptionOrNull()))
            }
        }
    }

    fun resetPassword(email: String, otp: String, newPassword: String) {
        viewModelScope.launch {
            _resetPasswordState.value = ResetPasswordState(isLoading = true)
            val request = com.finprov.plapofy.data.remote.dto.ResetPasswordRequest(
                email = email,
                token = otp,
                newPassword = newPassword
            )
            val result = authRepository.resetPassword(request)
            if (result.isSuccess) {
                _resetPasswordState.value = ResetPasswordState(isSuccess = true)
            } else {
                _resetPasswordState.value = ResetPasswordState(error = ErrorMessageMapper.parse(result.exceptionOrNull()))
            }
        }
    }

    fun signInWithGoogle(idToken: String, fcmToken: String? = null) {
        viewModelScope.launch {
            _loginState.value = LoginState(isLoading = true)
            
            try {
                // 1. Exchange Google ID Token for Firebase Credential
                val credential = com.google.firebase.auth.GoogleAuthProvider.getCredential(idToken, null)
                
                // 2. Sign in to Firebase Auth
                val authResult = com.google.firebase.auth.FirebaseAuth.getInstance().signInWithCredential(credential).await()
                val firebaseUser = authResult.user
                
                if (firebaseUser != null) {
                    // 3. Get Firebase ID Token (this has the correct 'aud' for backend)
                    val tokenResult = firebaseUser.getIdToken(true).await()
                    val firebaseIdToken = tokenResult.token
                    
                    if (firebaseIdToken != null) {
                        // 4. Send Firebase ID Token to Backend
                            authRepository.signInWithGoogle(firebaseIdToken, fcmToken)
                                .onSuccess { user ->
                                    AnalyticsHelper.logLogin("google")
                                    AnalyticsHelper.setUserId(user.id.toString())
                                    _loginState.value = LoginState(
                                        isSuccess = true
                                    )
                                    _isLoggedIn.value = true
                                }
                                .onFailure { exception ->
                                    _loginState.value = LoginState(
                                        error = ErrorMessageMapper.parse(exception)
                                    )
                                }
                        } else {
                            throw Exception("Failed to retrieve Firebase ID Token")
                        }
                    } else {
                         throw Exception("Firebase Authentication failed")
                    }
                } catch (e: Exception) {
                    _loginState.value = LoginState(
                        error = ErrorMessageMapper.parse(e)
                    )
                }
        }
    }


    fun clearResetPasswordState() {
        _resetPasswordState.value = ResetPasswordState()
    }
}
