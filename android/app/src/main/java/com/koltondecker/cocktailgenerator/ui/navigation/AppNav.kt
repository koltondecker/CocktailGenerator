package com.koltondecker.cocktailgenerator.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Kitchen
import androidx.compose.material.icons.filled.LocalBar
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.koltondecker.cocktailgenerator.ui.screens.BrowseScreen
import com.koltondecker.cocktailgenerator.ui.screens.DetailScreen
import com.koltondecker.cocktailgenerator.ui.screens.FavoritesScreen
import com.koltondecker.cocktailgenerator.ui.screens.HomeScreen
import com.koltondecker.cocktailgenerator.ui.screens.PantryScreen

private sealed class Dest(val route: String, val label: String, val icon: ImageVector) {
    data object Home     : Dest("home",      "Home",      Icons.Filled.Home)
    data object Pantry   : Dest("pantry",    "Pantry",    Icons.Filled.Kitchen)
    data object Browse   : Dest("browse",    "Browse",    Icons.Filled.LocalBar)
    data object Favorites: Dest("favorites", "Favorites", Icons.Filled.Bookmark)
}

private val tabs = listOf(Dest.Home, Dest.Pantry, Dest.Browse, Dest.Favorites)

/**
 * Nav graph shown only when the user is authenticated. Auth routing is handled
 * one level up by [com.koltondecker.cocktailgenerator.MainActivity]'s AppRoot,
 * which observes session status and swaps this graph in/out.
 */
@Composable
fun SignedInNav(onSignOut: () -> Unit) {
    val nav = rememberNavController()
    val entry by nav.currentBackStackEntryAsState()
    val current = entry?.destination

    Scaffold(
        bottomBar = {
            if (tabs.any { current?.hierarchy?.any { d -> d.route == it.route } == true }) {
                NavigationBar {
                    tabs.forEach { tab ->
                        val selected = current?.hierarchy?.any { it.route == tab.route } == true
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                nav.navigate(tab.route) {
                                    popUpTo(nav.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(tab.icon, contentDescription = tab.label) },
                            label = { Text(tab.label) }
                        )
                    }
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = nav,
            startDestination = Dest.Home.route,
            modifier = Modifier.padding(padding)
        ) {
            composable(Dest.Home.route) {
                HomeScreen(
                    onOpenCocktail = { id -> nav.navigate("cocktail/$id") },
                    onSignOut = onSignOut,
                )
            }
            composable(Dest.Pantry.route)   { PantryScreen() }
            composable(Dest.Browse.route)   { BrowseScreen(onOpenCocktail = { id -> nav.navigate("cocktail/$id") }) }
            composable(Dest.Favorites.route){ FavoritesScreen(onOpenCocktail = { id -> nav.navigate("cocktail/$id") }) }
            composable("cocktail/{id}") {
                // DetailViewModel reads the "id" nav-arg from SavedStateHandle.
                DetailScreen(onBack = { nav.popBackStack() })
            }
        }
    }
}
