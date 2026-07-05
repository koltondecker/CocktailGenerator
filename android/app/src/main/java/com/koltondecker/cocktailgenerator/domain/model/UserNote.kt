package com.koltondecker.cocktailgenerator.domain.model

data class UserNote(
    val cocktailId: Long,
    val body: String?,
    val personalRating: Int?,
)
