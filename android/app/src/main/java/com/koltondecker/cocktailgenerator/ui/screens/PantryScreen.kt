package com.koltondecker.cocktailgenerator.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

// TODO: categorized ingredient list w/ sticky headers, search, toggle chips.
// Reads ingredient_categories + ingredients (is_common=true) from Supabase,
// writes toggles to user_pantry with an optimistic local Room mirror.
@Composable
fun PantryScreen() {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("My Pantry", style = MaterialTheme.typography.headlineLarge)
        Text("Check off what you have on hand.")
    }
}
