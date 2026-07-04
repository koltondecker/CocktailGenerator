package com.koltondecker.cocktailgenerator.ui.session

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.koltondecker.cocktailgenerator.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SessionViewModel @Inject constructor(
    private val authRepository: AuthRepository,
) : ViewModel() {

    val sessionStatus: StateFlow<SessionStatus> = authRepository.sessionStatus

    fun signOut() {
        viewModelScope.launch { authRepository.signOut() }
    }
}
