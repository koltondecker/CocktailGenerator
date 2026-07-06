package com.koltondecker.cocktailgenerator.ui.components

import androidx.compose.ui.graphics.Color

/**
 * Per-category personality for the pantry: an emoji for headers and a tint
 * for chips. Keyed loosely on the category names seeded in the DB so a new
 * category degrades gracefully to the neutral style.
 */
fun categoryEmoji(name: String): String = when (name.lowercase()) {
    "spirits" -> "🥃"          // 🥃
    "liqueurs" -> "🍸"         // 🍸
    "wine & fortified" -> "🍷" // 🍷
    "bitters" -> "🧪"          // 🧪
    "mixers" -> "🥤"           // 🥤
    "juices" -> "🍋"           // 🍋
    "fresh" -> "🌿"            // 🌿
    "syrups" -> "🍯"           // 🍯
    "garnishes" -> "🍒"        // 🍒
    else -> "✨"                     // ✨
}

fun categoryTint(name: String): Color = when (name.lowercase()) {
    "spirits" -> Color(0xFFFFB74A)
    "liqueurs" -> Color(0xFFC77DFF)
    "wine & fortified" -> Color(0xFFE0559C)
    "bitters" -> Color(0xFFE8734A)
    "mixers" -> Color(0xFF5CC8FF)
    "juices" -> Color(0xFFFFD54F)
    "fresh" -> Color(0xFF7BE388)
    "syrups" -> Color(0xFFFF8FAB)
    "garnishes" -> Color(0xFFFF6B81)
    else -> Color(0xFF4DD6C1)
}
