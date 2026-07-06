package com.koltondecker.cocktailgenerator.domain.model

/**
 * One row returned by the `match_cocktails` RPC. [missingCount] is the number
 * of required ingredients absent from the caller's pantry; [missingIngredients]
 * lists them by name so the UI can render a "you need X" chip.
 */
data class CocktailMatch(
    val id: Long,
    val name: String,
    val slug: String,
    val imageUrl: String?,
    val method: String?,
    val difficulty: Int?,
    val abvEstimate: Double?,
    val flavorTags: List<String>,
    val missingCount: Int,
    val missingIngredients: List<MissingIngredient>,
) {
    val canMakeNow: Boolean get() = missingCount == 0
}

data class MissingIngredient(
    val id: Long,
    val name: String,
)
