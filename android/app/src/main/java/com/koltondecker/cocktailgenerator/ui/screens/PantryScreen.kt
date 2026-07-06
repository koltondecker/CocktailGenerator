package com.koltondecker.cocktailgenerator.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.koltondecker.cocktailgenerator.domain.model.Ingredient
import com.koltondecker.cocktailgenerator.ui.components.LocalSnackbarHostState
import com.koltondecker.cocktailgenerator.ui.components.categoryEmoji
import com.koltondecker.cocktailgenerator.ui.components.categoryTint

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantryScreen(vm: PantryViewModel = hiltViewModel()) {
    val ui by vm.state.collectAsStateWithLifecycle()
    val snackbarHost = LocalSnackbarHostState.current

    LaunchedEffect(ui.errorMessage) {
        val msg = ui.errorMessage ?: return@LaunchedEffect
        snackbarHost.showSnackbar(msg)
        vm.dismissError()
    }

    val stocked = ui.groups.sumOf { g -> g.items.count { it.inPantry } }

    Column(modifier = Modifier.fillMaxSize()) {
        Header(stocked = stocked)
        SearchField(
            query = ui.query,
            onQueryChange = vm::onQueryChange,
            modifier = Modifier.padding(horizontal = 20.dp),
        )
        Spacer(Modifier.height(8.dp))

        PullToRefreshBox(
            isRefreshing = ui.refreshing,
            onRefresh = vm::refresh,
            modifier = Modifier.fillMaxSize(),
        ) {
            when {
                ui.loading -> CenterBox { CircularProgressIndicator() }
                ui.groups.isEmpty() && ui.query.isBlank() ->
                    CenterBox { Text("No ingredients yet — try again in a moment.") }
                ui.groups.isEmpty() ->
                    CenterBox { Text("Nothing matches '${ui.query}'.") }
                else -> PantryList(groups = ui.groups, onToggle = vm::setInPantry)
            }
        }
    }
}

@Composable
private fun Header(stocked: Int) {
    Column(modifier = Modifier.padding(start = 20.dp, top = 20.dp, end = 20.dp)) {
        Text("My Bar", style = MaterialTheme.typography.displayLarge)
        Text(
            text = if (stocked == 0) "Tap what's on your shelf to unlock cocktails."
                   else "$stocked ingredient${if (stocked == 1) "" else "s"} stocked 🍾",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun SearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        placeholder = { Text("Search your shelf") },
        leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
        singleLine = true,
        shape = RoundedCornerShape(50),
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
        ),
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 12.dp),
    )
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun PantryList(
    groups: List<PantryCategoryGroup>,
    onToggle: (Ingredient, Boolean) -> Unit,
) {
    LazyColumn(
        contentPadding = PaddingValues(vertical = 8.dp),
        modifier = Modifier.fillMaxSize(),
    ) {
        groups.forEach { group ->
            val tint = categoryTint(group.category.name)
            stickyHeader(key = "cat-${group.category.id}") {
                Surface(
                    color = MaterialTheme.colorScheme.background,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
                    ) {
                        Text(categoryEmoji(group.category.name), style = MaterialTheme.typography.titleLarge)
                        Spacer(Modifier.padding(start = 8.dp))
                        Text(
                            group.category.name,
                            style = MaterialTheme.typography.titleLarge,
                            modifier = Modifier.padding(start = 8.dp),
                        )
                        val have = group.items.count { it.inPantry }
                        if (have > 0) {
                            Text(
                                "  $have/${group.items.size}",
                                style = MaterialTheme.typography.labelLarge,
                                color = tint,
                            )
                        }
                    }
                }
            }
            items(items = listOf(group), key = { "chips-${group.category.id}" }) { g ->
                FlowRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    g.items.forEach { item ->
                        FilterChip(
                            selected = item.inPantry,
                            onClick = { onToggle(item.ingredient, !item.inPantry) },
                            label = { Text(item.ingredient.name) },
                            leadingIcon = item.ingredient.iconUrl?.let { url ->
                                {
                                    AsyncImage(
                                        model = url,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp),
                                    )
                                }
                            },
                            shape = RoundedCornerShape(50),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = tint.copy(alpha = 0.28f),
                                selectedLabelColor = MaterialTheme.colorScheme.onSurface,
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = item.inPantry,
                                borderColor = MaterialTheme.colorScheme.outline,
                                selectedBorderColor = tint,
                                selectedBorderWidth = 1.dp,
                            ),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CenterBox(content: @Composable () -> Unit) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { content() }
}
