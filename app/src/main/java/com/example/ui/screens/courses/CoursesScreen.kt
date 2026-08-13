package com.example.ui.screens.courses

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import com.example.data.i18n.L10nStrings
import com.example.data.model.*
import com.example.data.repository.Resource
import com.example.ui.components.LinguaX3DButton
import com.example.ui.components.LinguaX3DCard
import com.example.ui.components.LinguaXHeader
import com.example.ui.components.ResourceContainer
import com.example.ui.theme.*

@Composable
fun CoursesScreen(
    l10n: L10nStrings,
    selectedTargetLanguage: LanguageItem,
    languagesResource: Resource<List<LanguageItem>>,
    coursesResource: Resource<List<Course>>,
    onLanguageSelected: (LanguageItem) -> Unit,
    onLessonClicked: (Lesson) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(LinguaXBackground)
            .padding(16.dp)
            .systemBarsPadding(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        LinguaXHeader(
            title = l10n.coursesTab,
            subtitle = "${selectedTargetLanguage.flagEmoji} ${selectedTargetLanguage.name} • ${l10n.allCourses}"
        )

        // 3D Horizontal Carousel for All Target Languages
        ResourceContainer(
            resource = languagesResource,
            loadingText = l10n.loading,
            emptyText = l10n.noDataAvailable
        ) { languages ->
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(languages) { lang ->
                    val isSelected = lang.code.equals(selectedTargetLanguage.code, ignoreCase = true)
                    Surface(
                        onClick = { onLanguageSelected(lang) },
                        shape = RoundedCornerShape(16.dp),
                        color = if (isSelected) LinguaXPrimary else Color(0xFF131C2E),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isSelected) LinguaXAccent else LinguaXBorder
                        ),
                        modifier = Modifier.testTag("lang_chip_${lang.code}")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(text = lang.flagEmoji, fontSize = 18.sp)
                            Column {
                                Text(
                                    text = lang.name,
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                    ),
                                    color = if (isSelected) Color.White else LinguaXTextPrimary
                                )
                                if (!lang.nativeName.isNullOrBlank()) {
                                    Text(
                                        text = lang.nativeName,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (isSelected) Color.White.copy(alpha = 0.8f) else LinguaXTextTertiary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Dynamic Course Hierarchy (Course -> Units -> Lessons)
        ResourceContainer(
            resource = coursesResource,
            loadingText = l10n.loading,
            emptyText = l10n.noDataAvailable
        ) { courses ->
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(18.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(courses) { course ->
                    CourseCardView(
                        course = course,
                        l10n = l10n,
                        onLessonClicked = onLessonClicked
                    )
                }
            }
        }
    }
}

@Composable
fun CourseCardView(
    course: Course,
    l10n: L10nStrings,
    onLessonClicked: (Lesson) -> Unit
) {
    var isExpanded by remember { mutableStateOf(true) }

    LinguaX3DCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = Color(0xFF121B2C),
        borderBrush = LinguaXBorderGradient,
        elevation = 6.dp
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = LinguaXPrimaryContainer,
                    border = androidx.compose.foundation.BorderStroke(1.dp, LinguaXPrimary.copy(alpha = 0.4f))
                ) {
                    Text(
                        text = course.level,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = LinguaXAccentLight,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.clickable { isExpanded = !isExpanded }
                ) {
                    Text(
                        text = "${course.units.size} ${l10n.unitsCount}",
                        style = MaterialTheme.typography.labelMedium,
                        color = LinguaXTextSecondary
                    )
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = "Toggle",
                        tint = LinguaXTextSecondary
                    )
                }
            }

            Text(
                text = course.title,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Black,
                    fontSize = 19.sp
                ),
                color = LinguaXTextPrimary
            )

            Text(
                text = course.description,
                style = MaterialTheme.typography.bodyMedium,
                color = LinguaXTextSecondary
            )

            // Units & Lessons Dropdown Accordion
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    course.units.forEach { unit ->
                        UnitSectionView(
                            unit = unit,
                            l10n = l10n,
                            onLessonClicked = onLessonClicked
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun UnitSectionView(
    unit: UnitItem,
    l10n: L10nStrings,
    onLessonClicked: (Lesson) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF162136))
            .border(1.dp, Color(0xFF243552), RoundedCornerShape(16.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(LinguaXAccent)
            )
            Text(
                text = unit.title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                ),
                color = LinguaXAccentLight
            )
        }

        if (unit.description.isNotBlank()) {
            Text(
                text = unit.description,
                style = MaterialTheme.typography.bodySmall,
                color = LinguaXTextSecondary
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            unit.lessons.forEach { lesson ->
                LessonItemRow(
                    lesson = lesson,
                    l10n = l10n,
                    onClick = { onLessonClicked(lesson) }
                )
            }
        }
    }
}

@Composable
fun LessonItemRow(
    lesson: Lesson,
    l10n: L10nStrings,
    onClick: () -> Unit
) {
    val isLocked = lesson.status == LessonStatus.LOCKED
    val isCompleted = lesson.status == LessonStatus.COMPLETED

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = when {
            isCompleted -> Color(0xFF14242A)
            !isLocked -> Color(0xFF1C2C47)
            else -> Color(0xFF131A26)
        },
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            when {
                isCompleted -> LinguaXSuccess.copy(alpha = 0.4f)
                !isLocked -> LinguaXPrimary.copy(alpha = 0.5f)
                else -> Color(0xFF1E2838)
            }
        ),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("lesson_row_${lesson.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(
                            when {
                                isCompleted -> LinguaXGreenGradient
                                !isLocked -> LinguaXPrimaryGradient
                                else -> Brush.linearGradient(listOf(Color(0xFF23324D), Color(0xFF1A2436)))
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = when {
                            isCompleted -> Icons.Default.Check
                            !isLocked -> Icons.Default.PlayArrow
                            else -> Icons.Default.Lock
                        },
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }

                Column {
                    Text(
                        text = lesson.title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        ),
                        color = if (isLocked) LinguaXTextTertiary else LinguaXTextPrimary
                    )
                    Text(
                        text = "${lesson.durationMins} min • +${lesson.xpReward} XP",
                        style = MaterialTheme.typography.labelSmall,
                        color = LinguaXTextSecondary
                    )
                }
            }

            if (!isLocked) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (isCompleted) LinguaXSuccess.copy(alpha = 0.2f) else LinguaXPrimary,
                    modifier = Modifier.padding(start = 8.dp)
                ) {
                    Text(
                        text = if (isCompleted) l10n.completedLesson else l10n.startLesson,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}
