package com.koltondecker.cocktailgenerator.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.koltondecker.cocktailgenerator.domain.model.CocktailMatch
import com.koltondecker.cocktailgenerator.ui.components.CocktailPosterCard
import com.koltondecker.cocktailgenerator.ui.components.LocalSnackbarHostState
import com.koltondecker.cocktailgenerator.ui.components.StatusPill
import com.koltondecker.cocktailgenerator.ui.theme.GradA
import com.koltondecker.cocktailgenerator.ui.theme.GradB
import com.koltondecker.cocktailgenerator.ui.theme.GradC
import com.koltondecker.cocktailgenerator.ui.theme.ScrimBottom
import com.koltondecker.cocktailgenerator.ui.theme.ScrimTop
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onOpenCocktail: (Long) -> Unit,
    onSignOut: () -> Unit,
    vm: HomeViewModel = hiltViewModel(),
) {
    val ui by vm.state.collectAsStateWithLifecycle()
    val snackbarHost = LocalSnackbarHostState.current

    LaunchedEffect(ui.errorMessage) {
        val msg = ui.errorMessage ?: return@LaunchedEffect
        snackbarHost.showSnackbar(msg)
        vm.clearError()
    }

    PullToRefreshBox(
        isRefreshing = ui.refreshing,
        onRefresh = vm::refresh,
        modifier = Modifier.fillMaxSize(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
        ) {
            GreetingHeader(onSignOut = onSignOut)

            when {
                ui.loading -> Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp),
                    contentAlignment = Alignment.Center,
                ) { CircularProgressIndicator() }

                ui.canMake.isEmpty() && ui.almost.isEmpty() -> EmptyHome()

                else -> {
                    StatRow(readyCount = ui.canMake.size, almostCount = ui.almost.size)
                    Spacer(Modifier.height(20.dp))

                    val hero = ui.canMake.firstOrNull() ?: ui.almost.firstOrNull()
                    if (hero != null) {
                        SectionTitle("Tonight's pick")
                        HeroCard(hero, onClick = { onOpenCocktail(hero.id) })
                        Spacer(Modifier.height(24.dp))
                    }

                    if (ui.canMake.size > 1 || (hero != null && !hero.canMakeNow)) {
                        val rail = if (hero != null && hero.canMakeNow) ui.canMake.drop(1) else ui.canMake
                        if (rail.isNotEmpty()) {
                            SectionTitle("On the menu", "Everything your bar can pour right now")
                            PosterRail(rail, onOpenCocktail)
                            Spacer(Modifier.height(24.dp))
                        }
                    }

                    if (ui.almost.isNotEmpty()) {
                        SectionTitle("So close", "One bottle away — worth the trip")
                        PosterRail(ui.almost, onOpenCocktail)
                        Spacer(Modifier.height(24.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun GreetingHeader(onSignOut: () -> Unit) {
    val greeting = remember {
        when (Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) {
            in 5..11 -> "Good morning ☀️"
            in 12..16 -> "Good afternoon 🍋"
            else -> "Good evening 🍸"
        }
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 20.dp, top = 20.dp, end = 8.dp, bottom = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column {
            Text(
                greeting,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text("What are we\nmixing tonight?", style = MaterialTheme.typography.displayLarge)
        }
        IconButton(onClick = onSignOut) {
            Icon(
                Icons.AutoMirrored.Filled.Logout,
                contentDescription = "Sign out",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun StatRow(readyCount: Int, almostCount: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        StatTile(
            value = "$readyCount",
            label = "ready to pour",
            brush = Brush.linearGradient(listOf(GradA, GradB)),
            modifier = Modifier.weight(1f),
        )
        StatTile(
            value = "$almostCount",
            label = "one bottle away",
            brush = Brush.linearGradient(listOf(GradB, GradC)),
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun StatTile(value: String, label: String, brush: Brush, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(MaterialTheme.shapes.medium)
            .background(brush)
            .padding(horizontal = 18.dp, vertical = 16.dp),
    ) {
        Text(value, style = MaterialTheme.typography.displaySmall, color = Color.White)
        Text(label, style = MaterialTheme.typography.labelLarge, color = Color(0xE6FFFFFF))
    }
}

@Composable
private fun SectionTitle(title: String, subtitle: String? = null) {
    Column(modifier = Modifier.padding(horizontal = 20.dp)) {
        Text(title, style = MaterialTheme.typography.headlineLarge)
        if (subtitle != null) {
            Text(
                subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
    Spacer(Modifier.height(12.dp))
}

@Composable
private fun HeroCard(match: CocktailMatch, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .aspectRatio(16f / 10f)
            .clip(MaterialTheme.shapes.large)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable(onClick = onClick),
    ) {
        if (match.imageUrl != null) {
            AsyncImage(
                model = match.imageUrl,
                contentDescription = match.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(0.35f to ScrimTop, 1f to ScrimBottom)),
        )
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .padding(20.dp),
        ) {
            StatusPill(match)
            Spacer(Modifier.height(8.dp))
            Text(
                match.name,
                style = MaterialTheme.typography.displaySmall,
                color = Color.White,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun PosterRail(items: List<CocktailMatch>, onOpenCocktail: (Long) -> Unit) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(items = items, key = { it.id }) { match ->
            CocktailPosterCard(match = match, onClick = { onOpenCocktail(match.id) })
        }
    }
}

@Composable
private fun EmptyHome() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 32.dp),
    ) {
        Text("🍹", style = MaterialTheme.typography.displayLarge)
        Spacer(Modifier.height(8.dp))
        Text("Your bar is empty", style = MaterialTheme.typography.headlineLarge)
        Text(
            "Head to Pantry and check off what's on your shelf — cocktails you can pour will show up here.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
