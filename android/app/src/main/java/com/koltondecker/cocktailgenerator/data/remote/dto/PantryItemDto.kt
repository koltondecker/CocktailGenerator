package com.koltondecker.cocktailgenerator.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PantryItemDto(
    @SerialName("user_id") val userId: String,
    @SerialName("ingredient_id") val ingredientId: Long,
)
