package com.koltondecker.cocktailgenerator.data.remote.dto

import com.koltondecker.cocktailgenerator.data.local.entities.IngredientEntity
import com.koltondecker.cocktailgenerator.domain.model.Ingredient
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class IngredientDto(
    val id: Long,
    val name: String,
    @SerialName("category_id") val categoryId: Int,
    @SerialName("is_common") val isCommon: Boolean,
) {
    fun toEntity(): IngredientEntity =
        IngredientEntity(id = id, name = name, categoryId = categoryId, isCommon = isCommon)

    fun toDomain(): Ingredient =
        Ingredient(id = id, name = name, categoryId = categoryId, isCommon = isCommon)
}
