package com.koltondecker.cocktailgenerator.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.koltondecker.cocktailgenerator.domain.model.CocktailMatch
import com.koltondecker.cocktailgenerator.ui.components.LocalSnackbarHostState

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
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text("Browse", style = MaterialTheme.typography.headlineLarge)
            BadgedBox(badge = {
                val n = ui.filters.activeCount
                if (n > 0) Badge { Text(n.toString()) }
            }) {
                FilledIconButton(onClick = vm::openFilters) {
                    Icon(Icons.Filled.FilterList, contentDescription = "Filters")
                }
            }
        }

        TabRow(selectedTabIndex = ui.tab.ordinal) {
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
                ui.loading -> LoadingBox()
                visible.isEmpty() -> EmptyBox(emptyMessageFor(ui.tab))
                else -> CocktailList(
                    results = visible,
                    onOpenCocktail = onOpenCocktail,
                )
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
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize(),
    ) {
        items(items = results, key = { it.id }) { match ->
            CocktailCard(match = match, onClick = { onOpenCocktail(match.id) })
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CocktailCard(match: CocktailMatch, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Row(modifier = Modifier.padding(12.dp)) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(8.dp)),
            ) {
                if (match.imageUrl != null) {
                    AsyncImage(
                        model = match.imageUrl,
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxSize()
                            .aspectRatio(1f),
                    )
                }
            }
            Spacer(Modifier.size(12.dp))
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(match.name, style = MaterialTheme.typography.titleLarge)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (match.canMakeNow) {
                        AssistChip(
                            onClick = onClick,
                            label = { Text("You can make this") },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                labelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            ),
                        )
                    } else {
                        AssistChip(
                            onClick = onClick,
                            label = { Text("Need ${match.missingCount}") },
                        )
                    }
                    match.difficulty?.let {
                        Spacer(Modifier.size(6.dp))
                        Text("• Difficulty $it/5", style = MaterialTheme.typography.labelLarge)
                    }
                }
                if (!match.canMakeNow && match.missingIngredients.isNotEmpty()) {
                    Spacer(Modifier.size(4.dp))
                    Text(
                        text = "Missing: " + match.missingIngredients.joinToString { it.name },
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun LoadingBox() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
private fun EmptyBox(message: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(
            message,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(24.dp),
        )
    }
}

private fun emptyMessageFor(tab: BrowseTab): String = when (tab) {
    BrowseTab.CAN_MAKE -> "Nothing you can make yet — add a few pantry items to unlock cocktails."
    BrowseTab.ONE_AWAY -> "Nothing one ingredient away right now. Try adding a base spirit."
    BrowseTab.ALL      -> "No cocktails match the current filters."
}
