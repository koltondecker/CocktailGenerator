package com.koltondecker.cocktailgenerator.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.koltondecker.cocktailgenerator.data.repository.PantryRepository
import com.koltondecker.cocktailgenerator.domain.model.Ingredient
import com.koltondecker.cocktailgenerator.domain.model.IngredientCategory
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PantryItemUi(
    val ingredient: Ingredient,
    val inPantry: Boolean,
)

data class PantryCategoryGroup(
    val category: IngredientCategory,
    val items: List<PantryItemUi>,
)

data class PantryUiState(
    val loading: Boolean = true,
    val refreshing: Boolean = false,
    val query: String = "",
    val groups: List<PantryCategoryGroup> = emptyList(),
    val errorMessage: String? = null,
)

@HiltViewModel
class PantryViewModel @Inject constructor(
    private val repository: PantryRepository,
) : ViewModel() {

    private val query = MutableStateFlow("")
    private val errorMessage = MutableStateFlow<String?>(null)
    private val loading = MutableStateFlow(true)
    private val refreshing = MutableStateFlow(false)

    val state: StateFlow<PantryUiState> = combine(
        repository.observeCategories(),
        repository.observeCommonIngredients(),
        repository.observePantryIds(),
        query,
        combine(loading, refreshing, errorMessage) { l, r, e -> Triple(l, r, e) },
    ) { categories, ingredients, pantryIds, q, meta ->
        val filter = q.trim().lowercase()
        val groups = categories
            .sortedBy { it.sortOrder }
            .mapNotNull { cat ->
                val items = ingredients
                    .asSequence()
                    .filter { it.categoryId == cat.id }
                    .filter { filter.isEmpty() || it.name.lowercase().contains(filter) }
                    .map { PantryItemUi(it, it.id in pantryIds) }
                    .toList()
                if (items.isEmpty()) null else PantryCategoryGroup(cat, items)
            }
        val (l, r, e) = meta
        PantryUiState(
            loading = l && groups.isEmpty(),
            refreshing = r,
            query = q,
            groups = groups,
            errorMessage = e,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = PantryUiState(),
    )

    init { refresh() }

    fun onQueryChange(q: String) = query.update { q }

    fun refresh() {
        viewModelScope.launch {
            refreshing.update { true }
            runCatching {
                repository.refreshCatalog()
                repository.refreshPantry()
            }.onFailure { err ->
                errorMessage.update { err.message ?: "Couldn't sync your pantry." }
            }
            loading.update { false }
            refreshing.update { false }
        }
    }

    fun setInPantry(ingredient: Ingredient, inPantry: Boolean) {
        viewModelScope.launch {
            runCatching { repository.setInPantry(ingredient.id, inPantry) }
                .onFailure { err ->
                    errorMessage.update { err.message ?: "Couldn't update ${ingredient.name}." }
                }
        }
    }

    fun dismissError() = errorMessage.update { null }
}
