package com.koltondecker.cocktailgenerator

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.koltondecker.cocktailgenerator.ui.navigation.SignedInNav
import com.koltondecker.cocktailgenerator.ui.screens.AuthScreen
import com.koltondecker.cocktailgenerator.ui.screens.LoadingScreen
import com.koltondecker.cocktailgenerator.ui.session.SessionViewModel
import com.koltondecker.cocktailgenerator.ui.theme.CocktailGeneratorTheme
import dagger.hilt.android.AndroidEntryPoint
import io.github.jan.supabase.auth.status.SessionStatus

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
            AppRoot()
        }
    }
}

@Composable
private fun AppRoot(sessionVm: SessionViewModel = hiltViewModel()) {
    val status by sessionVm.sessionStatus.collectAsStateWithLifecycle()
    when (status) {
        is SessionStatus.Initializing     -> LoadingScreen()
        is SessionStatus.NotAuthenticated -> AuthScreen()
        is SessionStatus.Authenticated    -> SignedInNav(onSignOut = sessionVm::signOut)
        is SessionStatus.RefreshFailure   -> AuthScreen()
    }
}
