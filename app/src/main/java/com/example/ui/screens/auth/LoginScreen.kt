package com.example.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.i18n.L10nStrings
import com.example.ui.components.LinguaX3DButton
import com.example.ui.components.LinguaXHeader
import com.example.ui.theme.*
import com.example.ui.viewmodel.AuthState

@Composable
fun LoginScreen(
    l10n: L10nStrings,
    authState: AuthState,
    onLogin: (String, String) -> Unit,
    onNavigateToSignup: () -> Unit,
    modifier: Modifier = Modifier
) {
    var email by remember { mutableStateOf("learner@linguax.com") }
    var password by remember { mutableStateOf("password123") }
    var isPasswordVisible by remember { mutableStateOf(false) }

    val isLoading = authState is AuthState.Loading
    val errorMessage = (authState as? AuthState.Error)?.message

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(LinguaXBackground)
            .padding(20.dp)
            .systemBarsPadding(),
        verticalArrangement = Arrangement.Center
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            LinguaXHeader(
                title = l10n.loginButton,
                subtitle = "Sign in to access your cloud-synced progress"
            )

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text(l10n.emailLabel) },
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = LinguaXAccent) },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = LinguaXSurfaceElevated,
                    unfocusedContainerColor = LinguaXSurface,
                    focusedBorderColor = LinguaXAccent,
                    unfocusedBorderColor = LinguaXBorder,
                    focusedTextColor = LinguaXTextPrimary,
                    unfocusedTextColor = LinguaXTextPrimary
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("login_email_field")
            )

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text(l10n.passwordLabel) },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = LinguaXAccent) },
                trailingIcon = {
                    IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                        Icon(
                            imageVector = if (isPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = null,
                            tint = LinguaXTextSecondary
                        )
                    }
                },
                visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = LinguaXSurfaceElevated,
                    unfocusedContainerColor = LinguaXSurface,
                    focusedBorderColor = LinguaXAccent,
                    unfocusedBorderColor = LinguaXBorder,
                    focusedTextColor = LinguaXTextPrimary,
                    unfocusedTextColor = LinguaXTextPrimary
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("login_password_field")
            )

            if (!errorMessage.isNullOrBlank()) {
                Text(
                    text = errorMessage,
                    style = MaterialTheme.typography.bodySmall,
                    color = LinguaXError
                )
            }

            LinguaX3DButton(
                text = if (isLoading) l10n.loading else l10n.loginButton,
                enabled = email.isNotBlank() && password.length >= 6 && !isLoading,
                onClick = { onLogin(email, password) },
                testTag = "login_submit_button"
            )

            TextButton(
                onClick = onNavigateToSignup,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = l10n.dontHaveAccount,
                    style = MaterialTheme.typography.bodyMedium,
                    color = LinguaXAccentLight
                )
            }
        }
    }
}
