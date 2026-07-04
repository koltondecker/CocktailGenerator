package com.koltondecker.cocktailgenerator.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.koltondecker.cocktailgenerator.domain.model.MatchFilters
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BrowseFilterSheet(
    current: MatchFilters,
    onApply: (MatchFilters) -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Local editable copies so the sheet feels responsive; parent gets the
    // final value only on Apply.
    var maxDifficulty by remember { mutableStateOf(current.maxDifficulty ?: 5) }
    var abvRange by remember {
        val lo = current.minAbv?.toFloat() ?: 0f
        val hi = current.maxAbv?.toFloat() ?: 40f
        mutableStateOf(lo..hi)
    }

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)) {
            Text("Filters", style = MaterialTheme.typography.headlineLarge)
            Spacer(Modifier.height(16.dp))

            Text("Max difficulty: $maxDifficulty", style = MaterialTheme.typography.titleLarge)
            Slider(
                value = maxDifficulty.toFloat(),
                onValueChange = { maxDifficulty = it.roundToInt().coerceIn(1, 5) },
                valueRange = 1f..5f,
                steps = 3, // 1..2..3..4..5 -> 3 stops between
            )

            Spacer(Modifier.height(16.dp))
            Text(
                "ABV: ${abvRange.start.roundToInt()}% – ${abvRange.endInclusive.roundToInt()}%",
                style = MaterialTheme.typography.titleLarge,
            )
            RangeSlider(
                value = abvRange,
                onValueChange = { abvRange = it },
                valueRange = 0f..50f,
                steps = 49,
            )

            Spacer(Modifier.height(24.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp, Arrangement.End),
            ) {
                TextButton(onClick = {
                    onApply(MatchFilters())
                }) { Text("Reset") }
                Button(onClick = {
                    onApply(
                        current.copy(
                            maxDifficulty = maxDifficulty.takeIf { it < 5 },
                            minAbv = abvRange.start.takeIf { it > 0f }?.toDouble(),
                            maxAbv = abvRange.endInclusive.takeIf { it < 50f }?.toDouble(),
                        )
                    )
                }) { Text("Apply") }
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}
