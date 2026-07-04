package com.koltondecker.cocktailgenerator

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.koltondecker.cocktailgenerator.ui.navigation.AppNav
import com.koltondecker.cocktailgenerator.ui.theme.CocktailGeneratorTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { App() }
    }
}

@Composable
private fun App() {
    CocktailGeneratorTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            AppNav()
        }
    }
}
