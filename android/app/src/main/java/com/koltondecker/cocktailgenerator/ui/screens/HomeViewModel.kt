package com.koltondecker.cocktailgenerator.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.koltondecker.cocktailgenerator.data.repository.CocktailsRepository
import com.koltondecker.cocktailgenerator.domain.model.CocktailMatch
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val loading: Boolean = true,
    val canMake: List<CocktailMatch> = emptyList(),
    val almost: List<CocktailMatch> = emptyList(),
    val errorMessage: String? = null,
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val cocktailsRepository: CocktailsRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(HomeUiState())
    val state: StateFlow<HomeUiState> = _state.asStateFlow()

    init { refresh() }

    fun refresh() {
        _state.update { it.copy(loading = true, errorMessage = null) }
        viewModelScope.launch {
            // One RPC call, sliced client-side; matches Browse's approach so
            // Home doesn't re-invent the pantry-relative sort order.
            runCatching { cocktailsRepository.match(missingAllowed = 1) }
                .onSuccess { results ->
                    _state.update {
                        it.copy(
                            loading = false,
                            canMake = results.filter { r -> r.missingCount == 0 },
                            almost = results.filter { r -> r.missingCount == 1 },
                        )
                    }
                }
                .onFailure { err ->
                    _state.update {
                        it.copy(loading = false, errorMessage = err.message ?: "Couldn't load Home.")
                    }
                }
        }
    }

    fun clearError() = _state.update { it.copy(errorMessage = null) }
}
