package com.koltondecker.cocktailgenerator.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pantry_items")
data class PantryItemEntity(
    @PrimaryKey val ingredientId: Long,
)
