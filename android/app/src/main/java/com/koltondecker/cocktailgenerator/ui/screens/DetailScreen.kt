package com.koltondecker.cocktailgenerator.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Favorite
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
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.koltondecker.cocktailgenerator.domain.model.CocktailDetail
import com.koltondecker.cocktailgenerator.ui.components.LocalSnackbarHostState
import com.koltondecker.cocktailgenerator.ui.theme.ReadyGreen
import com.koltondecker.cocktailgenerator.ui.theme.ScrimBottom
import com.koltondecker.cocktailgenerator.ui.theme.ScrimTop

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
                title = {},
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                        ),
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = vm::toggleFavorite,
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                        ),
                    ) {
                        val (icon, tint) = if (ui.favorited) {
                            Icons.Filled.Favorite to MaterialTheme.colorScheme.secondary
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
            .padding(bottom = 32.dp),
    ) {
        // Hero: photo with title on a scrim, like a menu cover.
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(4f / 3.4f)
                .clip(RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant),
        ) {
            if (cocktail.imageUrl != null) {
                AsyncImage(
                    model = cocktail.imageUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
            }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Brush.verticalGradient(0.4f to ScrimTop, 1f to ScrimBottom)),
            )
            Text(
                text = cocktail.name,
                style = MaterialTheme.typography.displayLarge,
                color = Color.White,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(20.dp),
            )
        }

        Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {
            cocktail.description?.let {
                Text(it, style = MaterialTheme.typography.bodyLarge)
                Spacer(Modifier.height(12.dp))
            }

            MetaChips(cocktail)

            Spacer(Modifier.height(24.dp))
            SectionHeader("Ingredients")
            IngredientList(ui.ingredientRows)

            cocktail.instructions?.takeIf { it.isNotBlank() }?.let { instructions ->
                Spacer(Modifier.height(24.dp))
                SectionHeader("Method")
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        instructions,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(16.dp),
                    )
                }
            }

            cocktail.garnish?.takeIf { it.isNotBlank() }?.let { garnish ->
                Spacer(Modifier.height(24.dp))
                SectionHeader("Garnish")
                Text("🍒 $garnish", style = MaterialTheme.typography.bodyLarge)
            }

            cocktail.sourceUrl?.takeIf { it.isNotBlank() }?.let { url ->
                Spacer(Modifier.height(16.dp))
                TextButton(onClick = { uriHandler.openUri(url) }) {
                    Icon(
                        Icons.AutoMirrored.Filled.OpenInNew,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                    )
                    Spacer(Modifier.size(6.dp))
                    Text(cocktail.sourceName?.let { "Source: $it" } ?: "Open source")
                }
            }

            Spacer(Modifier.height(24.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
            Spacer(Modifier.height(16.dp))
            SectionHeader("Your notes")
            RatingRow(rating = ui.noteRating, onChange = onNoteRatingChange)
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = ui.noteBody,
                onValueChange = onNoteBodyChange,
                label = { Text("Tasting notes, tweaks, etc.") },
                minLines = 3,
                shape = MaterialTheme.shapes.small,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
            ) {
                Button(
                    onClick = onSaveNote,
                    enabled = ui.noteDirty && !ui.noteSaving,
                    shape = RoundedCornerShape(50),
                ) {
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
        style = MaterialTheme.typography.headlineLarge,
        modifier = Modifier.padding(bottom = 10.dp),
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun MetaChips(cocktail: CocktailDetail) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        cocktail.glass?.let { AssistChip(onClick = {}, label = { Text("🥂 $it") }) }
        cocktail.method?.let {
            AssistChip(onClick = {}, label = { Text(it.replaceFirstChar { c -> c.uppercase() }) })
        }
        cocktail.difficulty?.let { AssistChip(onClick = {}, label = { Text("Difficulty $it/5") }) }
        cocktail.abvEstimate?.let { AssistChip(onClick = {}, label = { Text("~${it.toInt()}% ABV") }) }
        cocktail.flavorTags.forEach { tag -> AssistChip(onClick = {}, label = { Text(tag) }) }
    }
}

@Composable
private fun IngredientList(rows: List<IngredientRowUi>) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            rows.forEach { row -> IngredientRow(row) }
        }
    }
}

@Composable
private fun IngredientRow(row: IngredientRowUi) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        if (row.inPantry) {
            Icon(
                Icons.Filled.CheckCircle,
                contentDescription = "In your bar",
                tint = ReadyGreen,
                modifier = Modifier.size(20.dp),
            )
        } else {
            Icon(
                Icons.Outlined.Circle,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.outline,
                modifier = Modifier.size(20.dp),
            )
        }
        Spacer(Modifier.size(12.dp))
        Text(
            text = row.ingredient.display(),
            style = MaterialTheme.typography.bodyLarge,
            color = if (row.inPantry) MaterialTheme.colorScheme.onSurface
                    else MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun RatingRow(rating: Int?, onChange: (Int?) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        (1..5).forEach { star ->
            val filled = rating != null && star <= rating
            IconButton(
                onClick = { onChange(if (rating == star) null else star) },
                modifier = Modifier.size(36.dp),
            ) {
                Icon(
                    imageVector = if (filled) Icons.Filled.Star else Icons.Filled.StarBorder,
                    contentDescription = "$star star${if (star == 1) "" else "s"}",
                    tint = if (filled) MaterialTheme.colorScheme.primary
                           else MaterialTheme.colorScheme.outline,
                )
            }
        }
    }
}
