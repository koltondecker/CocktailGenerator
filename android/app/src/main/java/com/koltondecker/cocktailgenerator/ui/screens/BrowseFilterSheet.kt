package com.koltondecker.cocktailgenerator.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.koltondecker.cocktailgenerator.domain.model.Ingredient
import com.koltondecker.cocktailgenerator.domain.model.MatchFilters
import com.koltondecker.cocktailgenerator.domain.model.SortMode

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun BrowseFilterSheet(
    current: MatchFilters,
    spirits: List<Ingredient>,
    methods: List<String>,
    onApply: (MatchFilters) -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Editable copies — parent only sees the final MatchFilters on Apply.
    var baseSpiritIds by remember { mutableStateOf(current.baseSpiritIds.toSet()) }
    var selectedMethods by remember { mutableStateOf(current.methods.toSet()) }
    var favoritesOnly by remember { mutableStateOf(current.favoritesOnly) }
    var sortBy by remember { mutableStateOf(current.sortBy) }

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 8.dp),
        ) {
            Text("Filters", style = MaterialTheme.typography.headlineLarge)
            Spacer(Modifier.height(16.dp))

            // -- Base spirit -----------------------------------------------
            if (spirits.isNotEmpty()) {
                SectionLabel("Base spirit")
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    spirits.forEach { s ->
                        val selected = s.id in baseSpiritIds
                        FilterChip(
                            selected = selected,
                            onClick = {
                                baseSpiritIds = if (selected) baseSpiritIds - s.id else baseSpiritIds + s.id
                            },
                            label = { Text(s.name) },
                            shape = RoundedCornerShape(50),
                        )
                    }
                }
                Spacer(Modifier.height(20.dp))
            }

            // -- Method ----------------------------------------------------
            if (methods.isNotEmpty()) {
                SectionLabel("Method")
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    methods.forEach { m ->
                        val selected = m in selectedMethods
                        FilterChip(
                            selected = selected,
                            onClick = {
                                selectedMethods = if (selected) selectedMethods - m else selectedMethods + m
                            },
                            label = { Text(m.replaceFirstChar { it.uppercase() }) },
                            shape = RoundedCornerShape(50),
                        )
                    }
                }
                Spacer(Modifier.height(20.dp))
            }

            // -- Favorites only --------------------------------------------
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    SectionLabel("Favorites only")
                    Text(
                        "Only show cocktails you've hearted.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Switch(checked = favoritesOnly, onCheckedChange = { favoritesOnly = it })
            }
            Spacer(Modifier.height(20.dp))

            // -- Sort ------------------------------------------------------
            SectionLabel("Sort by")
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                SortMode.entries.forEach { s ->
                    FilterChip(
                        selected = s == sortBy,
                        onClick = { sortBy = s },
                        label = { Text(s.label) },
                        shape = RoundedCornerShape(50),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        ),
                    )
                }
            }

            Spacer(Modifier.height(24.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.End),
            ) {
                TextButton(onClick = {
                    onApply(MatchFilters(query = current.query))
                }) { Text("Reset") }
                Button(onClick = {
                    onApply(
                        current.copy(
                            baseSpiritIds = baseSpiritIds.toList(),
                            methods = selectedMethods.toList(),
                            favoritesOnly = favoritesOnly,
                            sortBy = sortBy,
                        )
                    )
                }) { Text("Apply") }
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.titleLarge,
        modifier = Modifier.padding(bottom = 10.dp),
    )
}
