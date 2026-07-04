package com.koltondecker.cocktailgenerator.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

// TODO: tabs (Can make | 1 away | All), filter sheet (spirit / ABV / flavor / difficulty).
@Composable
fun BrowseScreen(onOpenCocktail: (Long) -> Unit) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Browse", style = MaterialTheme.typography.headlineLarge)
        Text("All cocktails, filterable by spirit / ABV / flavor / difficulty.")
    }
}
