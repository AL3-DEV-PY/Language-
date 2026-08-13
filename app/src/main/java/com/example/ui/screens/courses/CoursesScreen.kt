package com.example.ui.screens.courses

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.i18n.L10nStrings
import com.example.data.model.*
import com.example.data.repository.Resource
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
            subtitle = "${selectedTargetLanguage.flagEmoji} ${selectedTargetLanguage.name}"
        )

        // Language Pills Selector
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
                    val isSelected = lang.code == selectedTargetLanguage.code
                    FilterChip(
                        selected = isSelected,
                        onClick = { onLanguageSelected(lang) },
                        label = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(text = lang.flagEmoji, fontSize = 16.sp)
                                Text(text = lang.name, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
                            }
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = LinguaXPrimary,
                            selectedLabelColor = Color.White
                        ),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.testTag("lang_chip_${lang.code}")
                    )
                }
            }
        }

        // Courses & Units Hierarchy
        ResourceContainer(
            resource = coursesResource,
            loadingText = l10n.loading,
            emptyText = l10n.noDataAvailable
        ) { courses ->
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(courses) { course ->
                    CourseCard(
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
private fun CourseCard(
    course: Course,
    l10n: L10nStrings,
    onLessonClicked: (Lesson) -> Unit
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Surface(
                        shape = CircleShape,
                        color = LinguaXPrimaryContainer,
                        modifier = Modifier.padding(bottom = 6.dp)
                    ) {
                        Text(
                            text = course.level,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = LinguaXOnPrimaryContainer
                            ),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                    Text(
                        text = course.title,
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }

            Text(
                text = course.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)

            // Units Breakdown
            course.units.forEach { unit ->
                UnitSection(
                    unit = unit,
                    l10n = l10n,
                    onLessonClicked = onLessonClicked
                )
            }
        }
    }
}

@Composable
private fun UnitSection(
    unit: UnitItem,
    l10n: L10nStrings,
    onLessonClicked: (Lesson) -> Unit
) {
    var isExpanded by remember { mutableStateOf(true) }

    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { isExpanded = !isExpanded }
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = unit.title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = LinguaXPrimary
                    )
                )
                Text(
                    text = unit.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                contentDescription = "Toggle Unit",
                tint = LinguaXPrimary
            )
        }

        AnimatedVisibility(
            visible = isExpanded,
            enter = expandVertically(),
            exit = shrinkVertically()
        ) {
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
}

@Composable
private fun LessonItemRow(
    lesson: Lesson,
    l10n: L10nStrings,
    onClick: () -> Unit
) {
    val isLocked = lesson.status == LessonStatus.LOCKED

    Surface(
        onClick = { if (!isLocked) onClick() },
        enabled = true,
        shape = RoundedCornerShape(16.dp),
        color = when (lesson.status) {
            LessonStatus.COMPLETED -> Color(0xFFECFDF5)
            LessonStatus.CURRENT -> LinguaXPrimaryContainer
            LessonStatus.LOCKED -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        },
        modifier = Modifier
            .fillMaxWidth()
            .testTag("lesson_row_${lesson.id}")
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = when (lesson.status) {
                    LessonStatus.COMPLETED -> LinguaXSuccessGreen
                    LessonStatus.CURRENT -> LinguaXPrimary
                    LessonStatus.LOCKED -> Color.Gray
                },
                modifier = Modifier.size(36.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = when (lesson.status) {
                            LessonStatus.COMPLETED -> Icons.Default.Check
                            LessonStatus.CURRENT -> Icons.Default.PlayArrow
                            LessonStatus.LOCKED -> Icons.Default.Lock
                        },
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = lesson.title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    ),
                    color = if (isLocked) Color.Gray else MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${lesson.durationMins} mins • ${lesson.xpReward} XP",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Text(
                text = when (lesson.status) {
                    LessonStatus.COMPLETED -> l10n.completedLesson
                    LessonStatus.CURRENT -> l10n.startLesson
                    LessonStatus.LOCKED -> l10n.lockedLesson
                },
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = when (lesson.status) {
                    LessonStatus.COMPLETED -> LinguaXSuccessGreen
                    LessonStatus.CURRENT -> LinguaXPrimary
                    LessonStatus.LOCKED -> Color.Gray
                }
            )
        }
    }
}
