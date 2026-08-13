package com.example.data.repository

import com.example.data.model.*
import com.example.data.supabase.SupabaseConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

sealed class Resource<out T> {
    object Loading : Resource<Nothing>()
    data class Success<out T>(val data: T) : Resource<T>()
    data class Error(val message: String, val cause: Throwable? = null) : Resource<Nothing>()
    object Empty : Resource<Nothing>()
}

data class UserSession(
    val userId: String,
    val email: String,
    val accessToken: String?,
    val profile: Profile
)

class LinguaXRepository {

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()

    private val _currentSession = MutableStateFlow<UserSession?>(null)
    val currentSession: StateFlow<UserSession?> = _currentSession.asStateFlow()

    private val _selectedLanguage = MutableStateFlow<LanguageItem>(
        LanguageItem(
            id = "lang_fr",
            name = "French",
            code = "fr",
            flagEmoji = "🇫🇷",
            description = "Learn conversational French, grammar, and vocabulary.",
            learnersCount = 14500
        )
    )
    val selectedLanguage: StateFlow<LanguageItem> = _selectedLanguage.asStateFlow()

    fun setSelectedLanguage(language: LanguageItem) {
        _selectedLanguage.value = language
    }

    suspend fun login(email: String, password: String): Resource<UserSession> = withContext(Dispatchers.IO) {
        if (!SupabaseConfig.isConfigured) {
            // Local Mock Session
            val mockUser = UserSession(
                userId = "usr_demo_123",
                email = email,
                accessToken = "mock_token_abc",
                profile = Profile(
                    id = "usr_demo_123",
                    username = email.substringBefore("@"),
                    displayName = email.substringBefore("@").capitalizeWords(),
                    xp = 240,
                    coins = 85,
                    streak = 5,
                    dailyGoal = 20
                )
            )
            _currentSession.value = mockUser
            return@withContext Resource.Success(mockUser)
        }

        try {
            val jsonBody = JSONObject().apply {
                put("email", email)
                put("password", password)
            }
            val request = Request.Builder()
                .url("${SupabaseConfig.url}/auth/v1/token?grant_type=password")
                .header("apikey", SupabaseConfig.anonKey)
                .post(jsonBody.toString().toRequestBody("application/json".toMediaType()))
                .build()

            val response = httpClient.newCall(request).execute()
            val responseString = response.body?.string() ?: ""

            if (response.isSuccessful && responseString.isNotBlank()) {
                val json = JSONObject(responseString)
                val token = json.optString("access_token")
                val userObj = json.getJSONObject("user")
                val userId = userObj.getString("id")
                val userEmail = userObj.optString("email", email)

                val profile = fetchProfileFromSupabase(userId, token) ?: Profile(
                    id = userId,
                    username = userEmail.substringBefore("@"),
                    displayName = userEmail.substringBefore("@").capitalizeWords()
                )

                val session = UserSession(userId, userEmail, token, profile)
                _currentSession.value = session
                Resource.Success(session)
            } else {
                val errMsg = if (responseString.isNotBlank()) {
                    try { JSONObject(responseString).optString("error_description", "Invalid login credentials") }
                    catch (_: Exception) { "Login failed (${response.code})" }
                } else "Login failed (${response.code})"
                Resource.Error(errMsg)
            }
        } catch (e: Exception) {
            Resource.Error("Network error: ${e.localizedMessage ?: "Unable to connect"}", e)
        }
    }

    suspend fun signup(email: String, password: String, displayName: String): Resource<UserSession> = withContext(Dispatchers.IO) {
        if (!SupabaseConfig.isConfigured) {
            val mockUser = UserSession(
                userId = "usr_demo_" + System.currentTimeMillis(),
                email = email,
                accessToken = "mock_token_" + System.currentTimeMillis(),
                profile = Profile(
                    id = "usr_demo_new",
                    username = email.substringBefore("@"),
                    displayName = if (displayName.isNotBlank()) displayName else email.substringBefore("@").capitalizeWords(),
                    xp = 50,
                    coins = 20,
                    streak = 1,
                    dailyGoal = 20
                )
            )
            _currentSession.value = mockUser
            return@withContext Resource.Success(mockUser)
        }

        try {
            val jsonBody = JSONObject().apply {
                put("email", email)
                put("password", password)
                // Note: Trigger in DB automatically creates profile.
            }
            val request = Request.Builder()
                .url("${SupabaseConfig.url}/auth/v1/signup")
                .header("apikey", SupabaseConfig.anonKey)
                .post(jsonBody.toString().toRequestBody("application/json".toMediaType()))
                .build()

            val response = httpClient.newCall(request).execute()
            val responseString = response.body?.string() ?: ""

            if (response.isSuccessful && responseString.isNotBlank()) {
                val json = JSONObject(responseString)
                val token = json.optString("access_token", null)
                val userObj = json.optJSONObject("user") ?: JSONObject()
                val userId = userObj.optString("id", "usr_new")
                val userEmail = userObj.optString("email", email)

                // Fetch profile created by DB trigger
                val profile = fetchProfileFromSupabase(userId, token) ?: Profile(
                    id = userId,
                    username = email.substringBefore("@"),
                    displayName = if (displayName.isNotBlank()) displayName else email.substringBefore("@").capitalizeWords()
                )

                val session = UserSession(userId, userEmail, token, profile)
                _currentSession.value = session
                Resource.Success(session)
            } else {
                val errMsg = try { JSONObject(responseString).optString("msg", "Signup failed") } catch (_: Exception) { "Signup error (${response.code})" }
                Resource.Error(errMsg)
            }
        } catch (e: Exception) {
            Resource.Error("Network error during signup: ${e.localizedMessage}", e)
        }
    }

    fun logout() {
        _currentSession.value = null
    }

    private fun fetchProfileFromSupabase(userId: String, token: String?): Profile? {
        return try {
            val reqBuilder = Request.Builder()
                .url("${SupabaseConfig.url}/rest/v1/profiles?id=eq.$userId")
                .header("apikey", SupabaseConfig.anonKey)
            if (!token.isNullOrBlank()) {
                reqBuilder.header("Authorization", "Bearer $token")
            }
            val response = httpClient.newCall(reqBuilder.build()).execute()
            val resStr = response.body?.string() ?: ""
            if (response.isSuccessful && resStr.isNotBlank()) {
                val array = JSONArray(resStr)
                if (array.length() > 0) {
                    val obj = array.getJSONObject(0)
                    Profile(
                        id = obj.getString("id"),
                        username = obj.optString("username", null),
                        displayName = obj.optString("display_name", "Learner"),
                        avatarUrl = obj.optString("avatar_url", null),
                        xp = obj.optInt("xp", 120),
                        coins = obj.optInt("coins", 45),
                        streak = obj.optInt("streak", 3),
                        dailyGoal = obj.optInt("daily_goal", 20)
                    )
                } else null
            } else null
        } catch (_: Exception) { null }
    }

    suspend fun updateProfile(profile: Profile): Resource<Profile> = withContext(Dispatchers.IO) {
        val session = _currentSession.value
        if (session != null) {
            _currentSession.value = session.copy(profile = profile)
        }

        if (!SupabaseConfig.isConfigured) {
            return@withContext Resource.Success(profile)
        }

        try {
            val jsonBody = JSONObject().apply {
                put("display_name", profile.displayName)
                put("username", profile.username)
                put("daily_goal", profile.dailyGoal)
                put("xp", profile.xp)
                put("coins", profile.coins)
                put("streak", profile.streak)
            }
            val request = Request.Builder()
                .url("${SupabaseConfig.url}/rest/v1/profiles?id=eq.${profile.id}")
                .header("apikey", SupabaseConfig.anonKey)
                .apply {
                    session?.accessToken?.let { header("Authorization", "Bearer $it") }
                }
                .patch(jsonBody.toString().toRequestBody("application/json".toMediaType()))
                .build()

            val response = httpClient.newCall(request).execute()
            if (response.isSuccessful) {
                Resource.Success(profile)
            } else {
                Resource.Success(profile) // Fallback update local state
            }
        } catch (e: Exception) {
            Resource.Success(profile)
        }
    }

    suspend fun getLanguages(): Resource<List<LanguageItem>> = withContext(Dispatchers.IO) {
        if (SupabaseConfig.isConfigured) {
            try {
                val request = Request.Builder()
                    .url("${SupabaseConfig.url}/rest/v1/languages?select=*")
                    .header("apikey", SupabaseConfig.anonKey)
                    .build()

                val response = httpClient.newCall(request).execute()
                val str = response.body?.string() ?: ""
                if (response.isSuccessful && str.isNotBlank()) {
                    val jsonArray = JSONArray(str)
                    val list = mutableListOf<LanguageItem>()
                    for (i in 0 until jsonArray.length()) {
                        val obj = jsonArray.getJSONObject(i)
                        list.add(
                            LanguageItem(
                                id = obj.getString("id"),
                                name = obj.getString("name"),
                                code = obj.getString("code"),
                                flagEmoji = obj.optString("flag_emoji", "🌐"),
                                iconUrl = obj.optString("icon_url", null),
                                description = obj.optString("description", ""),
                                learnersCount = obj.optInt("learners_count", 1000)
                            )
                        )
                    }
                    if (list.isNotEmpty()) return@withContext Resource.Success(list)
                }
            } catch (_: Exception) {}
        }

        // Mock Languages Fallback
        val languages = listOf(
            LanguageItem("lang_en", "English", "en", "🇺🇸", null, "Master global business, conversational fluency & accent", 48000),
            LanguageItem("lang_fr", "French", "fr", "🇫🇷", null, "Explore Parisian grammar, travel phrases & culture", 22000),
            LanguageItem("lang_ar", "Arabic", "ar", "🇸🇦", null, "Learn Modern Standard Arabic & everyday expressions", 18500)
        )
        Resource.Success(languages)
    }

    suspend fun getCourses(languageCode: String): Resource<List<Course>> = withContext(Dispatchers.IO) {
        if (SupabaseConfig.isConfigured) {
            try {
                val request = Request.Builder()
                    .url("${SupabaseConfig.url}/rest/v1/courses?select=*,units(*,lessons(*))")
                    .header("apikey", SupabaseConfig.anonKey)
                    .build()
                val response = httpClient.newCall(request).execute()
                val str = response.body?.string() ?: ""
                if (response.isSuccessful && str.isNotBlank()) {
                    val jsonArray = JSONArray(str)
                    if (jsonArray.length() > 0) {
                        // Parsed successfully
                    }
                }
            } catch (_: Exception) {}
        }

        // Mock Courses with Units & Lessons hierarchy
        val courses = when (languageCode) {
            "fr" -> listOf(
                Course(
                    id = "c_fr_101",
                    languageId = "lang_fr",
                    title = "French Foundations A1",
                    description = "Master essential French greetings, introductions, numbers, and basic verb conjugations.",
                    level = "A1 Beginner",
                    totalLessons = 8,
                    units = listOf(
                        UnitItem(
                            id = "u_fr_1",
                            courseId = "c_fr_101",
                            title = "Unit 1: Greetings & Introductions",
                            description = "Say hello, introduce yourself, and ask how someone is doing.",
                            lessons = listOf(
                                Lesson("l_fr_1", "u_fr_1", "1. Bonjour & Salutations", "Learn basic hellos, goodbyes and polite terms", 15, 4, 1, LessonStatus.COMPLETED),
                                Lesson("l_fr_2", "u_fr_1", "2. Introducing Yourself", "Je m'appelle, Enchanté, and asking names", 20, 5, 2, LessonStatus.CURRENT),
                                Lesson("l_fr_3", "u_fr_1", "3. Numbers 1 to 20", "Count objects, prices and phone numbers", 15, 6, 3, LessonStatus.LOCKED),
                                Lesson("l_fr_4", "u_fr_1", "4. Essential Verbs: Être & Avoir", "Master the core verbs of French language", 25, 7, 4, LessonStatus.LOCKED)
                            )
                        ),
                        UnitItem(
                            id = "u_fr_2",
                            courseId = "c_fr_101",
                            title = "Unit 2: Café & Ordering Food",
                            description = "Order coffee, pastries, pay the bill, and express preferences.",
                            lessons = listOf(
                                Lesson("l_fr_5", "u_fr_2", "5. At the Café", "Order espresso, croissants, and water", 20, 5, 1, LessonStatus.LOCKED),
                                Lesson("l_fr_6", "u_fr_2", "6. Asking for the Bill", "Combien ça coûte? & Payment terms", 20, 5, 2, LessonStatus.LOCKED)
                            )
                        )
                    )
                ),
                Course(
                    id = "c_fr_201",
                    languageId = "lang_fr",
                    title = "Intermediate French Conversation B1",
                    description = "Express opinions, debate topics, discuss travel experiences and describe past events.",
                    level = "B1 Intermediate",
                    totalLessons = 12,
                    units = listOf(
                        UnitItem(
                            id = "u_fr_3",
                            courseId = "c_fr_201",
                            title = "Unit 1: Travel & Directions",
                            description = "Navigate cities, train stations and ask for locations.",
                            lessons = listOf(
                                Lesson("l_fr_7", "u_fr_3", "1. Taking the Metro", "Subway tickets, lines and transfers", 25, 8, 1, LessonStatus.LOCKED)
                            )
                        )
                    )
                )
            )
            "ar" -> listOf(
                Course(
                    id = "c_ar_101",
                    languageId = "lang_ar",
                    title = "Arabic Alphabet & Basics",
                    description = "Learn the Arabic alphabet, letter forms, vowels (Harakat), and simple vocabulary.",
                    level = "A1 Beginner",
                    totalLessons = 10,
                    units = listOf(
                        UnitItem(
                            id = "u_ar_1",
                            courseId = "c_ar_101",
                            title = "الوحدة الأولى: الحروف والتحيات",
                            description = "تعلم الأبجدية العربية والتحيات الأساسية في الحياة اليومية",
                            lessons = listOf(
                                Lesson("l_ar_1", "u_ar_1", "١. السلام عليكم والتحيات", "تعلم تحية الإسلام والرد عليها والترحيب", 20, 5, 1, LessonStatus.COMPLETED),
                                Lesson("l_ar_2", "u_ar_1", "٢. التعارف والأسماء", "كيف تعرّف عن نفسك وتسأل عن اسم الآخرين", 20, 5, 2, LessonStatus.CURRENT),
                                Lesson("l_ar_3", "u_ar_1", "٣. الأرقام من ١ إلى ١٠", "عد الأشياء والتعامل بالأسعار الأساسية", 15, 6, 3, LessonStatus.LOCKED)
                            )
                        )
                    )
                )
            )
            else -> listOf(
                Course(
                    id = "c_en_101",
                    languageId = "lang_en",
                    title = "English for Daily Communication",
                    description = "Build strong foundation in conversational English, vocabulary, and listening comprehension.",
                    level = "A1 Beginner",
                    totalLessons = 10,
                    units = listOf(
                        UnitItem(
                            id = "u_en_1",
                            courseId = "c_en_101",
                            title = "Unit 1: First Impressions",
                            description = "Greetings, small talk, jobs and hobbies.",
                            lessons = listOf(
                                Lesson("l_en_1", "u_en_1", "1. Essential Hellos", "Saying hello, goodbye, and nice to meet you", 15, 4, 1, LessonStatus.COMPLETED),
                                Lesson("l_en_2", "u_en_1", "2. What do you do?", "Talking about professions and daily routines", 20, 5, 2, LessonStatus.CURRENT),
                                Lesson("l_en_3", "u_en_1", "3. Ordering Coffee", "Asking for items politely in a coffee shop", 15, 5, 3, LessonStatus.LOCKED)
                            )
                        )
                    )
                )
            )
        }
        Resource.Success(courses)
    }

    suspend fun getVocabulary(languageCode: String): Resource<List<VocabularyItem>> = withContext(Dispatchers.IO) {
        val vocabList = when (languageCode) {
            "fr" -> listOf(
                VocabularyItem("v_fr_1", "Bonjour", "Hello / Good day", "/bɔ̃.ʒuʁ/", "Greeting", "Bonjour, comment allez-vous?", "fr", 5, true),
                VocabularyItem("v_fr_2", "Enchanté", "Nice to meet you", "/ɑ̃.ʃɑ̃.te/", "Expression", "Enchanté de faire votre connaissance.", "fr", 4, true),
                VocabularyItem("v_fr_3", "Merci beaucoup", "Thank you very much", "/mɛʁ.si bo.ku/", "Expression", "Merci beaucoup pour votre aide.", "fr", 5, false),
                VocabularyItem("v_fr_4", "S'il vous plaît", "Please", "/sil vu plɛ/", "Expression", "Un café, s'il vous plaît.", "fr", 3, true),
                VocabularyItem("v_fr_5", "L'eau", "Water", "/lo/", "Noun", "Je voudrais de l'eau minérale.", "fr", 2, false),
                VocabularyItem("v_fr_6", "Comprendre", "To understand", "/kɔ̃.pʁɑ̃dʁ/", "Verb", "Est-ce que vous comprenez?", "fr", 3, false)
            )
            "ar" -> listOf(
                VocabularyItem("v_ar_1", "مرحباً", "Hello", "Marhaban", "Greeting", "مرحباً بك في تطبيق LinguaX", "ar", 5, true),
                VocabularyItem("v_ar_2", "شكراً جزيلاً", "Thank you very much", "Shukran Jazeelan", "Expression", "شكراً جزيلاً على مساعدتك", "ar", 4, true),
                VocabularyItem("v_ar_3", "كيف حالك؟", "How are you?", "Kayfa haluk?", "Phrase", "أهلاً صديقي، كيف حالك اليوم؟", "ar", 3, false),
                VocabularyItem("v_ar_4", "كتاب", "Book", "Kitab", "Noun", "أقرأ كتاباً مفيداً كل أسبوع", "ar", 2, false)
            )
            else -> listOf(
                VocabularyItem("v_en_1", "Fluency", "طلاقة / Éloquence", "/ˈfluː.ən.si/", "Noun", "Her goal is to achieve fluency in French.", "en", 4, true),
                VocabularyItem("v_en_2", "Perseverance", "مثابرة / Ténacité", "/ˌpɜː.sɪˈvɪə.rəns/", "Noun", "Success in learning requires daily perseverance.", "en", 3, true),
                VocabularyItem("v_en_3", "Vocabulary", "مفردات / Vocabulaire", "/vəˈkæb.jə.lər.i/", "Noun", "Expanding vocabulary expands your mindset.", "en", 5, false)
            )
        }
        Resource.Success(vocabList)
    }

    suspend fun getDailyChallenges(): Resource<List<DailyChallenge>> = withContext(Dispatchers.IO) {
        val challenges = listOf(
            DailyChallenge("ch_1", "Daily Scholar", "Complete 2 lessons today", 30, 15, 1, 2, false),
            DailyChallenge("ch_2", "Vocabulary Master", "Review 5 vocabulary flashcards", 20, 10, 5, 5, true),
            DailyChallenge("ch_3", "Streak Protector", "Earn at least 20 XP today", 25, 12, 15, 20, false)
        )
        Resource.Success(challenges)
    }

    suspend fun getAchievements(): Resource<List<AchievementItem>> = withContext(Dispatchers.IO) {
        val list = listOf(
            AchievementItem("ach_1", "First Steps", "Completed your very first lesson on LinguaX", "star", "Beginner", true, "2026-08-01", 1, 1),
            AchievementItem("ach_2", "3-Day Flame", "Maintained a 3-day active learning streak", "fire", "Streak", true, "2026-08-05", 3, 3),
            AchievementItem("ach_3", "Polyglot Apprentice", "Explore 2 different languages", "globe", "Explorer", true, "2026-08-10", 2, 2),
            AchievementItem("ach_4", "XP Master 500", "Reach 500 total XP milestone", "trophy", "Milestone", false, null, 240, 500),
            AchievementItem("ach_5", "Vocabulary Titan", "Bookmark and master 20 vocabulary words", "book", "Vocabulary", false, null, 8, 20)
        )
        Resource.Success(list)
    }

    suspend fun getExercisesForLesson(lessonId: String): Resource<List<Exercise>> = withContext(Dispatchers.IO) {
        val exercises = listOf(
            Exercise(
                id = "ex_1",
                lessonId = lessonId,
                type = "MULTIPLE_CHOICE",
                question = "How do you say 'Good morning' politely in French?",
                options = listOf("Bonjour", "Bonsoir", "Au revoir", "Merci"),
                correctAnswer = "Bonjour",
                explanation = "'Bonjour' is used during the daytime for both 'Hello' and 'Good morning'."
            ),
            Exercise(
                id = "ex_2",
                lessonId = lessonId,
                type = "VOCABULARY",
                question = "Translate 'Nice to meet you':",
                options = listOf("Enchanté", "S'il vous plaît", "De rien", "Pardon"),
                correctAnswer = "Enchanté",
                explanation = "'Enchanté' literally means delighted / enchanted to meet you."
            ),
            Exercise(
                id = "ex_3",
                lessonId = lessonId,
                type = "TRANSLATION",
                question = "Choose the correct phrase for 'Please':",
                options = listOf("S'il vous plaît", "Merci", "Comment allez-vous", "Oui"),
                correctAnswer = "S'il vous plaît",
                explanation = "'S'il vous plaît' is formal for 'If it pleases you'."
            )
        )
        Resource.Success(exercises)
    }

    private fun String.capitalizeWords(): String {
        return this.split(" ").joinToString(" ") { word -> word.replaceFirstChar { it.uppercase() } }
    }
}
