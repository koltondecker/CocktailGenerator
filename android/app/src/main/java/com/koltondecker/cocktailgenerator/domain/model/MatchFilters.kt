package com.koltondecker.cocktailgenerator.domain.model

/**
 * Filter state that maps to the `filters` jsonb param on `match_cocktails`.
 * A null / empty field means "no filter" on that dimension.
 */
data class MatchFilters(
    val baseSpiritIds: List<Long> = emptyList(),
    val flavorTags: List<String> = emptyList(),
    val maxDifficulty: Int? = null,
    val minAbv: Double? = null,
    val maxAbv: Double? = null,
) {
    val isEmpty: Boolean
        get() = baseSpiritIds.isEmpty() &&
                flavorTags.isEmpty() &&
                maxDifficulty == null &&
                minAbv == null &&
                maxAbv == null

    val activeCount: Int
        get() = listOf(
            baseSpiritIds.isNotEmpty(),
            flavorTags.isNotEmpty(),
            maxDifficulty != null,
            minAbv != null,
            maxAbv != null,
        ).count { it }
}
