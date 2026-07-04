package com.koltondecker.cocktailgenerator.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

// TODO: hero carousel of makeable-now + featured, wired to match_cocktails RPC.
// The sign-out affordance lives here until we build a proper Profile screen.
@Composable
fun HomeScreen(
    onOpenCocktail: (Long) -> Unit,
    onSignOut: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxSize().weight(1f, fill = false),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text("Tonight", style = MaterialTheme.typography.displayLarge)
            TextButton(onClick = onSignOut) { Text("Sign out") }
        }
        Text("Cocktails you can make right now will appear here.")
    }
}
