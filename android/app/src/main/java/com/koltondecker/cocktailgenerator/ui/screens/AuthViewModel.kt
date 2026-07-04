package com.koltondecker.cocktailgenerator.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.koltondecker.cocktailgenerator.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.jan.supabase.compose.auth.ComposeAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class AuthMode { SIGN_IN, SIGN_UP }

data class AuthUiState(
    val mode: AuthMode = AuthMode.SIGN_IN,
    val email: String = "",
    val password: String = "",
    val submitting: Boolean = false,
    val errorMessage: String? = null,
    val infoMessage: String? = null,
) {
    val canSubmit: Boolean
        get() = !submitting &&
                email.contains('@') &&
                password.length >= 6
}

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    val composeAuth: ComposeAuth,
) : ViewModel() {

    private val _state = MutableStateFlow(AuthUiState())
    val state: StateFlow<AuthUiState> = _state.asStateFlow()

    fun onEmailChange(v: String)    = _state.update { it.copy(email = v.trim(), errorMessage = null) }
    fun onPasswordChange(v: String) = _state.update { it.copy(password = v,     errorMessage = null) }
    fun toggleMode() = _state.update {
        val next = if (it.mode == AuthMode.SIGN_IN) AuthMode.SIGN_UP else AuthMode.SIGN_IN
        it.copy(mode = next, errorMessage = null, infoMessage = null)
    }

    fun submit() {
        val s = _state.value
        if (!s.canSubmit) return
        _state.update { it.copy(submitting = true, errorMessage = null, infoMessage = null) }
        viewModelScope.launch {
            val result = runCatching {
                when (s.mode) {
                    AuthMode.SIGN_IN -> authRepository.signInWithEmail(s.email, s.password)
                    AuthMode.SIGN_UP -> authRepository.signUpWithEmail(s.email, s.password)
                }
            }
            _state.update { current ->
                result.fold(
                    onSuccess = { current.copy(
                        submitting = false,
                        infoMessage = if (current.mode == AuthMode.SIGN_UP) {
                            "Check your email to confirm the address."
                        } else null,
                    ) },
                    onFailure = { err -> current.copy(
                        submitting = false,
                        errorMessage = err.message ?: "Something went wrong."
                    ) },
                )
            }
        }
    }
}
