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
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
import com.example.ui.components.*
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
    onOpenSettings: () -> Unit,
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
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // 1. Header with Avatar, Greeting, Target Language Pill & Action Buttons
        LinguaX3DCard(
            modifier = Modifier.fillMaxWidth(),
            backgroundColor = LinguaXSurfaceElevated,
            elevation = 6.dp
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    // 3D Glowing Avatar Ring
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .background(LinguaXPrimaryGradient)
                            .padding(2.5.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                                .background(LinguaXSurface)
                                .padding(4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = (profile.displayName ?: "L").take(1).uppercase(),
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Black,
                                    fontSize = 20.sp
                                ),
                                color = LinguaXPrimary
                            )
                        }
                    }

                    Column {
                        Text(
                            text = l10n.greeting,
                            style = MaterialTheme.typography.bodySmall,
                            color = LinguaXTextSecondary
                        )
                        Text(
                            text = profile.displayName ?: "Learner",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Black,
                                fontSize = 19.sp
                            ),
                            color = LinguaXTextPrimary
                        )
                    }
                }

                // Language Switcher Badge & Settings Button
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        onClick = onNavigateToCourses,
                        shape = RoundedCornerShape(20.dp),
                        color = Color(0xFF1E2D4A),
                        border = androidx.compose.foundation.BorderStroke(1.dp, LinguaXBorderLight),
                        modifier = Modifier.testTag("language_badge_button")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Text(text = selectedTargetLanguage.flagEmoji, fontSize = 16.sp)
                            Text(
                                text = selectedTargetLanguage.code.uppercase(),
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = LinguaXTextPrimary
                            )
                        }
                    }

                    IconButton(
                        onClick = onOpenSettings,
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF1E2D4A))
                            .border(1.dp, LinguaXBorder, CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = LinguaXTextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        // 2. 3D Stat Cards Section (XP, Coins, Daily Streak, Daily Goal)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            LinguaXStatCard(
                icon = Icons.Default.Bolt,
                value = "${profile.xp}",
                label = l10n.xpText,
                gradient = LinguaXAccentGradient,
                iconTint = Color.White,
                modifier = Modifier.weight(1f)
            )
            LinguaXStatCard(
                icon = Icons.Default.MonetizationOn,
                value = "${profile.coins}",
                label = l10n.coinsText,
                gradient = LinguaXGoldGradient,
                iconTint = Color(0xFF5B3800),
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            LinguaXStatCard(
                icon = Icons.Default.LocalFireDepartment,
                value = "${profile.streak} Days",
                label = l10n.streakText,
                gradient = LinguaXFlameGradient,
                iconTint = Color.White,
                modifier = Modifier.weight(1f)
            )
            LinguaXStatCard(
                icon = Icons.Default.TrackChanges,
                value = "${profile.dailyGoal} XP",
                label = l10n.dailyGoalText,
                gradient = LinguaXGreenGradient,
                iconTint = Color(0xFF003816),
                modifier = Modifier.weight(1f)
            )
        }

        // 3. Grand 3D Hero Card: Continue Learning
        ResourceContainer(
            resource = coursesResource,
            loadingText = l10n.loading,
            emptyText = l10n.noDataAvailable
        ) { courses ->
            val activeCourse = courses.firstOrNull()
            if (activeCourse != null) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = l10n.continueLearning,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp
                        ),
                        color = LinguaXTextPrimary
                    )

                    LinguaX3DCard(
                        modifier = Modifier.fillMaxWidth(),
                        backgroundColor = Color(0xFF141F36),
                        borderBrush = LinguaXBorderGradient,
                        elevation = 8.dp
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text(text = selectedTargetLanguage.flagEmoji, fontSize = 28.sp)
                                Column {
                                    Text(
                                        text = selectedTargetLanguage.name,
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                        color = LinguaXAccent
                                    )
                                    Text(
                                        text = activeCourse.title,
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 17.sp
                                        ),
                                        color = LinguaXTextPrimary
                                    )
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = LinguaXPrimaryContainer,
                                border = androidx.compose.foundation.BorderStroke(1.dp, LinguaXPrimary.copy(alpha = 0.4f))
                            ) {
                                Text(
                                    text = activeCourse.level,
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = LinguaXAccentLight,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = activeCourse.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = LinguaXTextSecondary,
                            maxLines = 2
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Progress bar with glowing beads
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Course Progress",
                                style = MaterialTheme.typography.labelSmall,
                                color = LinguaXTextSecondary
                            )
                            Text(
                                text = "42%",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = LinguaXAccent
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        LinguaXProgressBar(
                            progress = 0.42f,
                            fillBrush = LinguaXAccentGradient,
                            height = 8.dp
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        LinguaX3DButton(
                            text = l10n.continueLearning,
                            icon = Icons.AutoMirrored.Filled.ArrowForward,
                            onClick = onNavigateToCourses,
                            testTag = "continue_learning_button"
                        )
                    }
                }
            }

            // 4. Recommended Courses Carousel
            if (courses.size > 1) {
                Spacer(modifier = Modifier.height(4.dp))
                LinguaXHeader(
                    title = l10n.recommendedCourses,
                    subtitle = "Explore next level courses in ${selectedTargetLanguage.name}"
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(courses.drop(1)) { course ->
                        LinguaX3DCard(
                            modifier = Modifier.width(260.dp),
                            backgroundColor = Color(0xFF131C30),
                            elevation = 4.dp,
                            onClick = onNavigateToCourses
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = LinguaXSecondaryContainer
                                ) {
                                    Text(
                                        text = course.level,
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = LinguaXSecondaryLight,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                                Text(
                                    text = "${course.totalLessons} ${l10n.lessonsCount}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = LinguaXTextTertiary
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = course.title,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = LinguaXTextPrimary,
                                maxLines = 1
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = course.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = LinguaXTextSecondary,
                                maxLines = 2
                            )
                        }
                    }
                }
            }
        }

        // 5. Quick Access Action Grid (Vocabulary Lab & Daily Challenges)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            LinguaX3DCard(
                modifier = Modifier.weight(1f),
                backgroundColor = Color(0xFF131E33),
                onClick = onNavigateToVocabulary
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.MenuBook,
                    contentDescription = "Vocabulary",
                    tint = LinguaXAccent,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = l10n.vocabularyTab,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = LinguaXTextPrimary
                )
                Text(
                    text = "Flashcards & Audio",
                    style = MaterialTheme.typography.bodySmall,
                    color = LinguaXTextSecondary
                )
            }

            LinguaX3DCard(
                modifier = Modifier.weight(1f),
                backgroundColor = Color(0xFF161C30),
                onClick = onNavigateToChallenges
            ) {
                Icon(
                    imageVector = Icons.Default.EmojiEvents,
                    contentDescription = "Challenges",
                    tint = LinguaXWarning,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = l10n.challengesTab,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = LinguaXTextPrimary
                )
                Text(
                    text = "Daily XP Quests",
                    style = MaterialTheme.typography.bodySmall,
                    color = LinguaXTextSecondary
                )
            }
        }

        // Footer System Badge
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(LinguaXSuccess)
                )
                Text(
                    text = l10n.supabaseConnected,
                    style = MaterialTheme.typography.labelSmall,
                    color = LinguaXTextTertiary
                )
            }
        }
    }
}
