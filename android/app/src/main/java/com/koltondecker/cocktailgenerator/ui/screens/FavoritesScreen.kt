package com.koltondecker.cocktailgenerator.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

// TODO: grid of favorited cocktails from user_favorites join cocktails.
@Composable
fun FavoritesScreen(onOpenCocktail: (Long) -> Unit) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Favorites", style = MaterialTheme.typography.headlineLarge)
        Text("Your saved cocktails will live here.")
    }
}
