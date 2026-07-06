package com.koltondecker.cocktailgenerator.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.koltondecker.cocktailgenerator.BuildConfig
import com.koltondecker.cocktailgenerator.ui.theme.GradA
import com.koltondecker.cocktailgenerator.ui.theme.GradB
import com.koltondecker.cocktailgenerator.ui.theme.GradC
import com.koltondecker.cocktailgenerator.ui.theme.InkBackground
import io.github.jan.supabase.compose.auth.ComposeAuth
import io.github.jan.supabase.compose.auth.composable.rememberSignInWithGoogle

@Composable
fun AuthScreen(vm: AuthViewModel = hiltViewModel()) {
    val ui by vm.state.collectAsStateWithLifecycle()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    0f to GradC.copy(alpha = 0.5f),
                    0.35f to InkBackground,
                    1f to InkBackground,
                ),
            ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text("🍸", style = MaterialTheme.typography.displayLarge)
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Shelf to Glass",
                style = MaterialTheme.typography.displayLarge,
                color = Color.White,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = when (ui.mode) {
                    AuthMode.SIGN_IN -> "Sign in to see what your bar can pour tonight."
                    AuthMode.SIGN_UP -> "Create an account — your shelf and favorites follow you."
                },
                style = MaterialTheme.typography.bodyLarge,
                color = Color(0xCCF5EDE2),
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(32.dp))

            OutlinedTextField(
                value = ui.email,
                onValueChange = vm::onEmailChange,
                label = { Text("Email") },
                singleLine = true,
                shape = MaterialTheme.shapes.small,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = ui.password,
                onValueChange = vm::onPasswordChange,
                label = { Text("Password") },
                singleLine = true,
                shape = MaterialTheme.shapes.small,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth(),
            )

            ui.errorMessage?.let {
                Spacer(Modifier.height(12.dp))
                Text(it, color = MaterialTheme.colorScheme.error, textAlign = TextAlign.Center)
            }
            ui.infoMessage?.let {
                Spacer(Modifier.height(12.dp))
                Text(it, color = MaterialTheme.colorScheme.tertiary, textAlign = TextAlign.Center)
            }

            Spacer(Modifier.height(24.dp))
            Button(
                onClick = vm::submit,
                enabled = ui.canSubmit,
                shape = RoundedCornerShape(50),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
            ) {
                if (ui.submitting) {
                    CircularProgressIndicator(
                        modifier = Modifier.height(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                } else {
                    Text(
                        if (ui.mode == AuthMode.SIGN_IN) "Sign in" else "Create account",
                        style = MaterialTheme.typography.titleLarge,
                    )
                }
            }

            Spacer(Modifier.height(8.dp))
            TextButton(onClick = vm::toggleMode) {
                Text(
                    when (ui.mode) {
                        AuthMode.SIGN_IN -> "Don't have an account? Sign up"
                        AuthMode.SIGN_UP -> "Already have an account? Sign in"
                    },
                    color = Color(0xCCF5EDE2),
                )
            }

            if (BuildConfig.GOOGLE_WEB_CLIENT_ID.isNotBlank()) {
                Spacer(Modifier.height(16.dp))
                HorizontalDivider(color = Color(0x33FFFFFF))
                Spacer(Modifier.height(16.dp))
                GoogleSignInButton(vm.composeAuth)
            }
        }
    }
}

@Composable
private fun GoogleSignInButton(composeAuth: ComposeAuth) {
    // Supabase updates SessionStatus on success — no callback plumbing needed.
    val action = composeAuth.rememberSignInWithGoogle(onResult = {})
    OutlinedButton(
        onClick = { action.startFlow() },
        shape = RoundedCornerShape(50),
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp),
    ) {
        Text("Continue with Google")
    }
}
