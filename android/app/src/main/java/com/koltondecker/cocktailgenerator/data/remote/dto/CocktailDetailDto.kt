package com.koltondecker.cocktailgenerator.data.remote.dto

import com.koltondecker.cocktailgenerator.domain.model.CocktailDetail
import com.koltondecker.cocktailgenerator.domain.model.CocktailIngredient
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CocktailDetailDto(
    val id: Long,
    val name: String,
    val slug: String,
    val description: String? = null,
    val glass: String? = null,
    val method: String? = null,
    val garnish: String? = null,
    val instructions: String? = null,
    val difficulty: Int? = null,
    @SerialName("abv_estimate") val abvEstimate: Double? = null,
    @SerialName("flavor_tags") val flavorTags: List<String> = emptyList(),
    @SerialName("source_url") val sourceUrl: String? = null,
    @SerialName("source_name") val sourceName: String? = null,
    @SerialName("image_url") val imageUrl: String? = null,
    @SerialName("cocktail_ingredients") val ingredients: List<CocktailIngredientRowDto> = emptyList(),
) {
    fun toDomain(): CocktailDetail = CocktailDetail(
        id = id,
        name = name,
        slug = slug,
        description = description,
        glass = glass,
        method = method,
        garnish = garnish,
        instructions = instructions,
        difficulty = difficulty,
        abvEstimate = abvEstimate,
        flavorTags = flavorTags,
        sourceUrl = sourceUrl,
        sourceName = sourceName,
        imageUrl = imageUrl,
        ingredients = ingredients
            .sortedBy { it.position }
            .map { it.toDomain() },
    )
}

@Serializable
data class CocktailIngredientRowDto(
    @SerialName("ingredient_id") val ingredientId: Long,
    val quantity: Double? = null,
    val unit: String? = null,
    @SerialName("is_optional") val isOptional: Boolean = false,
    val position: Int = 0,
    val ingredient: IngredientEmbedDto,
) {
    fun toDomain(): CocktailIngredient = CocktailIngredient(
        ingredientId = ingredientId,
        name = ingredient.name,
        quantity = quantity,
        unit = unit,
        isOptional = isOptional,
        position = position,
    )
}

@Serializable
data class IngredientEmbedDto(
    val id: Long,
    val name: String,
    @SerialName("category_id") val categoryId: Int,
)
