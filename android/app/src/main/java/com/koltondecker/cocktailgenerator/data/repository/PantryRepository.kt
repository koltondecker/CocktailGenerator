package com.koltondecker.cocktailgenerator.data.repository

import com.koltondecker.cocktailgenerator.data.local.dao.PantryDao
import com.koltondecker.cocktailgenerator.data.local.entities.PantryItemEntity
import com.koltondecker.cocktailgenerator.data.remote.dto.IngredientCategoryDto
import com.koltondecker.cocktailgenerator.data.remote.dto.IngredientDto
import com.koltondecker.cocktailgenerator.data.remote.dto.PantryItemDto
import com.koltondecker.cocktailgenerator.domain.model.Ingredient
import com.koltondecker.cocktailgenerator.domain.model.IngredientCategory
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PantryRepository @Inject constructor(
    private val client: SupabaseClient,
    private val dao: PantryDao,
) {
    // -- observers ---------------------------------------------------------

    fun observeCategories(): Flow<List<IngredientCategory>> =
        dao.observeCategories().map { rows -> rows.map { it.toDomain() } }

    fun observeCommonIngredients(): Flow<List<Ingredient>> =
        dao.observeCommonIngredients().map { rows -> rows.map { it.toDomain() } }

    fun observePantryIds(): Flow<Set<Long>> =
        dao.observePantryIds().map { it.toSet() }

    // -- catalog refresh (idempotent; safe to call repeatedly) -------------

    suspend fun refreshCatalog() {
        val categories = client.postgrest["ingredient_categories"]
            .select()
            .decodeList<IngredientCategoryDto>()
        val ingredients = client.postgrest["ingredients"]
            .select {
                filter { eq("is_common", true) }
            }
            .decodeList<IngredientDto>()

        dao.upsertCategories(categories.map { it.toEntity() })
        dao.upsertIngredients(ingredients.map { it.toEntity() })
    }

    // -- pantry sync (RLS filters to auth.uid()) ---------------------------

    suspend fun refreshPantry() {
        val rows = client.postgrest["user_pantry"]
            .select()
            .decodeList<PantryItemDto>()
        dao.replacePantry(rows.map { PantryItemEntity(ingredientId = it.ingredientId) })
    }

    /**
     * Optimistically flip pantry membership for [ingredientId]. On failure the
     * local mirror is reverted so UI state stays truthful.
     *
     * @throws IllegalStateException if there's no authenticated user.
     */
    suspend fun setInPantry(ingredientId: Long, inPantry: Boolean) {
        val userId = client.auth.currentUserOrNull()?.id
            ?: throw IllegalStateException("No authenticated user for pantry toggle")

        if (inPantry) {
            dao.addToPantry(PantryItemEntity(ingredientId))
        } else {
            dao.removeFromPantry(ingredientId)
        }

        try {
            if (inPantry) {
                client.postgrest["user_pantry"].upsert(
                    value = PantryItemDto(userId = userId, ingredientId = ingredientId),
                )
            } else {
                client.postgrest["user_pantry"].delete {
                    filter {
                        eq("user_id", userId)
                        eq("ingredient_id", ingredientId)
                    }
                }
            }
        } catch (t: Throwable) {
            // revert
            if (inPantry) dao.removeFromPantry(ingredientId)
            else dao.addToPantry(PantryItemEntity(ingredientId))
            throw t
        }
    }
}
