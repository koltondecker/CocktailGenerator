package com.koltondecker.cocktailgenerator.ui.screens

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.koltondecker.cocktailgenerator.data.repository.CocktailsRepository
import com.koltondecker.cocktailgenerator.data.repository.FavoritesRepository
import com.koltondecker.cocktailgenerator.data.repository.NotesRepository
import com.koltondecker.cocktailgenerator.data.repository.PantryRepository
import com.koltondecker.cocktailgenerator.domain.model.CocktailDetail
import com.koltondecker.cocktailgenerator.domain.model.CocktailIngredient
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class IngredientRowUi(
    val ingredient: CocktailIngredient,
    val inPantry: Boolean,
)

data class DetailUiState(
    val loading: Boolean = true,
    val refreshing: Boolean = false,
    val cocktail: CocktailDetail? = null,
    val ingredientRows: List<IngredientRowUi> = emptyList(),
    val favorited: Boolean = false,
    val noteBody: String = "",
    val noteRating: Int? = null,
    val noteDirty: Boolean = false,
    val noteSaving: Boolean = false,
    val errorMessage: String? = null,
)

@HiltViewModel
class DetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val cocktailsRepository: CocktailsRepository,
    private val favoritesRepository: FavoritesRepository,
    private val notesRepository: NotesRepository,
    pantryRepository: PantryRepository,
) : ViewModel() {

    private val cocktailId: Long = checkNotNull(savedStateHandle.get<String>(ID_ARG)?.toLongOrNull()) {
        "DetailViewModel requires a numeric '$ID_ARG' nav argument"
    }

    private val internal = MutableStateFlow(DetailUiState())

    val state: StateFlow<DetailUiState> = combine(
        internal,
        pantryRepository.observePantryIds(),
    ) { s, pantryIds ->
        val rows = s.cocktail?.ingredients.orEmpty().map { ing ->
            IngredientRowUi(ingredient = ing, inPantry = ing.ingredientId in pantryIds)
        }
        s.copy(ingredientRows = rows)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = DetailUiState(),
    )

    init { load() }

    private fun load() = fetch(initial = true)

    /**
     * Re-pull cocktail + favorite + note. Preserves any locally-dirty note
     * edits so an accidental pull doesn't blow the user's in-progress work.
     */
    fun refresh() = fetch(initial = false)

    private fun fetch(initial: Boolean) {
        if (internal.value.refreshing) return
        internal.update {
            it.copy(loading = initial, refreshing = !initial, errorMessage = null)
        }
        viewModelScope.launch {
            runCatching {
                val detail = cocktailsRepository.getCocktailDetail(cocktailId)
                val favorited = favoritesRepository.isFavorite(cocktailId)
                val note = notesRepository.getNote(cocktailId)
                Triple(detail, favorited, note)
            }.onSuccess { (detail, favorited, note) ->
                internal.update {
                    val keepDirty = it.noteDirty
                    it.copy(
                        loading = false,
                        refreshing = false,
                        cocktail = detail,
                        favorited = favorited,
                        noteBody = if (keepDirty) it.noteBody else note?.body.orEmpty(),
                        noteRating = if (keepDirty) it.noteRating else note?.personalRating,
                        noteDirty = keepDirty,
                    )
                }
            }.onFailure { err ->
                internal.update {
                    it.copy(
                        loading = false,
                        refreshing = false,
                        errorMessage = err.message ?: "Couldn't load cocktail.",
                    )
                }
            }
        }
    }

    fun toggleFavorite() {
        val next = !internal.value.favorited
        internal.update { it.copy(favorited = next) } // optimistic
        viewModelScope.launch {
            runCatching { favoritesRepository.setFavorite(cocktailId, next) }
                .onFailure { err ->
                    internal.update {
                        it.copy(favorited = !next, errorMessage = err.message ?: "Couldn't update favorite.")
                    }
                }
        }
    }

    fun onNoteBodyChange(v: String) = internal.update { it.copy(noteBody = v, noteDirty = true) }

    fun onNoteRatingChange(v: Int?) = internal.update { it.copy(noteRating = v, noteDirty = true) }

    fun saveNote() {
        val s = internal.value
        if (!s.noteDirty || s.noteSaving) return
        internal.update { it.copy(noteSaving = true, errorMessage = null) }
        viewModelScope.launch {
            runCatching { notesRepository.saveNote(cocktailId, s.noteBody, s.noteRating) }
                .onSuccess { internal.update { it.copy(noteSaving = false, noteDirty = false) } }
                .onFailure { err ->
                    internal.update {
                        it.copy(noteSaving = false, errorMessage = err.message ?: "Couldn't save note.")
                    }
                }
        }
    }

    fun clearError() = internal.update { it.copy(errorMessage = null) }

    companion object {
        /** Nav-arg key for the cocktail id, matching the route in SignedInNav. */
        const val ID_ARG = "id"
    }
}
