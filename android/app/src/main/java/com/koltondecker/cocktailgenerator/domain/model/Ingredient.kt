package com.koltondecker.cocktailgenerator.domain.model

data class Ingredient(
    val id: Long,
    val name: String,
    val categoryId: Int,
    val isCommon: Boolean,
)
