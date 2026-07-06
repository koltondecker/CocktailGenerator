package com.koltondecker.cocktailgenerator.data.repository

import com.koltondecker.cocktailgenerator.data.remote.dto.UserFavoriteDto
import com.koltondecker.cocktailgenerator.domain.model.CocktailMatch
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FavoritesRepository @Inject constructor(
    private val client: SupabaseClient,
) {
    /** RLS scopes results to the caller — one point query per cocktail. */
    suspend fun isFavorite(cocktailId: Long): Boolean {
        val rows = client.postgrest["user_favorites"]
            .select { filter { eq("cocktail_id", cocktailId) } }
            .decodeList<UserFavoriteDto>()
        return rows.isNotEmpty()
    }

    /** Load every favorite for the current user — used to power the Browse
     *  "Favorites only" filter. RLS makes this scope to auth.uid() implicitly. */
    suspend fun favoriteIds(): Set<Long> = client.postgrest["user_favorites"]
        .select()
        .decodeList<UserFavoriteDto>()
        .mapTo(mutableSetOf()) { it.cocktailId }

    /**
     * One embedded-resource query returning every favorited cocktail with its
     * full row inline. Doesn't rely on the `match_cocktails` RPC — so a
     * favorite still surfaces even if it has no required ingredients or if
     * the pantry is empty. Missing-count is set to 0 so the poster card
     * renders cleanly; the Favorites screen hides the "ready" pill anyway.
     */
    suspend fun getFavoritedCocktails(): List<CocktailMatch> {
        val rows = client.postgrest["user_favorites"]
            .select(columns = Columns.raw("cocktail:cocktails(id,name,slug,image_url,method,difficulty,abv_estimate,flavor_tags)"))
            .decodeList<FavoriteWithCocktailDto>()
        return rows.mapNotNull { it.cocktail?.toMatch() }
    }

    suspend fun setFavorite(cocktailId: Long, favorited: Boolean) {
        val userId = client.auth.currentUserOrNull()?.id
            ?: throw IllegalStateException("No authenticated user for favorite toggle")
        if (favorited) {
            client.postgrest["user_favorites"].upsert(
                UserFavoriteDto(userId = userId, cocktailId = cocktailId),
            )
        } else {
            client.postgrest["user_favorites"].delete {
                filter { eq("cocktail_id", cocktailId) }
            }
        }
    }
}

@Serializable
private data class FavoriteWithCocktailDto(
    val cocktail: EmbeddedCocktailDto? = null,
)

@Serializable
private data class EmbeddedCocktailDto(
    val id: Long,
    val name: String,
    val slug: String,
    @SerialName("image_url") val imageUrl: String? = null,
    val method: String? = null,
    val difficulty: Int? = null,
    @SerialName("abv_estimate") val abvEstimate: Double? = null,
    @SerialName("flavor_tags") val flavorTags: List<String> = emptyList(),
) {
    fun toMatch() = CocktailMatch(
        id = id,
        name = name,
        slug = slug,
        imageUrl = imageUrl,
        method = method,
        difficulty = difficulty,
        abvEstimate = abvEstimate,
        flavorTags = flavorTags,
        missingCount = 0,
        missingIngredients = emptyList(),
    )
}
