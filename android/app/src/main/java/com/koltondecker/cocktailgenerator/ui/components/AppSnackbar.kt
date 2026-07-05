package com.koltondecker.cocktailgenerator.ui.components

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.staticCompositionLocalOf

/**
 * App-wide snackbar host. Owned by SignedInNav's Scaffold; any screen inside
 * the signed-in graph reads it with `LocalSnackbarHostState.current`. Screens
 * typically drive it from a `LaunchedEffect(ui.errorMessage)` so error state
 * flows out of the ViewModel and into a snackbar without a separate event bus.
 */
val LocalSnackbarHostState = staticCompositionLocalOf<SnackbarHostState> {
    error("LocalSnackbarHostState not provided — wrap the screen in SignedInNav's Scaffold.")
}
