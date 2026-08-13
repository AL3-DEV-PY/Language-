package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.i18n.AppLanguage
import com.example.data.i18n.L10nStrings
import com.example.data.i18n.Translations
import com.example.data.model.*
import com.example.data.repository.LinguaXRepository
import com.example.data.repository.Resource
import com.example.data.repository.UserSession
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

class MainViewModel(
    private val repository: LinguaXRepository = LinguaXRepository()
) : ViewModel() {

    // App Interface Language (Arabic, English, French, Spanish, German, Italian, Turkish, Japanese, Korean)
    private val _appLanguage = MutableStateFlow(AppLanguage.ENGLISH)
    val appLanguage: StateFlow<AppLanguage> = _appLanguage.asStateFlow()

    private val _l10n = MutableStateFlow(Translations.get(AppLanguage.ENGLISH))
    val l10n: StateFlow<L10nStrings> = _l10n.asStateFlow()

    // Auth state
    private val _authState = MutableStateFlow<AuthState>(AuthState.Unauthenticated)
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

    // Selected Lesson Exercises State for Interactive Player
    private val _activeExercisesState = MutableStateFlow<Resource<List<Exercise>>>(Resource.Empty)
    val activeExercisesState: StateFlow<Resource<List<Exercise>>> = _activeExercisesState.asStateFlow()

    private val _selectedLessonForDialog = MutableStateFlow<Lesson?>(null)
    val selectedLessonForDialog: StateFlow<Lesson?> = _selectedLessonForDialog.asStateFlow()

    init {
        loadLanguages()
        loadCourses(selectedTargetLanguage.value.code)
        loadVocabulary(selectedTargetLanguage.value.code)
        loadChallenges()
        loadAchievements()
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
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            when (val res = repository.login(email, pass)) {
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
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            when (val res = repository.signup(email, pass, displayName)) {
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

    fun openLessonPreview(lesson: Lesson) {
        _selectedLessonForDialog.value = lesson
        viewModelScope.launch {
            _activeExercisesState.value = Resource.Loading
            _activeExercisesState.value = repository.getExercisesForLesson(lesson.id)
        }
    }

    fun dismissLessonPreview() {
        _selectedLessonForDialog.value = null
        _activeExercisesState.value = Resource.Empty
    }

    fun completeLesson(lesson: Lesson) {
        viewModelScope.launch {
            val xp = lesson.xpReward
            val coins = 10
            repository.recordLessonCompleted(lesson.id, xp, coins)
            // Reload courses and profile to reflect updated status
            loadCourses(selectedTargetLanguage.value.code)
            loadChallenges()
            loadAchievements()
            dismissLessonPreview()
        }
    }

    fun toggleVocabularyBookmark(vocab: VocabularyItem) {
        repository.toggleVocabularyBookmark(vocab.id)
        loadVocabulary(selectedTargetLanguage.value.code)
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
