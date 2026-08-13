package com.example.ui.screens.courses

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.i18n.L10nStrings
import com.example.data.model.Exercise
import com.example.data.model.Lesson
import com.example.data.repository.Resource
import com.example.ui.components.LinguaX3DButton
import com.example.ui.components.LinguaXProgressBar
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
    var currentExerciseIndex by remember { mutableIntStateOf(0) }
    var selectedOption by remember { mutableStateOf<String?>(null) }
    var isAnswerChecked by remember { mutableStateOf(false) }
    var isLessonFinished by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.85f))
                .padding(16.dp)
                .systemBarsPadding(),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .shadow(16.dp, RoundedCornerShape(26.dp))
                    .border(1.dp, LinguaXBorderGradient, RoundedCornerShape(26.dp)),
                shape = RoundedCornerShape(26.dp),
                color = LinguaXSurfaceElevated
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Header with Lesson Title & Close Button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = lesson.title,
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Black,
                                    fontSize = 18.sp
                                ),
                                color = LinguaXTextPrimary
                            )
                            Text(
                                text = "${lesson.durationMins} mins • +${lesson.xpReward} XP Reward",
                                style = MaterialTheme.typography.labelSmall,
                                color = LinguaXAccentLight
                            )
                        }

                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF1F2B45))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = LinguaXTextSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    if (isLessonFinished) {
                        // Lesson Completed Celebration View
                        LessonCompletionView(
                            lesson = lesson,
                            l10n = l10n,
                            onFinish = onCompleteLesson
                        )
                    } else {
                        // Interactive Exercise View
                        ResourceContainer(
                            resource = exercisesResource,
                            loadingText = l10n.loading,
                            emptyText = l10n.noDataAvailable
                        ) { exercises ->
                            if (exercises.isNotEmpty()) {
                                val currentExercise = exercises.getOrNull(currentExerciseIndex) ?: exercises.first()
                                val progress = (currentExerciseIndex + 1).toFloat() / exercises.size.toFloat()

                                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                                    // Progress Bar & Step Indicator
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Exercise ${currentExerciseIndex + 1} of ${exercises.size}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = LinguaXTextSecondary
                                        )
                                        Text(
                                            text = currentExercise.type.replace("_", " "),
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = LinguaXSecondaryLight
                                        )
                                    }

                                    LinguaXProgressBar(
                                        progress = progress,
                                        fillBrush = LinguaXAccentGradient,
                                        height = 6.dp
                                    )

                                    // Question Box
                                    Surface(
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(16.dp),
                                        color = Color(0xFF111A2B),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, LinguaXBorder)
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(16.dp),
                                            verticalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Text(
                                                text = currentExercise.question,
                                                style = MaterialTheme.typography.titleMedium.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 16.sp
                                                ),
                                                color = LinguaXTextPrimary
                                            )
                                        }
                                    }

                                    // Options List
                                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        currentExercise.options.forEach { option ->
                                            val isSelected = selectedOption == option
                                            val isCorrect = option == currentExercise.correctAnswer

                                            val containerColor = when {
                                                isAnswerChecked && isCorrect -> LinguaXSuccess.copy(alpha = 0.25f)
                                                isAnswerChecked && isSelected && !isCorrect -> LinguaXError.copy(alpha = 0.25f)
                                                isSelected -> LinguaXPrimary.copy(alpha = 0.35f)
                                                else -> Color(0xFF152033)
                                            }

                                            val borderColor = when {
                                                isAnswerChecked && isCorrect -> LinguaXSuccess
                                                isAnswerChecked && isSelected && !isCorrect -> LinguaXError
                                                isSelected -> LinguaXPrimary
                                                else -> LinguaXBorder
                                            }

                                            Surface(
                                                onClick = {
                                                    if (!isAnswerChecked) {
                                                        selectedOption = option
                                                    }
                                                },
                                                shape = RoundedCornerShape(14.dp),
                                                color = containerColor,
                                                border = androidx.compose.foundation.BorderStroke(1.dp, borderColor),
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .testTag("option_${option.take(5)}")
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.SpaceBetween
                                                ) {
                                                    Text(
                                                        text = option,
                                                        style = MaterialTheme.typography.bodyLarge.copy(
                                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                                        ),
                                                        color = LinguaXTextPrimary
                                                    )
                                                    if (isAnswerChecked && isCorrect) {
                                                        Icon(
                                                            imageVector = Icons.Default.CheckCircle,
                                                            contentDescription = "Correct",
                                                            tint = LinguaXSuccess,
                                                            modifier = Modifier.size(20.dp)
                                                        )
                                                    } else if (isAnswerChecked && isSelected && !isCorrect) {
                                                        Icon(
                                                            imageVector = Icons.Default.Cancel,
                                                            contentDescription = "Incorrect",
                                                            tint = LinguaXError,
                                                            modifier = Modifier.size(20.dp)
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    // Feedback explanation banner
                                    AnimatedVisibility(visible = isAnswerChecked) {
                                        val isCorrect = selectedOption == currentExercise.correctAnswer
                                        Surface(
                                            shape = RoundedCornerShape(12.dp),
                                            color = if (isCorrect) Color(0xFF0F2B1F) else Color(0xFF2E1218),
                                            border = androidx.compose.foundation.BorderStroke(
                                                1.dp,
                                                if (isCorrect) LinguaXSuccess else LinguaXError
                                            ),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Column(modifier = Modifier.padding(12.dp)) {
                                                Text(
                                                    text = if (isCorrect) l10n.correctMessage else l10n.incorrectMessage,
                                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                                    color = if (isCorrect) LinguaXSuccessLight else LinguaXErrorLight
                                                )
                                                if (!currentExercise.explanation.isNullOrBlank()) {
                                                    Spacer(modifier = Modifier.height(4.dp))
                                                    Text(
                                                        text = currentExercise.explanation,
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = LinguaXTextSecondary
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    // Action Button (Check Answer -> Next Exercise)
                                    if (!isAnswerChecked) {
                                        LinguaX3DButton(
                                            text = l10n.checkAnswer,
                                            enabled = selectedOption != null,
                                            onClick = {
                                                isAnswerChecked = true
                                            },
                                            testTag = "check_answer_button"
                                        )
                                    } else {
                                        LinguaX3DButton(
                                            text = if (currentExerciseIndex + 1 < exercises.size) l10n.nextQuestion else l10n.finishLesson,
                                            gradient = LinguaXGreenGradient,
                                            onClick = {
                                                if (currentExerciseIndex + 1 < exercises.size) {
                                                    currentExerciseIndex++
                                                    selectedOption = null
                                                    isAnswerChecked = false
                                                } else {
                                                    isLessonFinished = true
                                                }
                                            },
                                            testTag = "next_exercise_button"
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LessonCompletionView(
    lesson: Lesson,
    l10n: L10nStrings,
    onFinish: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(LinguaXGreenGradient)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.EmojiEvents,
                contentDescription = "Success",
                tint = Color.White,
                modifier = Modifier.size(40.dp)
            )
        }

        Text(
            text = l10n.lessonCompletedTitle,
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Black,
                fontSize = 21.sp
            ),
            color = LinguaXTextPrimary,
            textAlign = TextAlign.Center
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Surface(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(14.dp),
                color = Color(0xFF111E30),
                border = androidx.compose.foundation.BorderStroke(1.dp, LinguaXAccent.copy(alpha = 0.4f))
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "+${lesson.xpReward}",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Black,
                            fontSize = 20.sp
                        ),
                        color = LinguaXAccent
                    )
                    Text(
                        text = l10n.xpEarned,
                        style = MaterialTheme.typography.labelSmall,
                        color = LinguaXTextSecondary
                    )
                }
            }

            Surface(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(14.dp),
                color = Color(0xFF261D12),
                border = androidx.compose.foundation.BorderStroke(1.dp, LinguaXWarning.copy(alpha = 0.4f))
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "+10",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Black,
                            fontSize = 20.sp
                        ),
                        color = LinguaXWarning
                    )
                    Text(
                        text = l10n.coinsEarned,
                        style = MaterialTheme.typography.labelSmall,
                        color = LinguaXTextSecondary
                    )
                }
            }
        }

        LinguaX3DButton(
            text = l10n.finishLesson,
            icon = Icons.AutoMirrored.Filled.ArrowForward,
            gradient = LinguaXPrimaryGradient,
            onClick = onFinish,
            testTag = "finish_lesson_button"
        )
    }
}
