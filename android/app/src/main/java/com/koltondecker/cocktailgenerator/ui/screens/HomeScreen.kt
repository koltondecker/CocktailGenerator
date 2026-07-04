package com.koltondecker.cocktailgenerator.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

// TODO: hero carousel of makeable-now + featured, wired to match_cocktails RPC.
@Composable
fun HomeScreen(onOpenCocktail: (Long) -> Unit) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Tonight", style = MaterialTheme.typography.displayLarge)
        Text("Cocktails you can make right now will appear here.")
    }
}
