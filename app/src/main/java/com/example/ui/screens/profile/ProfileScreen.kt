package com.example.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.i18n.AppLanguage
import com.example.data.i18n.L10nStrings
import com.example.data.model.LanguageItem
import com.example.data.model.Profile
import com.example.ui.components.LinguaX3DButton
import com.example.ui.components.LinguaX3DCard
import com.example.ui.components.LinguaXHeader
import com.example.ui.theme.*

@Composable
fun ProfileScreen(
    l10n: L10nStrings,
    profile: Profile,
    currentAppLanguage: AppLanguage,
    selectedTargetLanguage: LanguageItem,
    onAppLanguageChange: (AppLanguage) -> Unit,
    onUpdateProfile: (String, Int) -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    var displayName by remember { mutableStateOf(profile.displayName ?: "Learner") }
    var dailyGoal by remember { mutableIntStateOf(profile.dailyGoal) }
    var isSavedToastVisible by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(LinguaXBackground)
            .verticalScroll(scrollState)
            .padding(16.dp)
            .systemBarsPadding(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        LinguaXHeader(
            title = l10n.profileTab,
            subtitle = "Manage your preferences, language, and goals"
        )

        // Profile Avatar Card
        LinguaX3DCard(
            modifier = Modifier.fillMaxWidth(),
            backgroundColor = Color(0xFF131C2E)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(LinguaXPrimaryGradient)
                        .padding(3.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                            .background(LinguaXSurface),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = displayName.take(1).uppercase(),
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Black,
                                fontSize = 24.sp
                            ),
                            color = LinguaXPrimary
                        )
                    }
                }

                Column {
                    Text(
                        text = displayName,
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = LinguaXTextPrimary
                    )
                    Text(
                        text = "@${profile.username ?: "user"}",
                        style = MaterialTheme.typography.bodySmall,
                        color = LinguaXTextTertiary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = LinguaXSuccess.copy(alpha = 0.2f)
                    ) {
                        Text(
                            text = l10n.supabaseConnected,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = LinguaXSuccess,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }

        // Interface Language Switcher Card
        LinguaX3DCard(
            modifier = Modifier.fillMaxWidth(),
            backgroundColor = Color(0xFF131C2E)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = l10n.appLanguage,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = LinguaXTextPrimary
                )
                Text(
                    text = l10n.selectInterfaceLanguage,
                    style = MaterialTheme.typography.bodySmall,
                    color = LinguaXTextSecondary
                )

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    AppLanguage.values().forEach { lang ->
                        val isSelected = currentAppLanguage == lang
                        Surface(
                            onClick = { onAppLanguageChange(lang) },
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) LinguaXPrimaryContainer else Color(0xFF162136),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isSelected) LinguaXPrimary else Color(0xFF243552)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(text = lang.flag, fontSize = 18.sp)
                                    Text(
                                        text = "${lang.displayName} (${lang.nativeName})",
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        ),
                                        color = if (isSelected) Color.White else LinguaXTextPrimary
                                    )
                                }
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = LinguaXAccent,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Daily Goal Setting
        LinguaX3DCard(
            modifier = Modifier.fillMaxWidth(),
            backgroundColor = Color(0xFF131C2E)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = l10n.dailyGoalText,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = LinguaXTextPrimary
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    listOf(15, 30, 50).forEach { goal ->
                        val isSelected = dailyGoal == goal
                        Surface(
                            onClick = { dailyGoal = goal },
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) LinguaXPrimary else Color(0xFF162136),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "$goal XP",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = if (isSelected) Color.White else LinguaXTextPrimary
                                )
                                Text(
                                    text = if (goal == 15) "Casual" else if (goal == 30) "Regular" else "Intense",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isSelected) Color.White.copy(alpha = 0.8f) else LinguaXTextTertiary
                                )
                            }
                        }
                    }
                }
            }
        }

        // Edit Profile Details
        LinguaX3DCard(
            modifier = Modifier.fillMaxWidth(),
            backgroundColor = Color(0xFF131C2E)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Edit Profile",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = LinguaXTextPrimary
                )

                OutlinedTextField(
                    value = displayName,
                    onValueChange = { displayName = it },
                    label = { Text(l10n.displayNameLabel) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFF162136),
                        unfocusedContainerColor = Color(0xFF111726),
                        focusedBorderColor = LinguaXAccent,
                        unfocusedBorderColor = LinguaXBorder,
                        focusedTextColor = LinguaXTextPrimary,
                        unfocusedTextColor = LinguaXTextPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                LinguaX3DButton(
                    text = l10n.saveChanges,
                    onClick = {
                        onUpdateProfile(displayName, dailyGoal)
                        isSavedToastVisible = true
                    },
                    height = 46.dp,
                    testTag = "save_profile_button"
                )

                if (isSavedToastVisible) {
                    Text(
                        text = l10n.profileUpdated,
                        style = MaterialTheme.typography.labelMedium,
                        color = LinguaXSuccess
                    )
                }
            }
        }

        // Logout Button
        LinguaX3DButton(
            text = l10n.logout,
            icon = Icons.AutoMirrored.Filled.ExitToApp,
            gradient = Brush.linearGradient(listOf(Color(0xFF33141B), Color(0xFF260D12))),
            textColor = LinguaXErrorLight,
            onClick = onLogout,
            height = 48.dp,
            testTag = "logout_button"
        )
    }
}
