package com.koltondecker.cocktailgenerator.domain.model

/**
 * Filter + sort state for the Browse screen. Fields marked "server-side" are
 * pushed down into the `match_cocktails` RPC's `filters` jsonb; the rest are
 * applied client-side on the returned list.
 */
data class MatchFilters(
    val baseSpiritIds: List<Long> = emptyList(),   // server-side
    val methods: List<String> = emptyList(),        // server-side (shaken/stirred/…)
    val favoritesOnly: Boolean = false,              // client-side
    val query: String = "",                          // client-side (name substring)
    val sortBy: SortMode = SortMode.MATCH,           // client-side
) {
    val isEmpty: Boolean
        get() = baseSpiritIds.isEmpty() &&
                methods.isEmpty() &&
                !favoritesOnly &&
                query.isBlank()

    /** Number of *active* filters — used to badge the filter button. Sort
     *  isn't a filter, so it doesn't count. */
    val activeCount: Int
        get() = listOf(
            baseSpiritIds.isNotEmpty(),
            methods.isNotEmpty(),
            favoritesOnly,
            query.isNotBlank(),
        ).count { it }
}

enum class SortMode(val label: String) {
    MATCH("Best match"),
    NAME_ASC("A → Z"),
    NAME_DESC("Z → A"),
    SIMPLEST("Fewest ingredients"),
}
