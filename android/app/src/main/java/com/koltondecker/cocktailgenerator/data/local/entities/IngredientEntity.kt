package com.koltondecker.cocktailgenerator.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.koltondecker.cocktailgenerator.domain.model.Ingredient

@Entity(
    tableName = "ingredients",
    foreignKeys = [
        ForeignKey(
            entity = IngredientCategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.RESTRICT,
        ),
    ],
    indices = [Index("categoryId"), Index("isCommon")],
)
data class IngredientEntity(
    @PrimaryKey val id: Long,
    val name: String,
    val categoryId: Int,
    val isCommon: Boolean,
    val iconUrl: String? = null,
) {
    fun toDomain(): Ingredient = Ingredient(
        id = id,
        name = name,
        categoryId = categoryId,
        isCommon = isCommon,
        iconUrl = iconUrl,
    )
}
