package com.example.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.data.i18n.L10nStrings
import com.example.ui.components.LinguaX3DButton
import com.example.ui.components.LinguaXHeader
import com.example.ui.theme.*
import com.example.ui.viewmodel.AuthState

@Composable
fun SignupScreen(
    l10n: L10nStrings,
    authState: AuthState,
    onSignup: (String, String, String) -> Unit,
    onNavigateToLogin: () -> Unit,
    modifier: Modifier = Modifier
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var displayName by remember { mutableStateOf("") }

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
                title = l10n.signupButton,
                subtitle = "Create your free polyglot learner profile"
            )

            OutlinedTextField(
                value = displayName,
                onValueChange = { displayName = it },
                label = { Text(l10n.displayNameLabel) },
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = LinguaXAccent) },
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
                    .testTag("signup_name_field")
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
                    .testTag("signup_email_field")
            )

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text(l10n.passwordLabel) },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = LinguaXAccent) },
                visualTransformation = PasswordVisualTransformation(),
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
                    .testTag("signup_password_field")
            )

            if (!errorMessage.isNullOrBlank()) {
                Text(
                    text = errorMessage,
                    style = MaterialTheme.typography.bodySmall,
                    color = LinguaXError
                )
            }

            LinguaX3DButton(
                text = if (isLoading) l10n.loading else l10n.signupButton,
                enabled = email.isNotBlank() && password.length >= 6 && displayName.isNotBlank() && !isLoading,
                onClick = { onSignup(email, password, displayName) },
                testTag = "signup_submit_button"
            )

            TextButton(
                onClick = onNavigateToLogin,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = l10n.alreadyHaveAccount,
                    style = MaterialTheme.typography.bodyMedium,
                    color = LinguaXAccentLight
                )
            }
        }
    }
}
