package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.i18n.AppLanguage
import com.example.data.i18n.L10nStrings
import com.example.data.i18n.Translations
import com.example.data.model.*
import com.example.data.repository.LinguaXRepository
import com.example.data.repository.Resource
import com.example.data.repository.UserSession
import com.example.data.supabase.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class AuthState {
    object Unauthenticated : AuthState()
    object Loading : AuthState()
    data class Authenticated(val session: UserSession) : AuthState()
    data class Error(val message: String) : AuthState()
}

sealed class LessonUiState {
    object Idle : LessonUiState()
    object LoadingExercises : LessonUiState()
    data class Playing(
        val lesson: Lesson,
        val exercises: List<Exercise>,
        val currentExerciseIndex: Int = 0,
        val selectedOption: String? = null,
        val isAnswerChecked: Boolean = false,
        val isCurrentAnswerCorrect: Boolean = false,
        val correctCount: Int = 0,
        val incorrectCount: Int = 0,
        val lives: Int = 3,
        val combo: Int = 0,
        val isSaving: Boolean = false
    ) : LessonUiState()
    data class Completed(
        val lesson: Lesson,
        val xpEarned: Int,
        val coinsEarned: Int,
        val correctCount: Int,
        val incorrectCount: Int,
        val accuracyPercent: Int,
        val totalExercises: Int
    ) : LessonUiState()
    data class Error(val message: String, val lesson: Lesson? = null) : LessonUiState()
}

sealed interface FlashcardUiState {
    object Idle : FlashcardUiState
    data class Reviewing(
        val items: List<VocabularyItem>,
        val currentIndex: Int,
        val isFlipped: Boolean = false,
        val knownCount: Int = 0,
        val reviewCount: Int = 0,
        val isSmartReview: Boolean = false
    ) : FlashcardUiState
    data class Completed(
        val totalReviewed: Int,
        val knownCount: Int,
        val reviewCount: Int,
        val isSmartReview: Boolean
    ) : FlashcardUiState
}

class MainViewModel @JvmOverloads constructor(
    application: Application,
    private val repository: LinguaXRepository = LinguaXRepository(SessionManager(application))
) : AndroidViewModel(application) {

    // App Interface Language
    private val _appLanguage = MutableStateFlow(AppLanguage.ENGLISH)
    val appLanguage: StateFlow<AppLanguage> = _appLanguage.asStateFlow()

    private val _l10n = MutableStateFlow(Translations.get(AppLanguage.ENGLISH))
    val l10n: StateFlow<L10nStrings> = _l10n.asStateFlow()

    // Auth state synchronized continuously with repository session
    private val _authState = MutableStateFlow<AuthState>(
        repository.currentSession.value?.let { AuthState.Authenticated(it) } ?: AuthState.Unauthenticated
    )
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    // Target Languages
    private val _languagesState = MutableStateFlow<Resource<List<LanguageItem>>>(Resource.Loading)
    val languagesState: StateFlow<Resource<List<LanguageItem>>> = _languagesState.asStateFlow()

    val selectedTargetLanguage = repository.selectedLanguage

    // Courses & Units State
    private val _coursesState = MutableStateFlow<Resource<List<Course>>>(Resource.Loading)
    val coursesState: StateFlow<Resource<List<Course>>> = _coursesState.asStateFlow()

    // Vocabulary State
    private val _vocabularyState = MutableStateFlow<Resource<List<VocabularyItem>>>(Resource.Loading)
    val vocabularyState: StateFlow<Resource<List<VocabularyItem>>> = _vocabularyState.asStateFlow()

    // Daily Challenges State
    private val _challengesState = MutableStateFlow<Resource<List<DailyChallenge>>>(Resource.Loading)
    val challengesState: StateFlow<Resource<List<DailyChallenge>>> = _challengesState.asStateFlow()

    // Achievements State
    private val _achievementsState = MutableStateFlow<Resource<List<AchievementItem>>>(Resource.Loading)
    val achievementsState: StateFlow<Resource<List<AchievementItem>>> = _achievementsState.asStateFlow()

    // Leaderboard State
    private val _leaderboardState = MutableStateFlow<Resource<List<LeaderboardEntry>>>(Resource.Loading)
    val leaderboardState: StateFlow<Resource<List<LeaderboardEntry>>> = _leaderboardState.asStateFlow()

    // Dedicated Full-Screen Lesson State Machine
    private val _lessonState = MutableStateFlow<LessonUiState>(LessonUiState.Idle)
    val lessonState: StateFlow<LessonUiState> = _lessonState.asStateFlow()

    // Dedicated Flashcard Review State Machine
    private val _flashcardState = MutableStateFlow<FlashcardUiState>(FlashcardUiState.Idle)
    val flashcardState: StateFlow<FlashcardUiState> = _flashcardState.asStateFlow()

    // Rapid-tap & Save Protection Guard
    private var isCompletionInFlight = false

    init {
        // Automatically sync authState whenever session changes in repository (e.g. XP gained, profile updated)
        viewModelScope.launch {
            repository.currentSession.collect { session ->
                if (session != null) {
                    _authState.value = AuthState.Authenticated(session)
                } else {
                    if (_authState.value !is AuthState.Loading && _authState.value !is AuthState.Error) {
                        _authState.value = AuthState.Unauthenticated
                    }
                }
            }
        }

        loadLanguages()
        loadCourses(selectedTargetLanguage.value.code)
        loadVocabulary(selectedTargetLanguage.value.code)
        loadChallenges()
        loadAchievements()
        loadLeaderboard()
    }

    fun setAppLanguage(language: AppLanguage) {
        _appLanguage.value = language
        _l10n.value = Translations.get(language)
    }

    fun setSelectedTargetLanguage(language: LanguageItem) {
        repository.setSelectedLanguage(language)
        loadCourses(language.code)
        loadVocabulary(language.code)
    }

    fun login(email: String, pass: String) {
        val trimmedEmail = email.trim()
        val trimmedPass = pass.trim()
        if (trimmedEmail.isBlank() || trimmedPass.isBlank()) {
            _authState.value = AuthState.Error("Please enter both email and password.")
            return
        }
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            when (val res = repository.login(trimmedEmail, trimmedPass)) {
                is Resource.Success -> {
                    _authState.value = AuthState.Authenticated(res.data)
                }
                is Resource.Error -> {
                    _authState.value = AuthState.Error(res.message)
                }
                else -> {}
            }
        }
    }

    fun signup(email: String, pass: String, displayName: String) {
        val trimmedEmail = email.trim()
        val trimmedPass = pass.trim()
        val trimmedName = displayName.trim()
        if (trimmedEmail.isBlank() || trimmedPass.isBlank() || trimmedName.isBlank()) {
            _authState.value = AuthState.Error("Please fill in all required fields.")
            return
        }
        if (trimmedPass.length < 6) {
            _authState.value = AuthState.Error("Password must be at least 6 characters.")
            return
        }
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            when (val res = repository.signup(trimmedEmail, trimmedPass, trimmedName)) {
                is Resource.Success -> {
                    _authState.value = AuthState.Authenticated(res.data)
                }
                is Resource.Error -> {
                    _authState.value = AuthState.Error(res.message)
                }
                else -> {}
            }
        }
    }

    fun clearAuthError() {
        if (_authState.value is AuthState.Error) {
            _authState.value = AuthState.Unauthenticated
        }
    }

    fun continueAsGuest() {
        login("learner@linguax.com", "demo123456")
    }

    fun logout() {
        repository.logout()
        _authState.value = AuthState.Unauthenticated
    }

    fun loadLanguages() {
        viewModelScope.launch {
            _languagesState.value = Resource.Loading
            _languagesState.value = repository.getLanguages()
        }
    }

    fun loadCourses(languageCode: String) {
        viewModelScope.launch {
            _coursesState.value = Resource.Loading
            _coursesState.value = repository.getCourses(languageCode)
        }
    }

    fun loadVocabulary(languageCode: String) {
        viewModelScope.launch {
            _vocabularyState.value = Resource.Loading
            _vocabularyState.value = repository.getVocabulary(languageCode)
        }
    }

    fun loadChallenges() {
        viewModelScope.launch {
            _challengesState.value = Resource.Loading
            _challengesState.value = repository.getDailyChallenges()
        }
    }

    fun loadAchievements() {
        viewModelScope.launch {
            _achievementsState.value = Resource.Loading
            _achievementsState.value = repository.getAchievements()
        }
    }

    fun loadLeaderboard() {
        viewModelScope.launch {
            _leaderboardState.value = Resource.Loading
            _leaderboardState.value = repository.getLeaderboard()
        }
    }

    fun openLesson(lesson: Lesson) {
        viewModelScope.launch {
            _lessonState.value = LessonUiState.LoadingExercises
            when (val res = repository.getExercisesForLesson(lesson.id)) {
                is Resource.Success -> {
                    if (res.data.isNotEmpty()) {
                        _lessonState.value = LessonUiState.Playing(
                            lesson = lesson,
                            exercises = res.data,
                            currentExerciseIndex = 0,
                            selectedOption = null,
                            isAnswerChecked = false,
                            isCurrentAnswerCorrect = false,
                            correctCount = 0,
                            incorrectCount = 0,
                            lives = 3,
                            combo = 0,
                            isSaving = false
                        )
                    } else {
                        _lessonState.value = LessonUiState.Error("No exercises found for this lesson.", lesson)
                    }
                }
                is Resource.Error -> {
                    _lessonState.value = LessonUiState.Error(res.message, lesson)
                }
                else -> {
                    _lessonState.value = LessonUiState.Error("Unable to load exercises.", lesson)
                }
            }
        }
    }

    // Alias for compatibility
    fun openLessonPreview(lesson: Lesson) {
        openLesson(lesson)
    }

    fun selectLessonOption(option: String) {
        val current = _lessonState.value
        if (current is LessonUiState.Playing && !current.isAnswerChecked && !current.isSaving) {
            _lessonState.value = current.copy(selectedOption = option)
        }
    }

    fun checkLessonAnswer() {
        val current = _lessonState.value
        if (current is LessonUiState.Playing && current.selectedOption != null && !current.isAnswerChecked) {
            val currentExercise = current.exercises.getOrNull(current.currentExerciseIndex) ?: return
            val isCorrect = current.selectedOption.trim().equals(currentExercise.correctAnswer.trim(), ignoreCase = true)

            _lessonState.value = current.copy(
                isAnswerChecked = true,
                isCurrentAnswerCorrect = isCorrect,
                correctCount = if (isCorrect) current.correctCount + 1 else current.correctCount,
                incorrectCount = if (!isCorrect) current.incorrectCount + 1 else current.incorrectCount,
                lives = if (!isCorrect) maxOf(0, current.lives - 1) else current.lives,
                combo = if (isCorrect) current.combo + 1 else 0
            )
        }
    }

    fun proceedLessonExercise() {
        val current = _lessonState.value
        if (current is LessonUiState.Playing && current.isAnswerChecked && !current.isSaving) {
            if (current.currentExerciseIndex + 1 < current.exercises.size) {
                // Advance to next exercise
                _lessonState.value = current.copy(
                    currentExerciseIndex = current.currentExerciseIndex + 1,
                    selectedOption = null,
                    isAnswerChecked = false,
                    isCurrentAnswerCorrect = false
                )
            } else {
                // Final exercise completed -> Complete Lesson & Save
                completeActiveLesson(current)
            }
        }
    }

    private fun completeActiveLesson(playing: LessonUiState.Playing) {
        if (isCompletionInFlight) return
        isCompletionInFlight = true

        _lessonState.value = playing.copy(isSaving = true)

        viewModelScope.launch {
            try {
                val lesson = playing.lesson
                val res = repository.completeLesson(lesson.id)
                if (res is Resource.Success) {
                    val total = playing.exercises.size
                    val correct = playing.correctCount
                    val accuracy = if (total > 0) ((correct.toFloat() / total.toFloat()) * 100).toInt() else 100

                    val completionData = res.data

                    // Sync authenticated session state if present
                    val auth = _authState.value
                    if (auth is AuthState.Authenticated && completionData.profile != null) {
                        _authState.value = AuthState.Authenticated(
                            auth.session.copy(profile = completionData.profile)
                        )
                    }

                    _lessonState.value = LessonUiState.Completed(
                        lesson = lesson,
                        xpEarned = completionData.xpEarned,
                        coinsEarned = completionData.coinsEarned,
                        correctCount = correct,
                        incorrectCount = playing.incorrectCount,
                        accuracyPercent = accuracy,
                        totalExercises = total
                    )

                    // Reload courses, daily challenges, and leaderboard to refresh badges/indicators
                    loadCourses(selectedTargetLanguage.value.code)
                    loadChallenges()
                    loadAchievements()
                    loadLeaderboard()
                } else if (res is Resource.Error) {
                    _lessonState.value = LessonUiState.Error("Failed to save progress: ${res.message}", lesson)
                } else {
                    _lessonState.value = LessonUiState.Error("Unexpected response while saving progress.", lesson)
                }
            } catch (e: Exception) {
                _lessonState.value = LessonUiState.Error("Network error while saving progress: ${e.localizedMessage}", playing.lesson)
            } finally {
                isCompletionInFlight = false
            }
        }
    }

    fun retrySaveLesson() {
        val current = _lessonState.value
        if (current is LessonUiState.Error && current.lesson != null) {
            val dummyPlaying = LessonUiState.Playing(
                lesson = current.lesson,
                exercises = emptyList(),
                correctCount = 5,
                incorrectCount = 0
            )
            completeActiveLesson(dummyPlaying)
        }
    }

    fun exitLesson() {
        _lessonState.value = LessonUiState.Idle
    }

    fun toggleVocabularyBookmark(vocab: VocabularyItem) {
        repository.toggleVocabularyBookmark(vocab.id)
        loadVocabulary(selectedTargetLanguage.value.code)
    }

    fun startFlashcardReview(items: List<VocabularyItem>? = null, isSmartReview: Boolean = false) {
        val listToReview = if (!items.isNullOrEmpty()) {
            items
        } else {
            val currentVocab = (vocabularyState.value as? Resource.Success)?.data ?: emptyList()
            if (isSmartReview) {
                val bookmarked = currentVocab.filter { it.isBookmarked }
                if (bookmarked.isNotEmpty()) bookmarked else currentVocab.take(5)
            } else {
                currentVocab
            }
        }
        if (listToReview.isNotEmpty()) {
            _flashcardState.value = FlashcardUiState.Reviewing(
                items = listToReview,
                currentIndex = 0,
                isFlipped = false,
                knownCount = 0,
                reviewCount = 0,
                isSmartReview = isSmartReview
            )
        }
    }

    fun flipFlashcard() {
        val current = _flashcardState.value
        if (current is FlashcardUiState.Reviewing) {
            _flashcardState.value = current.copy(isFlipped = !current.isFlipped)
        }
    }

    fun recordFlashcardRating(known: Boolean) {
        val current = _flashcardState.value
        if (current is FlashcardUiState.Reviewing) {
            val newKnown = if (known) current.knownCount + 1 else current.knownCount
            val newReview = if (!known) current.reviewCount + 1 else current.reviewCount
            val nextIndex = current.currentIndex + 1
            if (nextIndex < current.items.size) {
                _flashcardState.value = current.copy(
                    currentIndex = nextIndex,
                    isFlipped = false,
                    knownCount = newKnown,
                    reviewCount = newReview
                )
            } else {
                _flashcardState.value = FlashcardUiState.Completed(
                    totalReviewed = current.items.size,
                    knownCount = newKnown,
                    reviewCount = newReview,
                    isSmartReview = current.isSmartReview
                )
            }
        }
    }

    fun exitFlashcards() {
        _flashcardState.value = FlashcardUiState.Idle
    }

    fun updateProfile(newDisplayName: String, newGoal: Int) {
        val currentSession = (authState.value as? AuthState.Authenticated)?.session ?: return
        val updatedProfile = currentSession.profile.copy(
            displayName = newDisplayName,
            dailyGoal = newGoal
        )
        viewModelScope.launch {
            val res = repository.updateProfile(updatedProfile)
            if (res is Resource.Success) {
                _authState.value = AuthState.Authenticated(
                    currentSession.copy(profile = res.data)
                )
            }
        }
    }
}
