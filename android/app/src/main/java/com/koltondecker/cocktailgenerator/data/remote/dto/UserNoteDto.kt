package com.koltondecker.cocktailgenerator.data.remote.dto

import com.koltondecker.cocktailgenerator.domain.model.UserNote
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserNoteDto(
    @SerialName("user_id") val userId: String,
    @SerialName("cocktail_id") val cocktailId: Long,
    val body: String? = null,
    @SerialName("personal_rating") val personalRating: Int? = null,
) {
    fun toDomain(): UserNote = UserNote(
        cocktailId = cocktailId,
        body = body,
        personalRating = personalRating,
    )
}
