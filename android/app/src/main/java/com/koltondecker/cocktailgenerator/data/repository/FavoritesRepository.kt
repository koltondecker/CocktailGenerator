package com.koltondecker.cocktailgenerator.data.repository

import com.koltondecker.cocktailgenerator.data.remote.dto.UserFavoriteDto
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
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

    /** Load every favorite for the current user — used by Home + Favorites. */
    suspend fun favoriteIds(): Set<Long> = client.postgrest["user_favorites"]
        .select()
        .decodeList<UserFavoriteDto>()
        .mapTo(mutableSetOf()) { it.cocktailId }

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
