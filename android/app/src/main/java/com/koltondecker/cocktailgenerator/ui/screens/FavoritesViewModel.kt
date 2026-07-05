package com.koltondecker.cocktailgenerator.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.koltondecker.cocktailgenerator.data.repository.CocktailsRepository
import com.koltondecker.cocktailgenerator.data.repository.FavoritesRepository
import com.koltondecker.cocktailgenerator.domain.model.CocktailMatch
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FavoritesUiState(
    val loading: Boolean = true,
    val refreshing: Boolean = false,
    val results: List<CocktailMatch> = emptyList(),
    val errorMessage: String? = null,
)

@HiltViewModel
class FavoritesViewModel @Inject constructor(
    private val cocktailsRepository: CocktailsRepository,
    private val favoritesRepository: FavoritesRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(FavoritesUiState())
    val state: StateFlow<FavoritesUiState> = _state.asStateFlow()

    init { load() }

    private fun load() = fetch(initial = true)

    fun refresh() = fetch(initial = false)

    private fun fetch(initial: Boolean) {
        if (_state.value.refreshing) return
        _state.update {
            it.copy(loading = initial, refreshing = !initial, errorMessage = null)
        }
        viewModelScope.launch {
            runCatching {
                val favIds = favoritesRepository.favoriteIds()
                if (favIds.isEmpty()) {
                    emptyList()
                } else {
                    // Reuse match() so each favorite carries missing_count +
                    // ingredient chips — same UX as Browse without a second
                    // schema-specific fetch path.
                    cocktailsRepository.match().filter { it.id in favIds }
                }
            }.onSuccess { results ->
                _state.update {
                    it.copy(loading = false, refreshing = false, results = results)
                }
            }.onFailure { err ->
                _state.update {
                    it.copy(
                        loading = false,
                        refreshing = false,
                        errorMessage = err.message ?: "Couldn't load favorites.",
                    )
                }
            }
        }
    }

    fun clearError() = _state.update { it.copy(errorMessage = null) }
}
