package com.koltondecker.cocktailgenerator.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.koltondecker.cocktailgenerator.data.repository.CocktailsRepository
import com.koltondecker.cocktailgenerator.domain.model.CocktailMatch
import com.koltondecker.cocktailgenerator.domain.model.MatchFilters
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class BrowseTab(val label: String) {
    CAN_MAKE("Can make"),
    ONE_AWAY("1 away"),
    ALL("All"),
}

data class BrowseUiState(
    val tab: BrowseTab = BrowseTab.CAN_MAKE,
    val filters: MatchFilters = MatchFilters(),
    val loading: Boolean = true,
    val refreshing: Boolean = false,
    val filterSheetOpen: Boolean = false,
    val allResults: List<CocktailMatch> = emptyList(),
    val errorMessage: String? = null,
) {
    val visibleResults: List<CocktailMatch>
        get() = when (tab) {
            BrowseTab.CAN_MAKE -> allResults.filter { it.missingCount == 0 }
            BrowseTab.ONE_AWAY -> allResults.filter { it.missingCount == 1 }
            BrowseTab.ALL      -> allResults
        }
}

@HiltViewModel
class BrowseViewModel @Inject constructor(
    private val repository: CocktailsRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(BrowseUiState())
    val state: StateFlow<BrowseUiState> = _state.asStateFlow()

    init { load() }

    fun selectTab(tab: BrowseTab)   = _state.update { it.copy(tab = tab) }
    fun openFilters()               = _state.update { it.copy(filterSheetOpen = true) }
    fun closeFilters()              = _state.update { it.copy(filterSheetOpen = false) }
    fun clearError()                = _state.update { it.copy(errorMessage = null) }

    fun applyFilters(filters: MatchFilters) {
        _state.update { it.copy(filters = filters, filterSheetOpen = false) }
        // A filter change is closer to a fresh load than a pull-refresh —
        // show the full-screen spinner while results reshape.
        load()
    }

    private fun load() = fetch(initial = true)

    fun refresh() = fetch(initial = false)

    private fun fetch(initial: Boolean) {
        if (_state.value.refreshing) return
        val filters = _state.value.filters
        _state.update {
            it.copy(loading = initial, refreshing = !initial, errorMessage = null)
        }
        viewModelScope.launch {
            runCatching { repository.match(filters = filters) }
                .onSuccess { results ->
                    _state.update {
                        it.copy(loading = false, refreshing = false, allResults = results)
                    }
                }
                .onFailure { err ->
                    _state.update {
                        it.copy(
                            loading = false,
                            refreshing = false,
                            errorMessage = err.message ?: "Couldn't load cocktails.",
                        )
                    }
                }
        }
    }
}
