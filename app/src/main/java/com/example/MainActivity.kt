package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.i18n.AppLanguage
import com.example.data.i18n.L10nStrings
import com.example.ui.screens.achievements.AchievementsScreen
import com.example.ui.screens.auth.LandingScreen
import com.example.ui.screens.auth.LoginScreen
import com.example.ui.screens.auth.SignupScreen
import com.example.ui.screens.challenges.ChallengesScreen
import com.example.ui.screens.courses.CoursesScreen
import com.example.ui.screens.courses.LessonPreviewDialog
import com.example.ui.screens.home.HomeScreen
import com.example.ui.screens.profile.ProfileScreen
import com.example.ui.screens.vocabulary.VocabularyScreen
import com.example.ui.theme.*
import com.example.ui.viewmodel.AuthState
import com.example.ui.viewmodel.MainViewModel

enum class AuthSubScreen {
    LANDING, LOGIN, SIGNUP
}

enum class MainTab(val icon: ImageVector) {
    HOME(Icons.Default.Home),
    COURSES(Icons.Default.School),
    VOCABULARY(Icons.AutoMirrored.Filled.MenuBook),
    CHALLENGES(Icons.Default.EmojiEvents),
    PROFILE(Icons.Default.Person)
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
    val appLanguage by viewModel.appLanguage.collectAsStateWithLifecycle()
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

    // Dynamic RTL / LTR layout provider
    CompositionLocalProvider(LocalLayoutDirection provides appLanguage.layoutDirection) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = LinguaXBackground
        ) {
            when (val state = authState) {
                is AuthState.Unauthenticated, is AuthState.Loading, is AuthState.Error -> {
                    when (authSubScreen) {
                        AuthSubScreen.LANDING -> {
                            LandingScreen(
                                l10n = l10n,
                                currentAppLanguage = appLanguage,
                                onLanguageChange = { viewModel.setAppLanguage(it) },
                                onNavigateToLogin = { authSubScreen = AuthSubScreen.LOGIN },
                                onNavigateToSignup = { authSubScreen = AuthSubScreen.SIGNUP },
                                onContinueAsGuest = { viewModel.continueAsGuest() }
                            )
                        }
                        AuthSubScreen.LOGIN -> {
                            LoginScreen(
                                l10n = l10n,
                                authState = state,
                                onLogin = { email, pass -> viewModel.login(email, pass) },
                                onNavigateToSignup = { authSubScreen = AuthSubScreen.SIGNUP }
                            )
                        }
                        AuthSubScreen.SIGNUP -> {
                            SignupScreen(
                                l10n = l10n,
                                authState = state,
                                onSignup = { email, pass, name -> viewModel.signup(email, pass, name) },
                                onNavigateToLogin = { authSubScreen = AuthSubScreen.LOGIN }
                            )
                        }
                    }
                }
                is AuthState.Authenticated -> {
                    val profile = state.session.profile

                    Scaffold(
                        bottomBar = {
                            NavigationBar(
                                containerColor = LinguaXSurface,
                                tonalElevation = 8.dp,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                            ) {
                                MainTab.values().forEach { tab ->
                                    val isSelected = currentTab == tab
                                    val label = when (tab) {
                                        MainTab.HOME -> l10n.homeTab
                                        MainTab.COURSES -> l10n.coursesTab
                                        MainTab.VOCABULARY -> l10n.vocabularyTab
                                        MainTab.CHALLENGES -> l10n.challengesTab
                                        MainTab.PROFILE -> l10n.profileTab
                                    }

                                    NavigationBarItem(
                                        selected = isSelected,
                                        onClick = { currentTab = tab },
                                        icon = {
                                            Icon(
                                                imageVector = tab.icon,
                                                contentDescription = label
                                            )
                                        },
                                        label = {
                                            Text(
                                                text = label,
                                                style = MaterialTheme.typography.labelSmall
                                            )
                                        },
                                        colors = NavigationBarItemDefaults.colors(
                                            selectedIconColor = LinguaXAccent,
                                            selectedTextColor = LinguaXAccent,
                                            unselectedIconColor = LinguaXTextTertiary,
                                            unselectedTextColor = LinguaXTextTertiary,
                                            indicatorColor = LinguaXPrimaryContainer
                                        ),
                                        modifier = Modifier.testTag("nav_tab_${tab.name.lowercase()}")
                                    )
                                }
                            }
                        }
                    ) { innerPadding ->
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding)
                                .background(LinguaXBackground)
                        ) {
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
                                        onNavigateToChallenges = { currentTab = MainTab.CHALLENGES },
                                        onOpenSettings = { currentTab = MainTab.PROFILE }
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
                                        selectedTargetLanguage = selectedTargetLanguage,
                                        vocabularyResource = vocabularyResource,
                                        onToggleBookmark = { viewModel.toggleVocabularyBookmark(it) }
                                    )
                                }
                                MainTab.CHALLENGES -> {
                                    ChallengesScreen(
                                        l10n = l10n,
                                        challengesResource = challengesResource,
                                        onClaimReward = { challenge ->
                                            viewModel.updateProfile(
                                                newDisplayName = profile.displayName ?: "Learner",
                                                newGoal = profile.dailyGoal
                                            )
                                        }
                                    )
                                }
                                MainTab.PROFILE -> {
                                    ProfileScreen(
                                        l10n = l10n,
                                        profile = profile,
                                        currentAppLanguage = appLanguage,
                                        selectedTargetLanguage = selectedTargetLanguage,
                                        onAppLanguageChange = { viewModel.setAppLanguage(it) },
                                        onUpdateProfile = { name, goal -> viewModel.updateProfile(name, goal) },
                                        onLogout = { viewModel.logout() }
                                    )
                                }
                            }
                        }

                        // Dynamic 3D Interactive Lesson Dialog
                        selectedLessonForDialog?.let { lesson ->
                            LessonPreviewDialog(
                                lesson = lesson,
                                l10n = l10n,
                                exercisesResource = activeExercisesResource,
                                onDismiss = { viewModel.dismissLessonPreview() },
                                onCompleteLesson = {
                                    viewModel.completeLesson(lesson)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
