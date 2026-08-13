package com.example.ui.screens.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.i18n.L10nStrings
import com.example.data.model.Course
import com.example.data.model.DailyChallenge
import com.example.data.model.LanguageItem
import com.example.data.model.Profile
import com.example.data.repository.Resource
import com.example.ui.components.LinguaXHeader
import com.example.ui.components.ResourceContainer
import com.example.ui.components.StatChip
import com.example.ui.theme.*

@Composable
fun HomeScreen(
    l10n: L10nStrings,
    profile: Profile,
    selectedTargetLanguage: LanguageItem,
    languagesResource: Resource<List<LanguageItem>>,
    coursesResource: Resource<List<Course>>,
    challengesResource: Resource<List<DailyChallenge>>,
    onLanguageSelected: (LanguageItem) -> Unit,
    onNavigateToCourses: () -> Unit,
    onNavigateToVocabulary: () -> Unit,
    onNavigateToChallenges: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(LinguaXBackground)
            .verticalScroll(scrollState)
            .padding(16.dp)
            .systemBarsPadding(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // User Greeting & Stat Bar
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = l10n.greeting,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = profile.displayName ?: "Learner",
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.Black,
                                fontSize = 22.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    // Target Language Selector Chip
                    Surface(
                        onClick = onNavigateToCourses,
                        shape = RoundedCornerShape(20.dp),
                        color = LinguaXPrimaryContainer,
                        modifier = Modifier.testTag("target_language_chip")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(text = selectedTargetLanguage.flagEmoji, fontSize = 18.sp)
                            Text(
                                text = selectedTargetLanguage.name,
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                color = LinguaXOnPrimaryContainer
                            )
                            Icon(
                                imageVector = Icons.Default.ExpandMore,
                                contentDescription = "Change Language",
                                tint = LinguaXOnPrimaryContainer,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)

                // Stats Chips Bar: Streak, XP, Coins
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    StatChip(
                        icon = Icons.Default.LocalFireDepartment,
                        value = "${profile.streak}",
                        label = l10n.streakText,
                        iconTint = LinguaXAccentFlame,
                        backgroundTint = Color(0xFFFEF2F2),
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    StatChip(
                        icon = Icons.Default.Bolt,
                        value = "${profile.xp}",
                        label = l10n.xpText,
                        iconTint = LinguaXAccentCyan,
                        backgroundTint = Color(0xFFECFEFF),
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    StatChip(
                        icon = Icons.Default.MonetizationOn,
                        value = "${profile.coins}",
                        label = l10n.coinsText,
                        iconTint = LinguaXAccentGold,
                        backgroundTint = Color(0xFFFFFBEB),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Daily Goal Progress Banner
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(
                        progress = { (profile.xp % profile.dailyGoal).toFloat() / profile.dailyGoal.toFloat().coerceAtLeast(1f) },
                        modifier = Modifier.size(52.dp),
                        color = LinguaXPrimary,
                        trackColor = LinguaXPrimaryContainer,
                        strokeWidth = 6.dp
                    )
                    Icon(
                        imageVector = Icons.Default.EmojiEvents,
                        contentDescription = null,
                        tint = LinguaXAccentGold,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "${l10n.dailyGoalText}: ${profile.dailyGoal} XP",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "Earned ${profile.xp % profile.dailyGoal} / ${profile.dailyGoal} XP today",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Continue Learning Banner
        LinguaXHeader(
            title = l10n.continueLearning,
            action = {
                TextButton(onClick = onNavigateToCourses) {
                    Text(text = "View All", color = LinguaXPrimary)
                }
            }
        )

        ResourceContainer(
            resource = coursesResource,
            loadingText = l10n.loading,
            emptyText = l10n.noDataAvailable
        ) { courses ->
            val activeCourse = courses.firstOrNull()
            if (activeCourse != null) {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = LinguaXPrimary),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(LinguaXPrimary, LinguaXSecondary)
                                )
                            )
                            .padding(20.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = Color.White.copy(alpha = 0.2f)
                                ) {
                                    Text(
                                        text = activeCourse.level,
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold
                                        ),
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    )
                                }
                                Text(
                                    text = "${selectedTargetLanguage.flagEmoji} ${selectedTargetLanguage.name}",
                                    color = Color.White.copy(alpha = 0.9f),
                                    style = MaterialTheme.typography.labelLarge
                                )
                            }

                            Text(
                                text = activeCourse.title,
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White
                                )
                            )

                            Text(
                                text = activeCourse.description,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = Color.White.copy(alpha = 0.85f)
                                ),
                                maxLines = 2
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Button(
                                onClick = onNavigateToCourses,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.White,
                                    contentColor = LinguaXPrimary
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.testTag("home_continue_lesson_button")
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(Icons.Default.PlayArrow, contentDescription = null)
                                    Text(
                                        text = l10n.startLesson,
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Quick Actions Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            QuickActionCard(
                title = l10n.vocabularyTab,
                subtitle = "Practice flashcards",
                icon = Icons.Default.MenuBook,
                iconBg = Color(0xFFEFF6FF),
                iconTint = LinguaXPrimary,
                onClick = onNavigateToVocabulary,
                modifier = Modifier.weight(1f)
            )
            QuickActionCard(
                title = l10n.challengesTab,
                subtitle = "Earn bonus XP",
                icon = Icons.Default.Flag,
                iconBg = Color(0xFFFEF2F2),
                iconTint = LinguaXAccentFlame,
                onClick = onNavigateToChallenges,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun QuickActionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconBg: Color,
    iconTint: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = iconBg,
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
