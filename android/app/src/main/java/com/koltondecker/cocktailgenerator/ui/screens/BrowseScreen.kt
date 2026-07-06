package com.koltondecker.cocktailgenerator.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.LocalBar
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.koltondecker.cocktailgenerator.domain.model.CocktailMatch
import com.koltondecker.cocktailgenerator.ui.components.LocalSnackbarHostState
import com.koltondecker.cocktailgenerator.ui.components.StatusPill

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BrowseScreen(
    onOpenCocktail: (Long) -> Unit,
    vm: BrowseViewModel = hiltViewModel(),
) {
    val ui by vm.state.collectAsStateWithLifecycle()
    val snackbarHost = LocalSnackbarHostState.current

    LaunchedEffect(ui.errorMessage) {
        val msg = ui.errorMessage ?: return@LaunchedEffect
        snackbarHost.showSnackbar(msg)
        vm.clearError()
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text("Browse", style = MaterialTheme.typography.displayLarge)
            BadgedBox(badge = {
                val n = ui.filters.activeCount
                if (n > 0) Badge { Text(n.toString()) }
            }) {
                FilledIconButton(
                    onClick = vm::openFilters,
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = MaterialTheme.colorScheme.primary,
                    ),
                ) {
                    Icon(Icons.Filled.FilterList, contentDescription = "Filters")
                }
            }
        }

        TabRow(
            selectedTabIndex = ui.tab.ordinal,
            containerColor = MaterialTheme.colorScheme.background,
        ) {
            BrowseTab.entries.forEach { tab ->
                Tab(
                    selected = ui.tab == tab,
                    onClick = { vm.selectTab(tab) },
                    text = { Text(tab.label) },
                )
            }
        }

        PullToRefreshBox(
            isRefreshing = ui.refreshing,
            onRefresh = vm::refresh,
            modifier = Modifier.fillMaxSize(),
        ) {
            val visible = ui.visibleResults
            when {
                ui.loading -> CenterBox { CircularProgressIndicator() }
                visible.isEmpty() -> CenterBox {
                    Text(
                        emptyMessageFor(ui.tab),
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(24.dp),
                    )
                }
                else -> CocktailList(results = visible, onOpenCocktail = onOpenCocktail)
            }
        }
    }

    if (ui.filterSheetOpen) {
        BrowseFilterSheet(
            current = ui.filters,
            onApply = vm::applyFilters,
            onDismiss = vm::closeFilters,
        )
    }
}

@Composable
private fun CocktailList(
    results: List<CocktailMatch>,
    onOpenCocktail: (Long) -> Unit,
) {
    LazyColumn(
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize(),
    ) {
        items(items = results, key = { it.id }) { match ->
            CocktailRow(match = match, onClick = { onOpenCocktail(match.id) })
        }
    }
}

@Composable
private fun CocktailRow(match: CocktailMatch, onClick: () -> Unit) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .clickable(onClick = onClick),
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(76.dp)
                    .clip(MaterialTheme.shapes.small)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
            ) {
                if (match.imageUrl != null) {
                    AsyncImage(
                        model = match.imageUrl,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                    )
                } else {
                    Icon(
                        Icons.Filled.LocalBar,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(20.dp),
                    )
                }
            }
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    match.name,
                    style = MaterialTheme.typography.titleLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.padding(top = 6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    StatusPill(match)
                    match.difficulty?.let {
                        Text(
                            "  ·  ${"🍸".repeat(it.coerceIn(1, 5))}",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                        )
                    }
                }
                if (!match.canMakeNow && match.missingIngredients.isNotEmpty()) {
                    Text(
                        "Missing: " + match.missingIngredients.joinToString { it.name },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun CenterBox(content: @Composable () -> Unit) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { content() }
}

private fun emptyMessageFor(tab: BrowseTab): String = when (tab) {
    BrowseTab.CAN_MAKE -> "Nothing you can pour yet — stock a few pantry items to unlock cocktails."
    BrowseTab.ONE_AWAY -> "Nothing one ingredient away right now. Try adding a base spirit."
    BrowseTab.ALL -> "No cocktails match the current filters."
}
