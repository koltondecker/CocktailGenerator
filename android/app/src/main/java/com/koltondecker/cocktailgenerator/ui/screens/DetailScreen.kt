package com.koltondecker.cocktailgenerator.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

// TODO: full recipe view — ingredients (highlight pantry matches), method,
// source link, favorite + notes.
@Composable
fun DetailScreen(cocktailId: Long, onBack: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        TextButton(onClick = onBack) { Text("Back") }
        Text("Cocktail #$cocktailId", style = MaterialTheme.typography.headlineLarge)
    }
}
