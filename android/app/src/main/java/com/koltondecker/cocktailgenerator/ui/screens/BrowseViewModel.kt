package com.koltondecker.cocktailgenerator.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.koltondecker.cocktailgenerator.data.repository.CocktailsRepository
import com.koltondecker.cocktailgenerator.data.repository.FavoritesRepository
import com.koltondecker.cocktailgenerator.data.repository.PantryRepository
import com.koltondecker.cocktailgenerator.domain.model.CocktailMatch
import com.koltondecker.cocktailgenerator.domain.model.Ingredient
import com.koltondecker.cocktailgenerator.domain.model.IngredientCategory
import com.koltondecker.cocktailgenerator.domain.model.MatchFilters
import com.koltondecker.cocktailgenerator.domain.model.SortMode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
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
    val favoriteIds: Set<Long> = emptySet(),
    val spiritOptions: List<Ingredient> = emptyList(),
    val methodOptions: List<String> = emptyList(),
    val errorMessage: String? = null,
) {
    /** Client-side pipeline: tab-slice → filters → sort. */
    val visibleResults: List<CocktailMatch>
        get() {
            val tabFiltered = when (tab) {
                BrowseTab.CAN_MAKE -> allResults.filter { it.missingCount == 0 }
                BrowseTab.ONE_AWAY -> allResults.filter { it.missingCount == 1 }
                BrowseTab.ALL -> allResults
            }
            val q = filters.query.trim().lowercase()
            val filtered = tabFiltered.asSequence()
                .let { seq -> if (filters.favoritesOnly) seq.filter { it.id in favoriteIds } else seq }
                .let { seq -> if (q.isEmpty()) seq else seq.filter { it.name.lowercase().contains(q) } }
                .toList()
            return when (filters.sortBy) {
                SortMode.MATCH -> filtered.sortedWith(
                    compareBy({ it.missingCount }, { it.name.lowercase() })
                )
                SortMode.NAME_ASC -> filtered.sortedBy { it.name.lowercase() }
                SortMode.NAME_DESC -> filtered.sortedByDescending { it.name.lowercase() }
                SortMode.SIMPLEST -> filtered.sortedWith(
                    compareBy({ it.missingIngredients.size + 0 }, { it.name.lowercase() })
                )
            }
        }
}

@HiltViewModel
class BrowseViewModel @Inject constructor(
    private val cocktailsRepository: CocktailsRepository,
    private val favoritesRepository: FavoritesRepository,
    pantryRepository: PantryRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(BrowseUiState())

    /** Spirits pulled from the Room-mirrored catalog so the filter sheet has
     *  chips even offline. Emits whenever the pantry catalog refresh writes. */
    private val spiritsFlow: StateFlow<List<Ingredient>> = combine(
        pantryRepository.observeCategories(),
        pantryRepository.observeCommonIngredients(),
    ) { categories: List<IngredientCategory>, ingredients: List<Ingredient> ->
        val spiritsCatId = categories.firstOrNull { it.name.equals("Spirits", ignoreCase = true) }?.id
            ?: return@combine emptyList()
        ingredients.filter { it.categoryId == spiritsCatId }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val state: StateFlow<BrowseUiState> = combine(_state, spiritsFlow) { s, spirits ->
        s.copy(spiritOptions = spirits)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), BrowseUiState())

    init { load() }

    fun selectTab(tab: BrowseTab)   = _state.update { it.copy(tab = tab) }
    fun openFilters()               = _state.update { it.copy(filterSheetOpen = true) }
    fun closeFilters()              = _state.update { it.copy(filterSheetOpen = false) }
    fun clearError()                = _state.update { it.copy(errorMessage = null) }

    fun onQueryChange(q: String) = _state.update {
        it.copy(filters = it.filters.copy(query = q))
    }

    fun applyFilters(filters: MatchFilters) {
        val prev = _state.value.filters
        _state.update { it.copy(filters = filters, filterSheetOpen = false) }
        // Only re-hit the RPC when server-side filters actually changed.
        if (filters.baseSpiritIds != prev.baseSpiritIds || filters.methods != prev.methods) {
            load()
        }
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
            runCatching {
                val results = cocktailsRepository.match(filters = filters)
                val favIds = runCatching { favoritesRepository.favoriteIds() }.getOrDefault(emptySet())
                val methods = results.mapNotNull { it.method?.takeIf { m -> m.isNotBlank() } }
                    .distinct().sorted()
                Triple(results, favIds, methods)
            }.onSuccess { (results, favIds, methods) ->
                _state.update {
                    it.copy(
                        loading = false,
                        refreshing = false,
                        allResults = results,
                        favoriteIds = favIds,
                        methodOptions = methods,
                    )
                }
            }.onFailure { err ->
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
