package com.koltondecker.cocktailgenerator.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.koltondecker.cocktailgenerator.BuildConfig
import io.github.jan.supabase.compose.auth.ComposeAuth
import io.github.jan.supabase.compose.auth.composable.rememberSignInWithGoogle

@Composable
fun AuthScreen(vm: AuthViewModel = hiltViewModel()) {
    val ui by vm.state.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "Cocktail Generator",
            style = MaterialTheme.typography.displayLarge,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = when (ui.mode) {
                AuthMode.SIGN_IN -> "Sign in to see what you can make tonight."
                AuthMode.SIGN_UP -> "Create an account to save your pantry and favorites."
            },
            style = MaterialTheme.typography.bodyLarge,
        )
        Spacer(Modifier.height(24.dp))

        OutlinedTextField(
            value = ui.email,
            onValueChange = vm::onEmailChange,
            label = { Text("Email") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = ui.password,
            onValueChange = vm::onPasswordChange,
            label = { Text("Password") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth(),
        )

        ui.errorMessage?.let {
            Spacer(Modifier.height(12.dp))
            Text(it, color = MaterialTheme.colorScheme.error)
        }
        ui.infoMessage?.let {
            Spacer(Modifier.height(12.dp))
            Text(it, color = MaterialTheme.colorScheme.primary)
        }

        Spacer(Modifier.height(24.dp))
        Button(
            onClick = vm::submit,
            enabled = ui.canSubmit,
            modifier = Modifier.fillMaxWidth(),
        ) {
            if (ui.submitting) {
                CircularProgressIndicator(
                    modifier = Modifier.height(20.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onPrimary,
                )
            } else {
                Text(if (ui.mode == AuthMode.SIGN_IN) "Sign in" else "Create account")
            }
        }

        Spacer(Modifier.height(8.dp))
        TextButton(onClick = vm::toggleMode) {
            Text(
                when (ui.mode) {
                    AuthMode.SIGN_IN -> "Don't have an account? Sign up"
                    AuthMode.SIGN_UP -> "Already have an account? Sign in"
                }
            )
        }

        if (BuildConfig.GOOGLE_WEB_CLIENT_ID.isNotBlank()) {
            Spacer(Modifier.height(16.dp))
            HorizontalDivider()
            Spacer(Modifier.height(16.dp))
            GoogleSignInButton(vm.composeAuth)
        }
    }
}

@Composable
private fun GoogleSignInButton(composeAuth: ComposeAuth) {
    // Supabase updates SessionStatus on success — no callback plumbing needed.
    val action = composeAuth.rememberSignInWithGoogle(onResult = {})
    OutlinedButton(
        onClick = { action.startFlow() },
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text("Continue with Google")
    }
}
