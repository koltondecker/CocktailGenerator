package com.koltondecker.cocktailgenerator.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.koltondecker.cocktailgenerator.data.local.entities.IngredientCategoryEntity
import com.koltondecker.cocktailgenerator.data.local.entities.IngredientEntity
import com.koltondecker.cocktailgenerator.data.local.entities.PantryItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PantryDao {

    // -- observers ---------------------------------------------------------
    @Query("SELECT * FROM ingredient_categories ORDER BY sortOrder ASC")
    fun observeCategories(): Flow<List<IngredientCategoryEntity>>

    @Query("SELECT * FROM ingredients WHERE isCommon = 1 ORDER BY name ASC")
    fun observeCommonIngredients(): Flow<List<IngredientEntity>>

    @Query("SELECT ingredientId FROM pantry_items")
    fun observePantryIds(): Flow<List<Long>>

    // -- catalog upserts ---------------------------------------------------
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertCategories(rows: List<IngredientCategoryEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertIngredients(rows: List<IngredientEntity>)

    // -- pantry mutations --------------------------------------------------
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addToPantry(item: PantryItemEntity)

    @Query("DELETE FROM pantry_items WHERE ingredientId = :id")
    suspend fun removeFromPantry(id: Long)

    @Query("DELETE FROM pantry_items")
    suspend fun clearPantry()

    @Transaction
    suspend fun replacePantry(rows: List<PantryItemEntity>) {
        clearPantry()
        insertAllPantry(rows)
    }

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllPantry(rows: List<PantryItemEntity>)
}
