package com.koltondecker.cocktailgenerator.domain.model

data class CocktailDetail(
    val id: Long,
    val name: String,
    val slug: String,
    val description: String?,
    val glass: String?,
    val method: String?,
    val garnish: String?,
    val instructions: String?,
    val difficulty: Int?,
    val abvEstimate: Double?,
    val flavorTags: List<String>,
    val sourceUrl: String?,
    val sourceName: String?,
    val imageUrl: String?,
    val ingredients: List<CocktailIngredient>,
)

data class CocktailIngredient(
    val ingredientId: Long,
    val name: String,
    val quantity: Double?,
    val unit: String?,
    val isOptional: Boolean,
    val position: Int,
) {
    fun display(): String = buildString {
        val q = quantity?.let { if (it % 1.0 == 0.0) it.toInt().toString() else it.toString() }
        if (q != null) append(q).append(' ')
        if (!unit.isNullOrBlank()) append(unit).append(' ')
        append(name)
        if (isOptional) append(" (optional)")
    }.trim()
}
