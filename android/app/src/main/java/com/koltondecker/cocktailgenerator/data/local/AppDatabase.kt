package com.koltondecker.cocktailgenerator.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.koltondecker.cocktailgenerator.data.local.dao.PantryDao
import com.koltondecker.cocktailgenerator.data.local.entities.IngredientCategoryEntity
import com.koltondecker.cocktailgenerator.data.local.entities.IngredientEntity
import com.koltondecker.cocktailgenerator.data.local.entities.PantryItemEntity

@Database(
    entities = [
        IngredientCategoryEntity::class,
        IngredientEntity::class,
        PantryItemEntity::class,
    ],
    version = 1,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun pantryDao(): PantryDao
}
