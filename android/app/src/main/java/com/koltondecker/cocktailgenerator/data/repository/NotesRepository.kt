package com.koltondecker.cocktailgenerator.data.repository

import com.koltondecker.cocktailgenerator.data.remote.dto.UserNoteDto
import com.koltondecker.cocktailgenerator.domain.model.UserNote
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotesRepository @Inject constructor(
    private val client: SupabaseClient,
) {
    suspend fun getNote(cocktailId: Long): UserNote? = client.postgrest["user_notes"]
        .select { filter { eq("cocktail_id", cocktailId) } }
        .decodeList<UserNoteDto>()
        .firstOrNull()
        ?.toDomain()

    /**
     * Save (upsert) a note. If [body] is blank and [rating] is null the row is
     * deleted instead — empty notes shouldn't take up rows.
     */
    suspend fun saveNote(cocktailId: Long, body: String, rating: Int?) {
        val userId = client.auth.currentUserOrNull()?.id
            ?: throw IllegalStateException("No authenticated user for note save")
        val trimmed = body.trim()
        if (trimmed.isEmpty() && rating == null) {
            client.postgrest["user_notes"].delete {
                filter { eq("cocktail_id", cocktailId) }
            }
            return
        }
        client.postgrest["user_notes"].upsert(
            UserNoteDto(
                userId = userId,
                cocktailId = cocktailId,
                body = trimmed.ifEmpty { null },
                personalRating = rating,
            ),
        )
    }
}
