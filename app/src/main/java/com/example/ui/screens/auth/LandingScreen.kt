package com.example.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Language
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.i18n.AppLanguage
import com.example.data.i18n.L10nStrings
import com.example.ui.components.LinguaX3DButton
import com.example.ui.components.LinguaX3DCard
import com.example.ui.theme.*

@Composable
fun LandingScreen(
    l10n: L10nStrings,
    currentAppLanguage: AppLanguage,
    onLanguageChange: (AppLanguage) -> Unit,
    onNavigateToLogin: () -> Unit,
    onNavigateToSignup: () -> Unit,
    onContinueAsGuest: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showLanguageMenu by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(LinguaXBackground)
            .padding(20.dp)
            .systemBarsPadding()
    ) {
        // Top Bar: Language Selector
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopEnd),
            horizontalArrangement = Arrangement.End
        ) {
            Box {
                Surface(
                    onClick = { showLanguageMenu = true },
                    shape = RoundedCornerShape(20.dp),
                    color = Color(0xFF162238),
                    border = androidx.compose.foundation.BorderStroke(1.dp, LinguaXBorderLight)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(text = currentAppLanguage.flag, fontSize = 16.sp)
                        Text(
                            text = currentAppLanguage.displayName,
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = LinguaXTextPrimary
                        )
                    }
                }

                DropdownMenu(
                    expanded = showLanguageMenu,
                    onDismissRequest = { showLanguageMenu = false },
                    modifier = Modifier.background(LinguaXSurfaceElevated)
                ) {
                    AppLanguage.values().forEach { lang ->
                        DropdownMenuItem(
                            text = {
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text(text = lang.flag)
                                    Text(text = "${lang.displayName} (${lang.nativeName})", color = LinguaXTextPrimary)
                                }
                            },
                            onClick = {
                                onLanguageChange(lang)
                                showLanguageMenu = false
                            }
                        )
                    }
                }
            }
        }

        // Center Content
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // 3D Glowing App Brand Icon
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(LinguaXPrimaryGradient)
                    .padding(3.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(22.dp))
                        .background(LinguaXSurface)
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "LX",
                        style = MaterialTheme.typography.displayLarge.copy(
                            fontWeight = FontWeight.Black,
                            fontSize = 32.sp
                        ),
                        color = LinguaXAccent
                    )
                }
            }

            Text(
                text = l10n.appName,
                style = MaterialTheme.typography.displayMedium.copy(
                    fontWeight = FontWeight.Black,
                    fontSize = 32.sp
                ),
                color = LinguaXTextPrimary
            )

            Text(
                text = l10n.welcomeTitle,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                ),
                color = LinguaXAccentLight,
                textAlign = TextAlign.Center
            )

            Text(
                text = l10n.welcomeSubtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = LinguaXTextSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 10.dp)
            )

            // Supported Languages Flag Pill Ribbon
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color(0xFF131C2E),
                border = androidx.compose.foundation.BorderStroke(1.dp, LinguaXBorder)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("🇸🇦", fontSize = 18.sp)
                    Text("🇺🇸", fontSize = 18.sp)
                    Text("🇫🇷", fontSize = 18.sp)
                    Text("🇪🇸", fontSize = 18.sp)
                    Text("🇩🇪", fontSize = 18.sp)
                    Text("🇮🇹", fontSize = 18.sp)
                    Text("🇹🇷", fontSize = 18.sp)
                    Text("🇯🇵", fontSize = 18.sp)
                    Text("🇰🇷", fontSize = 18.sp)
                }
            }
        }

        // Bottom Actions
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            LinguaX3DButton(
                text = l10n.getStarted,
                icon = Icons.AutoMirrored.Filled.ArrowForward,
                onClick = onNavigateToSignup,
                testTag = "landing_signup_button"
            )

            LinguaX3DButton(
                text = l10n.signIn,
                gradient = Brush.linearGradient(listOf(Color(0xFF1E2D4A), Color(0xFF162136))),
                textColor = LinguaXTextPrimary,
                onClick = onNavigateToLogin,
                testTag = "landing_login_button"
            )

            TextButton(
                onClick = onContinueAsGuest,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Explore as Guest",
                    style = MaterialTheme.typography.labelMedium,
                    color = LinguaXTextTertiary
                )
            }
        }
    }
}
