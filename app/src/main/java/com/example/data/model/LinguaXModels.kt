package com.example.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Profile(
    @Json(name = "id") val id: String,
    @Json(name = "username") val username: String? = null,
    @Json(name = "display_name") val displayName: String? = "Learner",
    @Json(name = "avatar_url") val avatarUrl: String? = null,
    @Json(name = "xp") val xp: Int = 120,
    @Json(name = "coins") val coins: Int = 45,
    @Json(name = "streak") val streak: Int = 3,
    @Json(name = "daily_goal") val dailyGoal: Int = 20,
    @Json(name = "created_at") val createdAt: String? = null,
    @Json(name = "updated_at") val updatedAt: String? = null
)

@JsonClass(generateAdapter = true)
data class LanguageItem(
    @Json(name = "id") val id: String,
    @Json(name = "name") val name: String,
    @Json(name = "code") val code: String,
    @Json(name = "flag_emoji") val flagEmoji: String,
    @Json(name = "icon_url") val iconUrl: String? = null,
    @Json(name = "description") val description: String? = null,
    @Json(name = "learners_count") val learnersCount: Int = 1000
)

@JsonClass(generateAdapter = true)
data class Course(
    @Json(name = "id") val id: String,
    @Json(name = "language_id") val languageId: String,
    @Json(name = "title") val title: String,
    @Json(name = "description") val description: String,
    @Json(name = "level") val level: String = "A1 Beginner",
    @Json(name = "total_lessons") val totalLessons: Int = 12,
    @Json(name = "order_index") val orderIndex: Int = 1,
    val units: List<UnitItem> = emptyList()
)

@JsonClass(generateAdapter = true)
data class UnitItem(
    @Json(name = "id") val id: String,
    @Json(name = "course_id") val courseId: String,
    @Json(name = "title") val title: String,
    @Json(name = "description") val description: String,
    @Json(name = "order_index") val orderIndex: Int = 1,
    val lessons: List<Lesson> = emptyList()
)

enum class LessonStatus {
    COMPLETED,
    CURRENT,
    LOCKED
}

@JsonClass(generateAdapter = true)
data class Lesson(
    @Json(name = "id") val id: String,
    @Json(name = "unit_id") val unitId: String,
    @Json(name = "title") val title: String,
    @Json(name = "description") val description: String,
    @Json(name = "xp_reward") val xpReward: Int = 15,
    @Json(name = "duration_mins") val durationMins: Int = 5,
    @Json(name = "order_index") val orderIndex: Int = 1,
    val status: LessonStatus = LessonStatus.LOCKED,
    val exercisesCount: Int = 5
)

enum class ExerciseType {
    MULTIPLE_CHOICE,
    VOCABULARY,
    LISTENING,
    TRANSLATION
}

@JsonClass(generateAdapter = true)
data class Exercise(
    @Json(name = "id") val id: String,
    @Json(name = "lesson_id") val lessonId: String,
    @Json(name = "type") val type: String = "MULTIPLE_CHOICE",
    @Json(name = "question") val question: String,
    @Json(name = "options") val options: List<String> = emptyList(),
    @Json(name = "correct_answer") val correctAnswer: String,
    @Json(name = "explanation") val explanation: String? = null,
    val audioUrl: String? = null
)

@JsonClass(generateAdapter = true)
data class VocabularyItem(
    @Json(name = "id") val id: String,
    @Json(name = "word") val word: String,
    @Json(name = "translation") val translation: String,
    @Json(name = "phonetic") val phonetic: String? = null,
    @Json(name = "part_of_speech") val partOfSpeech: String = "Noun",
    @Json(name = "example_sentence") val exampleSentence: String? = null,
    @Json(name = "language_code") val languageCode: String = "en",
    val masteryLevel: Int = 2, // 1 to 5
    val isBookmarked: Boolean = false
)

@JsonClass(generateAdapter = true)
data class DailyChallenge(
    @Json(name = "id") val id: String,
    @Json(name = "title") val title: String,
    @Json(name = "description") val description: String,
    @Json(name = "reward_xp") val rewardXp: Int = 20,
    @Json(name = "reward_coins") val rewardCoins: Int = 10,
    val currentProgress: Int = 1,
    val target: Int = 3,
    val isCompleted: Boolean = false
)

@JsonClass(generateAdapter = true)
data class AchievementItem(
    @Json(name = "id") val id: String,
    @Json(name = "title") val title: String,
    @Json(name = "description") val description: String,
    @Json(name = "icon") val iconName: String = "star",
    @Json(name = "category") val category: String = "Streak",
    val isUnlocked: Boolean = true,
    val unlockedAt: String? = "2026-08-10",
    val progress: Int = 5,
    val maxProgress: Int = 5
)

@JsonClass(generateAdapter = true)
data class LeaderboardEntry(
    @Json(name = "user_id") val userId: String,
    @Json(name = "username") val username: String,
    @Json(name = "display_name") val displayName: String,
    @Json(name = "avatar_url") val avatarUrl: String? = null,
    @Json(name = "xp") val xp: Int,
    @Json(name = "rank") val rank: Int
)
