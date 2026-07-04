package com.koltondecker.cocktailgenerator.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.koltondecker.cocktailgenerator.domain.model.IngredientCategory

@Entity(tableName = "ingredient_categories")
data class IngredientCategoryEntity(
    @PrimaryKey val id: Int,
    val name: String,
    val sortOrder: Int,
) {
    fun toDomain(): IngredientCategory =
        IngredientCategory(id = id, name = name, sortOrder = sortOrder)
}
