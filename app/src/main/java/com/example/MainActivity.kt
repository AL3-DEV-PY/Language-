package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.i18n.L10nStrings
import com.example.ui.screens.achievements.AchievementsScreen
import com.example.ui.screens.auth.LandingScreen
import com.example.ui.screens.auth.LoginScreen
import com.example.ui.screens.auth.SignupScreen
import com.example.ui.screens.challenges.ChallengesScreen
import com.example.ui.screens.courses.CoursesScreen
import com.example.ui.screens.courses.LessonPreviewDialog
import com.example.ui.screens.home.HomeScreen
import com.example.ui.screens.vocabulary.VocabularyScreen
import com.example.ui.theme.LinguaXPrimary
import com.example.ui.theme.LinguaXTheme
import com.example.ui.viewmodel.AuthState
import com.example.ui.viewmodel.MainViewModel

enum class AuthSubScreen {
    LANDING, LOGIN, SIGNUP
}

enum class MainTab(val labelKey: String, val icon: ImageVector) {
    HOME("homeTab", Icons.Default.Home),
    COURSES("coursesTab", Icons.Default.School),
    VOCABULARY("vocabularyTab", Icons.Default.MenuBook),
    CHALLENGES("challengesTab", Icons.Default.Flag),
    ACHIEVEMENTS("achievementsTab", Icons.Default.EmojiEvents)
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LinguaXTheme {
                LinguaXApp()
            }
        }
    }
}

@Composable
fun LinguaXApp(viewModel: MainViewModel = viewModel()) {
    val l10n by viewModel.l10n.collectAsStateWithLifecycle()
    val authState by viewModel.authState.collectAsStateWithLifecycle()

    var authSubScreen by remember { mutableStateOf(AuthSubScreen.LANDING) }
    var currentTab by remember { mutableStateOf(MainTab.HOME) }

    val selectedTargetLanguage by viewModel.selectedTargetLanguage.collectAsStateWithLifecycle()
    val languagesResource by viewModel.languagesState.collectAsStateWithLifecycle()
    val coursesResource by viewModel.coursesState.collectAsStateWithLifecycle()
    val vocabularyResource by viewModel.vocabularyState.collectAsStateWithLifecycle()
    val challengesResource by viewModel.challengesState.collectAsStateWithLifecycle()
    val achievementsResource by viewModel.achievementsState.collectAsStateWithLifecycle()
    val selectedLessonForDialog by viewModel.selectedLessonForDialog.collectAsStateWithLifecycle()
    val activeExercisesResource by viewModel.activeExercisesState.collectAsStateWithLifecycle()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        when (val state = authState) {
            is AuthState.Unauthenticated, is AuthState.Loading, is AuthState.Error -> {
                when (authSubScreen) {
                    AuthSubScreen.LANDING -> {
                        LandingScreen(
                            l10n = l10n,
                            onNavigateToLogin = { authSubScreen = AuthSubScreen.LOGIN },
                            onNavigateToSignup = { authSubScreen = AuthSubScreen.SIGNUP }
                        )
                    }
                    AuthSubScreen.LOGIN -> {
                        LoginScreen(
                            l10n = l10n,
                            authState = state,
                            onLoginSubmit = { email, pass -> viewModel.login(email, pass) },
                            onNavigateToSignup = { authSubScreen = AuthSubScreen.SIGNUP },
                            onBack = { authSubScreen = AuthSubScreen.LANDING }
                        )
                    }
                    AuthSubScreen.SIGNUP -> {
                        SignupScreen(
                            l10n = l10n,
                            authState = state,
                            onSignupSubmit = { email, pass, name -> viewModel.signup(email, pass, name) },
                            onNavigateToLogin = { authSubScreen = AuthSubScreen.LOGIN },
                            onBack = { authSubScreen = AuthSubScreen.LANDING }
                        )
                    }
                }
            }
            is AuthState.Authenticated -> {
                val profile = state.session.profile

                Scaffold(
                    bottomBar = {
                        NavigationBar(
                            containerColor = MaterialTheme.colorScheme.surface,
                            tonalElevation = 8.dp
                        ) {
                            MainTab.values().forEach { tab ->
                                val selected = currentTab == tab
                                NavigationBarItem(
                                    selected = selected,
                                    onClick = { currentTab = tab },
                                    icon = {
                                        Icon(
                                            imageVector = tab.icon,
                                            contentDescription = getTabLabel(l10n, tab)
                                        )
                                    },
                                    label = {
                                        Text(
                                            text = getTabLabel(l10n, tab),
                                            style = MaterialTheme.typography.labelSmall
                                        )
                                    },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = LinguaXPrimary,
                                        selectedTextColor = LinguaXPrimary,
                                        indicatorColor = LinguaXPrimary.copy(alpha = 0.12f)
                                    ),
                                    modifier = Modifier.testTag("nav_tab_${tab.name.lowercase()}")
                                )
                            }
                        }
                    }
                ) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        when (currentTab) {
                            MainTab.HOME -> {
                                HomeScreen(
                                    l10n = l10n,
                                    profile = profile,
                                    selectedTargetLanguage = selectedTargetLanguage,
                                    languagesResource = languagesResource,
                                    coursesResource = coursesResource,
                                    challengesResource = challengesResource,
                                    onLanguageSelected = { viewModel.setSelectedTargetLanguage(it) },
                                    onNavigateToCourses = { currentTab = MainTab.COURSES },
                                    onNavigateToVocabulary = { currentTab = MainTab.VOCABULARY },
                                    onNavigateToChallenges = { currentTab = MainTab.CHALLENGES }
                                )
                            }
                            MainTab.COURSES -> {
                                CoursesScreen(
                                    l10n = l10n,
                                    selectedTargetLanguage = selectedTargetLanguage,
                                    languagesResource = languagesResource,
                                    coursesResource = coursesResource,
                                    onLanguageSelected = { viewModel.setSelectedTargetLanguage(it) },
                                    onLessonClicked = { lesson -> viewModel.openLessonPreview(lesson) }
                                )
                            }
                            MainTab.VOCABULARY -> {
                                VocabularyScreen(
                                    l10n = l10n,
                                    vocabularyResource = vocabularyResource
                                )
                            }
                            MainTab.CHALLENGES -> {
                                ChallengesScreen(
                                    l10n = l10n,
                                    challengesResource = challengesResource
                                )
                            }
                            MainTab.ACHIEVEMENTS -> {
                                AchievementsScreen(
                                    l10n = l10n,
                                    achievementsResource = achievementsResource
                                )
                            }
                        }
                    }

                    // Lesson Preview & Exercise Dialog
                    selectedLessonForDialog?.let { lesson ->
                        LessonPreviewDialog(
                            lesson = lesson,
                            l10n = l10n,
                            exercisesResource = activeExercisesResource,
                            onDismiss = { viewModel.dismissLessonPreview() },
                            onCompleteLesson = {
                                viewModel.updateProfile(
                                    newDisplayName = profile.displayName ?: "Learner",
                                    newGoal = profile.dailyGoal
                                )
                                viewModel.dismissLessonPreview()
                            }
                        )
                    }
                }
            }
        }
    }
}

private fun getTabLabel(l10n: L10nStrings, tab: MainTab): String {
    return when (tab) {
        MainTab.HOME -> l10n.homeTab
        MainTab.COURSES -> l10n.coursesTab
        MainTab.VOCABULARY -> l10n.vocabularyTab
        MainTab.CHALLENGES -> l10n.challengesTab
        MainTab.ACHIEVEMENTS -> l10n.achievementsTab
    }
}
