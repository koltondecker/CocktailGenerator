package com.koltondecker.cocktailgenerator.data.repository

import com.koltondecker.cocktailgenerator.data.remote.dto.CocktailDetailDto
import com.koltondecker.cocktailgenerator.data.remote.dto.CocktailMatchDto
import com.koltondecker.cocktailgenerator.domain.model.CocktailDetail
import com.koltondecker.cocktailgenerator.domain.model.CocktailMatch
import com.koltondecker.cocktailgenerator.domain.model.MatchFilters
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.add
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CocktailsRepository @Inject constructor(
    private val client: SupabaseClient,
) {
    /**
     * Run the `match_cocktails` RPC and return the caller's pantry-relative
     * results. RLS scopes the pantry to `auth.uid()`; no explicit user id
     * needs to travel with the call.
     *
     * With [missingAllowed] >= a large number, the RPC returns the full
     * catalog with per-row missing counts, letting the UI show all three
     * tabs (Can make / 1 away / All) from a single fetch.
     */
    suspend fun match(
        missingAllowed: Int = LARGE_MISSING_ALLOWED,
        filters: MatchFilters = MatchFilters(),
    ): List<CocktailMatch> {
        val params = buildJsonObject {
            put("missing_allowed", missingAllowed)
            put("filters", filters.toJson())
        }
        val rows: List<CocktailMatchDto> = client.postgrest
            .rpc(function = "match_cocktails", parameters = params)
            .decodeList()
        return rows.map { it.toDomain() }
    }

    /**
     * Load one cocktail plus its ingredient rows in a single embedded-resource
     * query. The `cocktail_ingredients(...)` join follows the FK; the nested
     * `ingredient:ingredients(...)` follows the ingredient FK so each row
     * carries the ingredient name without a second round-trip.
     */
    suspend fun getCocktailDetail(cocktailId: Long): CocktailDetail {
        val dto = client.postgrest["cocktails"]
            .select(
                columns = Columns.raw(
                    """
                    id,name,slug,description,glass,method,garnish,instructions,
                    difficulty,abv_estimate,flavor_tags,source_url,source_name,image_url,
                    cocktail_ingredients(
                        ingredient_id,quantity,unit,is_optional,position,
                        ingredient:ingredients(id,name,category_id)
                    )
                    """.trimIndent().replace("\n", " ")
                )
            ) {
                filter { eq("id", cocktailId) }
            }
            .decodeSingle<CocktailDetailDto>()
        return dto.toDomain()
    }

    companion object {
        /**
         * "Effectively unlimited" — no realistic cocktail has more ingredients
         * than this, so passing it as `missing_allowed` disables the ceiling.
         */
        const val LARGE_MISSING_ALLOWED: Int = 999
    }
}

private fun MatchFilters.toJson(): JsonObject = buildJsonObject {
    if (baseSpiritIds.isNotEmpty()) {
        put("base_spirit_ids", buildJsonArray { baseSpiritIds.forEach { add(it) } })
    }
    if (flavorTags.isNotEmpty()) {
        put("flavor_tags", buildJsonArray { flavorTags.forEach { add(it) } })
    }
    maxDifficulty?.let { put("max_difficulty", it) }
    minAbv?.let { put("min_abv", it) }
    maxAbv?.let { put("max_abv", it) }
}
