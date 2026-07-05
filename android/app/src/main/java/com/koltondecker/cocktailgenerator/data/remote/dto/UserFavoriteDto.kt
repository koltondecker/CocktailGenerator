package com.koltondecker.cocktailgenerator.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserFavoriteDto(
    @SerialName("user_id") val userId: String,
    @SerialName("cocktail_id") val cocktailId: Long,
)
