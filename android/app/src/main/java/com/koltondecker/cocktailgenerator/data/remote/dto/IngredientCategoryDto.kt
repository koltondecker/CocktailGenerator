package com.koltondecker.cocktailgenerator.data.remote.dto

import com.koltondecker.cocktailgenerator.data.local.entities.IngredientCategoryEntity
import com.koltondecker.cocktailgenerator.domain.model.IngredientCategory
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class IngredientCategoryDto(
    val id: Int,
    val name: String,
    @SerialName("sort_order") val sortOrder: Int,
) {
    fun toEntity(): IngredientCategoryEntity =
        IngredientCategoryEntity(id = id, name = name, sortOrder = sortOrder)

    fun toDomain(): IngredientCategory =
        IngredientCategory(id = id, name = name, sortOrder = sortOrder)
}
