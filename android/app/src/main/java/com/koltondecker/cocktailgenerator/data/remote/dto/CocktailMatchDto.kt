package com.koltondecker.cocktailgenerator.data.remote.dto

import com.koltondecker.cocktailgenerator.domain.model.CocktailMatch
import com.koltondecker.cocktailgenerator.domain.model.MissingIngredient
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CocktailMatchDto(
    val id: Long,
    val name: String,
    val slug: String,
    @SerialName("image_url") val imageUrl: String? = null,
    val difficulty: Int? = null,
    @SerialName("abv_estimate") val abvEstimate: Double? = null,
    @SerialName("flavor_tags") val flavorTags: List<String> = emptyList(),
    @SerialName("missing_count") val missingCount: Int,
    @SerialName("missing_ingredients") val missingIngredients: List<MissingIngredientDto> = emptyList(),
) {
    fun toDomain(): CocktailMatch = CocktailMatch(
        id = id,
        name = name,
        slug = slug,
        imageUrl = imageUrl,
        difficulty = difficulty,
        abvEstimate = abvEstimate,
        flavorTags = flavorTags,
        missingCount = missingCount,
        missingIngredients = missingIngredients.map { it.toDomain() },
    )
}

@Serializable
data class MissingIngredientDto(
    val id: Long,
    val name: String,
) {
    fun toDomain() = MissingIngredient(id = id, name = name)
}
