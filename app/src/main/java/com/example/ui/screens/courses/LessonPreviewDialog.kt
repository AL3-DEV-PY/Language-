package com.example.ui.screens.courses

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.i18n.L10nStrings
import com.example.data.model.Exercise
import com.example.data.model.Lesson
import com.example.data.repository.Resource
import com.example.ui.components.ResourceContainer
import com.example.ui.theme.*

@Composable
fun LessonPreviewDialog(
    lesson: Lesson,
    l10n: L10nStrings,
    exercisesResource: Resource<List<Exercise>>,
    onDismiss: () -> Unit,
    onCompleteLesson: () -> Unit
) {
    val scrollState = rememberScrollState()

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(28.dp),
        containerColor = Color.White,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = lesson.title,
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "${lesson.durationMins} mins • +${lesson.xpReward} XP Reward",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 420.dp)
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = lesson.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)

                Text(
                    text = "Interactive Lesson Exercises Architecture:",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = LinguaXPrimary
                )

                ResourceContainer(
                    resource = exercisesResource,
                    loadingText = "Loading lesson exercises...",
                    emptyText = "No exercises configured for this lesson."
                ) { exercises ->
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        exercises.forEachIndexed { index, exercise ->
                            ExercisePreviewCard(
                                index = index + 1,
                                exercise = exercise
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onCompleteLesson()
                    onDismiss()
                },
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = LinguaXPrimary),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("dialog_complete_lesson_button")
            ) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Complete & Claim +${lesson.xpReward} XP",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
        }
    )
}

@Composable
private fun ExercisePreviewCard(
    index: Int,
    exercise: Exercise
) {
    var selectedOption by remember { mutableStateOf<String?>(null) }
    var isSubmitted by remember { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = LinguaXSurfaceVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Question $index • ${exercise.type}",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = LinguaXPrimary
                    )
                )

                IconButton(
                    onClick = { /* Simulated Audio Pronunciation */ },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.VolumeUp,
                        contentDescription = "Audio",
                        tint = LinguaXPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Text(
                text = exercise.question,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp
                )
            )

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                exercise.options.forEach { option ->
                    val isSelected = option == selectedOption
                    val isCorrect = option == exercise.correctAnswer

                    Surface(
                        onClick = {
                            selectedOption = option
                            isSubmitted = true
                        },
                        shape = RoundedCornerShape(12.dp),
                        color = when {
                            isSubmitted && isSelected && isCorrect -> LinguaXSuccessGreen.copy(alpha = 0.2f)
                            isSubmitted && isSelected && !isCorrect -> MaterialTheme.colorScheme.errorContainer
                            isSelected -> LinguaXPrimaryContainer
                            else -> Color.White
                        },
                        border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, LinguaXPrimary) else null,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = option,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            )
                            if (isSubmitted && isSelected) {
                                Icon(
                                    imageVector = if (isCorrect) Icons.Default.CheckCircle else Icons.Default.Cancel,
                                    contentDescription = null,
                                    tint = if (isCorrect) LinguaXSuccessGreen else MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }

            if (isSubmitted && !exercise.explanation.isNullOrBlank()) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = LinguaXPrimaryContainer.copy(alpha = 0.6f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "💡 ${exercise.explanation}",
                        style = MaterialTheme.typography.bodySmall,
                        color = LinguaXOnPrimaryContainer,
                        modifier = Modifier.padding(10.dp)
                    )
                }
            }
        }
    }
}
