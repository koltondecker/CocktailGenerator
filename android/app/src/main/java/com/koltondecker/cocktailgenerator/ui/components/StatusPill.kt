package com.koltondecker.cocktailgenerator.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.koltondecker.cocktailgenerator.domain.model.CocktailMatch
import com.koltondecker.cocktailgenerator.ui.theme.ReadyGreen

/**
 * Small rounded status pill — "Ready to pour" / "1 away" / "N needed".
 * Solid tinted background so it stays legible on top of photos.
 */
@Composable
fun StatusPill(match: CocktailMatch, modifier: Modifier = Modifier) {
    val (label, bg, fg) = when {
        match.canMakeNow -> Triple("Ready to pour", ReadyGreen, Color(0xFF07301E))
        match.missingCount == 1 -> Triple("1 away", MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.onPrimary)
        else -> Triple("${match.missingCount} needed", Color(0xCC241B32), Color(0xFFB3A8C2))
    }
    Text(
        text = label,
        style = MaterialTheme.typography.labelMedium,
        color = fg,
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(bg)
            .padding(horizontal = 10.dp, vertical = 4.dp),
    )
}
