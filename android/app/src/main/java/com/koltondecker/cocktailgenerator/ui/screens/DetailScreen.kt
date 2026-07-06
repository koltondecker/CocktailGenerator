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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.koltondecker.cocktailgenerator.domain.model.CocktailDetail
import com.koltondecker.cocktailgenerator.ui.components.LocalSnackbarHostState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    onBack: () -> Unit,
    vm: DetailViewModel = hiltViewModel(),
) {
    val ui by vm.state.collectAsStateWithLifecycle()
    val snackbarHost = LocalSnackbarHostState.current

    LaunchedEffect(ui.errorMessage) {
        val msg = ui.errorMessage ?: return@LaunchedEffect
        snackbarHost.showSnackbar(msg)
        vm.clearError()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(ui.cocktail?.name ?: "") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = vm::toggleFavorite) {
                        val (icon, tint) = if (ui.favorited) {
                            Icons.Filled.Favorite to MaterialTheme.colorScheme.primary
                        } else {
                            Icons.Outlined.FavoriteBorder to MaterialTheme.colorScheme.onSurface
                        }
                        Icon(icon, contentDescription = "Favorite", tint = tint)
                    }
                },
            )
        },
    ) { padding ->
        PullToRefreshBox(
            isRefreshing = ui.refreshing,
            onRefresh = vm::refresh,
            modifier = Modifier.fillMaxSize().padding(padding),
        ) {
            when {
                ui.loading -> Box(Modifier.fillMaxSize(), Alignment.Center) {
                    CircularProgressIndicator()
                }
                ui.cocktail == null -> Box(Modifier.fillMaxSize(), Alignment.Center) {
                    Text(
                        ui.errorMessage ?: "Cocktail not found.",
                        modifier = Modifier.padding(24.dp),
                    )
                }
                else -> DetailContent(
                    ui = ui,
                    onNoteBodyChange = vm::onNoteBodyChange,
                    onNoteRatingChange = vm::onNoteRatingChange,
                    onSaveNote = vm::saveNote,
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DetailContent(
    ui: DetailUiState,
    onNoteBodyChange: (String) -> Unit,
    onNoteRatingChange: (Int?) -> Unit,
    onSaveNote: () -> Unit,
) {
    val cocktail = ui.cocktail ?: return
    val uriHandler = LocalUriHandler.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 24.dp),
    ) {
        if (cocktail.imageUrl != null) {
            AsyncImage(
                model = cocktail.imageUrl,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 10f)
                    .clip(RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp)),
            )
        }

        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
            cocktail.description?.let {
                Text(it, style = MaterialTheme.typography.bodyLarge)
                Spacer(Modifier.size(12.dp))
            }

            MetaChips(cocktail)

            Spacer(Modifier.size(20.dp))
            SectionHeader("Ingredients")
            IngredientList(ui.ingredientRows)

            cocktail.instructions?.takeIf { it.isNotBlank() }?.let { instructions ->
                Spacer(Modifier.size(20.dp))
                SectionHeader("Method")
                Text(instructions, style = MaterialTheme.typography.bodyLarge)
            }

            cocktail.garnish?.takeIf { it.isNotBlank() }?.let { garnish ->
                Spacer(Modifier.size(20.dp))
                SectionHeader("Garnish")
                Text(garnish, style = MaterialTheme.typography.bodyLarge)
            }

            cocktail.sourceUrl?.takeIf { it.isNotBlank() }?.let { url ->
                Spacer(Modifier.size(20.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.OpenInNew, contentDescription = null)
                    Spacer(Modifier.size(6.dp))
                    TextButton(onClick = { uriHandler.openUri(url) }) {
                        Text(cocktail.sourceName?.let { "Source: $it" } ?: "Open source")
                    }
                }
            }

            Spacer(Modifier.size(24.dp))
            HorizontalDivider()
            Spacer(Modifier.size(16.dp))
            SectionHeader("Your notes")
            RatingRow(rating = ui.noteRating, onChange = onNoteRatingChange)
            Spacer(Modifier.size(8.dp))
            OutlinedTextField(
                value = ui.noteBody,
                onValueChange = onNoteBodyChange,
                label = { Text("Tasting notes, tweaks, etc.") },
                minLines = 3,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.size(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
            ) {
                Button(onClick = onSaveNote, enabled = ui.noteDirty && !ui.noteSaving) {
                    if (ui.noteSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.height(20.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary,
                        )
                    } else {
                        Text(if (ui.noteDirty) "Save" else "Saved")
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleLarge,
        modifier = Modifier.padding(bottom = 8.dp),
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun MetaChips(cocktail: CocktailDetail) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        cocktail.glass?.let { AssistChip(onClick = {}, label = { Text(it) }) }
        cocktail.method?.let { AssistChip(onClick = {}, label = { Text(it.replaceFirstChar { c -> c.uppercase() }) }) }
        cocktail.difficulty?.let {
            AssistChip(onClick = {}, label = { Text("Difficulty $it/5") })
        }
        cocktail.abvEstimate?.let {
            AssistChip(onClick = {}, label = { Text("~${it.toInt()}% ABV") })
        }
        cocktail.flavorTags.forEach { tag ->
            AssistChip(onClick = {}, label = { Text(tag) })
        }
    }
}

@Composable
private fun IngredientList(rows: List<IngredientRowUi>) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        rows.forEach { row ->
            IngredientRow(row)
        }
    }
}

@Composable
private fun IngredientRow(row: IngredientRowUi) {
    val (icon, tint, textColor) = if (row.inPantry) {
        Triple(
            Icons.Filled.CheckCircle,
            MaterialTheme.colorScheme.primary,
            MaterialTheme.colorScheme.onSurface,
        )
    } else {
        Triple(
            Icons.Outlined.Circle,
            MaterialTheme.colorScheme.onSurfaceVariant,
            MaterialTheme.colorScheme.onSurface,
        )
    }
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(20.dp))
        Spacer(Modifier.size(10.dp))
        Text(
            text = row.ingredient.display(),
            style = MaterialTheme.typography.bodyLarge,
            color = textColor,
        )
    }
}

@Composable
private fun RatingRow(rating: Int?, onChange: (Int?) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text("Rating", style = MaterialTheme.typography.labelLarge)
        Spacer(Modifier.size(8.dp))
        (1..5).forEach { star ->
            val filled = rating != null && star <= rating
            IconButton(
                onClick = { onChange(if (rating == star) null else star) },
                modifier = Modifier.size(32.dp),
            ) {
                Icon(
                    imageVector = if (filled) Icons.Filled.Star else Icons.Filled.StarBorder,
                    contentDescription = "$star star${if (star == 1) "" else "s"}",
                    tint = if (filled) MaterialTheme.colorScheme.primary
                           else MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
