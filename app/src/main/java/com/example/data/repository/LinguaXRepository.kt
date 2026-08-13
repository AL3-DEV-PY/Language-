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

private fun JSONObject.optNullableString(key: String): String? {
    if (!has(key) || isNull(key)) return null
    val value = optString(key)
    return if (value == "null" || value.isEmpty()) null else value
}

class LinguaXRepository {

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()

    private val _currentSession = MutableStateFlow<UserSession?>(null)
    val currentSession: StateFlow<UserSession?> = _currentSession.asStateFlow()

    // Default target language: Arabic or English
    private val _selectedLanguage = MutableStateFlow<LanguageItem>(
        LanguageItem(
            id = "lang_ar",
            name = "Arabic",
            nativeName = "العربية",
            code = "ar",
            flagEmoji = "🇸🇦",
            description = "Master Modern Standard Arabic, grammar & everyday conversations.",
            learnersCount = 35000,
            sortOrder = 1
        )
    )
    val selectedLanguage: StateFlow<LanguageItem> = _selectedLanguage.asStateFlow()

    // Local in-memory caches
    private val completedLessonIds = mutableSetOf("l_ar_1", "l_en_1", "l_fr_1", "l_es_1")
    private val bookmarkedVocabIds = mutableSetOf("v_ar_1", "v_ar_2", "v_en_1", "v_fr_1")

    fun setSelectedLanguage(language: LanguageItem) {
        _selectedLanguage.value = language
    }

    suspend fun login(email: String, password: String): Resource<UserSession> = withContext(Dispatchers.IO) {
        if (!SupabaseConfig.isConfigured) {
            val mockUser = UserSession(
                userId = "usr_demo_123",
                email = email,
                accessToken = "mock_token_abc",
                profile = Profile(
                    id = "usr_demo_123",
                    username = email.substringBefore("@"),
                    displayName = email.substringBefore("@").capitalizeWords(),
                    xp = 420,
                    coins = 150,
                    streak = 7,
                    dailyGoal = 30
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
                    displayName = userEmail.substringBefore("@").capitalizeWords(),
                    xp = 420,
                    coins = 150,
                    streak = 7,
                    dailyGoal = 30
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
                    coins = 50,
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
                val token = json.optNullableString("access_token")
                val userObj = json.optJSONObject("user") ?: JSONObject()
                val userId = userObj.optString("id", "usr_new")
                val userEmail = userObj.optString("email", email)

                val profile = fetchProfileFromSupabase(userId, token) ?: Profile(
                    id = userId,
                    username = email.substringBefore("@"),
                    displayName = if (displayName.isNotBlank()) displayName else email.substringBefore("@").capitalizeWords(),
                    xp = 50,
                    coins = 50,
                    streak = 1,
                    dailyGoal = 20
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
                        username = obj.optNullableString("username"),
                        displayName = obj.optString("display_name", "Learner"),
                        avatarUrl = obj.optNullableString("avatar_url"),
                        xp = obj.optInt("xp", 420),
                        coins = obj.optInt("coins", 150),
                        streak = obj.optInt("streak", 7),
                        dailyGoal = obj.optInt("daily_goal", 30)
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
                Resource.Success(profile)
            }
        } catch (e: Exception) {
            Resource.Success(profile)
        }
    }

    suspend fun recordLessonCompleted(lessonId: String, xpEarned: Int, coinsEarned: Int) {
        completedLessonIds.add(lessonId)
        val session = _currentSession.value
        if (session != null) {
            val updated = session.profile.copy(
                xp = session.profile.xp + xpEarned,
                coins = session.profile.coins + coinsEarned,
                streak = if (session.profile.streak == 0) 1 else session.profile.streak
            )
            updateProfile(updated)
        }
    }

    fun toggleVocabularyBookmark(vocabId: String): Boolean {
        return if (bookmarkedVocabIds.contains(vocabId)) {
            bookmarkedVocabIds.remove(vocabId)
            false
        } else {
            bookmarkedVocabIds.add(vocabId)
            true
        }
    }

    suspend fun getLanguages(): Resource<List<LanguageItem>> = withContext(Dispatchers.IO) {
        if (SupabaseConfig.isConfigured) {
            try {
                val request = Request.Builder()
                    .url("${SupabaseConfig.url}/rest/v1/languages?select=*&is_active=eq.true&order=sort_order.asc")
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
                                nativeName = obj.optNullableString("native_name"),
                                code = obj.getString("code"),
                                flagEmoji = obj.optString("flag_emoji", obj.optString("flag", "🌐")),
                                iconUrl = obj.optNullableString("icon_url"),
                                description = obj.optString("description", ""),
                                learnersCount = obj.optInt("learners_count", 1000),
                                isActive = obj.optBoolean("is_active", true),
                                sortOrder = obj.optInt("sort_order", i + 1)
                            )
                        )
                    }
                    if (list.isNotEmpty()) return@withContext Resource.Success(list)
                }
            } catch (_: Exception) {}
        }

        // Full 9 Supported Languages Fallback & Local Cache
        val languages = listOf(
            LanguageItem("lang_ar", "Arabic", "العربية", "ar", "🇸🇦", null, "Modern Standard Arabic, grammar & conversational fluency", 35000, true, 1),
            LanguageItem("lang_en", "English", "English", "en", "🇺🇸", null, "Global business, conversational fluency & native pronunciation", 64000, true, 2),
            LanguageItem("lang_fr", "French", "Français", "fr", "🇫🇷", null, "Parisian grammar, travel dialogue, vocabulary & culture", 28000, true, 3),
            LanguageItem("lang_es", "Spanish", "Español", "es", "🇪🇸", null, "Latin & European Spanish, daily conversations & conjugations", 41000, true, 4),
            LanguageItem("lang_de", "German", "Deutsch", "de", "🇩🇪", null, "A1 to B2 German syntax, cases, work & everyday communication", 22000, true, 5),
            LanguageItem("lang_it", "Italian", "Italiano", "it", "🇮🇹", null, "Melodic Italian expressions, cuisine, travel & grammar", 19000, true, 6),
            LanguageItem("lang_tr", "Turkish", "Türkçe", "tr", "🇹🇷", null, "Turkish vowel harmony, daily phrases & Istanbul expressions", 16000, true, 7),
            LanguageItem("lang_ja", "Japanese", "日本語", "ja", "🇯🇵", null, "Hiragana, Katakana, Kanji, polite keigo & daily anime dialogue", 38000, true, 8),
            LanguageItem("lang_ko", "Korean", "한국어", "ko", "🇰🇷", null, "Hangul mastery, honorifics, K-culture & conversational fluency", 32000, true, 9)
        )
        Resource.Success(languages)
    }

    suspend fun getCourses(languageCode: String): Resource<List<Course>> = withContext(Dispatchers.IO) {
        if (SupabaseConfig.isConfigured) {
            try {
                val request = Request.Builder()
                    .url("${SupabaseConfig.url}/rest/v1/courses?select=*,units(*,lessons(*))&is_active=eq.true&order=sort_order.asc")
                    .header("apikey", SupabaseConfig.anonKey)
                    .build()
                val response = httpClient.newCall(request).execute()
                val str = response.body?.string() ?: ""
                if (response.isSuccessful && str.isNotBlank()) {
                    val jsonArray = JSONArray(str)
                    val list = mutableListOf<Course>()
                    for (i in 0 until jsonArray.length()) {
                        val cObj = jsonArray.getJSONObject(i)
                        val langId = cObj.optString("language_id", "")
                        // Match with code or language_id
                        if (langId.contains(languageCode, ignoreCase = true) || langId.equals("lang_$languageCode", ignoreCase = true)) {
                            val unitsJson = cObj.optJSONArray("units") ?: JSONArray()
                            val unitsList = mutableListOf<UnitItem>()
                            for (u in 0 until unitsJson.length()) {
                                val uObj = unitsJson.getJSONObject(u)
                                val lessonsJson = uObj.optJSONArray("lessons") ?: JSONArray()
                                val lessonsList = mutableListOf<Lesson>()
                                for (l in 0 until lessonsJson.length()) {
                                    val lObj = lessonsJson.getJSONObject(l)
                                    val lId = lObj.getString("id")
                                    val isComp = completedLessonIds.contains(lId)
                                    lessonsList.add(
                                        Lesson(
                                            id = lId,
                                            unitId = uObj.getString("id"),
                                            title = lObj.getString("title"),
                                            description = lObj.optString("description", ""),
                                            xpReward = lObj.optInt("xp_reward", 20),
                                            durationMins = lObj.optInt("duration_mins", lObj.optInt("duration", 5)),
                                            orderIndex = lObj.optInt("order_index", lObj.optInt("sort_order", l + 1)),
                                            isFree = lObj.optBoolean("is_free", true),
                                            isActive = lObj.optBoolean("is_active", true),
                                            status = if (isComp) LessonStatus.COMPLETED else if (l == 0) LessonStatus.CURRENT else LessonStatus.LOCKED,
                                            exercisesCount = 5
                                        )
                                    )
                                }
                                unitsList.add(
                                    UnitItem(
                                        id = uObj.getString("id"),
                                        courseId = cObj.getString("id"),
                                        title = uObj.getString("title"),
                                        description = uObj.optString("description", ""),
                                        orderIndex = uObj.optInt("order_index", uObj.optInt("sort_order", u + 1)),
                                        lessons = lessonsList
                                    )
                                )
                            }
                            list.add(
                                Course(
                                    id = cObj.getString("id"),
                                    languageId = langId,
                                    title = cObj.getString("title"),
                                    description = cObj.optString("description", ""),
                                    level = cObj.optString("level", "A1 Beginner"),
                                    imageUrl = cObj.optNullableString("image_url"),
                                    totalLessons = cObj.optInt("total_lessons", 10),
                                    orderIndex = cObj.optInt("order_index", cObj.optInt("sort_order", i + 1)),
                                    units = unitsList
                                )
                            )
                        }
                    }
                    if (list.isNotEmpty()) return@withContext Resource.Success(list)
                }
            } catch (_: Exception) {}
        }

        // Rich Multi-Language Courses Engine (All 9 Languages)
        val courses = generateCoursesForLanguage(languageCode)
        Resource.Success(courses)
    }

    private fun generateCoursesForLanguage(code: String): List<Course> {
        return when (code.lowercase()) {
            "ar" -> listOf(
                Course(
                    id = "c_ar_101",
                    languageId = "lang_ar",
                    title = "Arabic Foundations A1",
                    description = "Master Arabic letters, vowels (Harakat), essential greetings, introductions and numbers.",
                    level = "A1 Beginner",
                    totalLessons = 8,
                    units = listOf(
                        UnitItem(
                            id = "u_ar_1",
                            courseId = "c_ar_101",
                            title = "الوحدة الأولى: التحيات والتعارف",
                            description = "تعلم تحية الإسلام والترحيب والسؤال عن الحال والاسم",
                            lessons = listOf(
                                Lesson("l_ar_1", "u_ar_1", "١. السلام عليكم والتحيات", "تعلم تحية الإسلام والترحيب اليومي", 20, 4, 1, status = if (completedLessonIds.contains("l_ar_1")) LessonStatus.COMPLETED else LessonStatus.CURRENT),
                                Lesson("l_ar_2", "u_ar_1", "٢. التعارف والأسماء", "كيف تعرّف عن نفسك وتسأل عن اسم الآخرين", 25, 5, 2, status = if (completedLessonIds.contains("l_ar_2")) LessonStatus.COMPLETED else LessonStatus.CURRENT),
                                Lesson("l_ar_3", "u_ar_1", "٣. الأرقام والحساب الأساسي", "الأرقام من ١ إلى ٢٠ واستخدامها في الحياة", 20, 6, 3, status = LessonStatus.LOCKED),
                                Lesson("l_ar_4", "u_ar_1", "٤. الضمائر المنفصلة", "أنا، أنتَ، أنتِ، هو، هي واستخداماتها", 30, 7, 4, status = LessonStatus.LOCKED)
                            )
                        ),
                        UnitItem(
                            id = "u_ar_2",
                            courseId = "c_ar_101",
                            title = "الوحدة الثانية: في السوق والمطعم",
                            description = "طلب الطعام والشراب والسؤال عن الأسعار والتسوق",
                            lessons = listOf(
                                Lesson("l_ar_5", "u_ar_2", "٥. طلب الطعام والقهوة", "مفردات المأكولات والمشروبات في المطعم", 25, 5, 1, status = LessonStatus.LOCKED),
                                Lesson("l_ar_6", "u_ar_2", "٦. السؤال عن السعر والدفع", "كم السعر؟ والدفع بالنقود أو البطاقة", 25, 6, 2, status = LessonStatus.LOCKED)
                            )
                        )
                    )
                ),
                Course(
                    id = "c_ar_201",
                    languageId = "lang_ar",
                    title = "Intermediate Arabic Fluency B1",
                    description = "Deepen comprehension with complex sentences, Modern Standard Arabic media and idioms.",
                    level = "B1 Intermediate",
                    totalLessons = 10,
                    units = listOf(
                        UnitItem(
                            id = "u_ar_3",
                            courseId = "c_ar_201",
                            title = "الوحدة الثالثة: السفر والاتجاهات",
                            description = "التنقل في المطار والفنادق وسؤال المارة عن المواقع",
                            lessons = listOf(
                                Lesson("l_ar_7", "u_ar_3", "٧. في المطار والجوازات", "إجراءات السفر وحجز الفنادق", 35, 8, 1, status = LessonStatus.LOCKED)
                            )
                        )
                    )
                )
            )
            "fr" -> listOf(
                Course(
                    id = "c_fr_101",
                    languageId = "lang_fr",
                    title = "French Foundations A1",
                    description = "Master essential French greetings, introductions, numbers, and core verb conjugations.",
                    level = "A1 Beginner",
                    totalLessons = 8,
                    units = listOf(
                        UnitItem(
                            id = "u_fr_1",
                            courseId = "c_fr_101",
                            title = "Unit 1: Greetings & Introductions",
                            description = "Say hello, introduce yourself, and polite Parisian expressions.",
                            lessons = listOf(
                                Lesson("l_fr_1", "u_fr_1", "1. Bonjour & Salutations", "Basic hellos, goodbyes and courtesy phrases", 20, 4, 1, status = if (completedLessonIds.contains("l_fr_1")) LessonStatus.COMPLETED else LessonStatus.CURRENT),
                                Lesson("l_fr_2", "u_fr_1", "2. Introducing Yourself", "Je m'appelle, Enchanté, and nationalities", 25, 5, 2, status = if (completedLessonIds.contains("l_fr_2")) LessonStatus.COMPLETED else LessonStatus.CURRENT),
                                Lesson("l_fr_3", "u_fr_1", "3. Numbers 1 to 30", "Count objects, prices and phone numbers", 20, 5, 3, status = LessonStatus.LOCKED),
                                Lesson("l_fr_4", "u_fr_1", "4. Essential Verbs: Être & Avoir", "Master the cornerstone verbs of French", 30, 7, 4, status = LessonStatus.LOCKED)
                            )
                        ),
                        UnitItem(
                            id = "u_fr_2",
                            courseId = "c_fr_101",
                            title = "Unit 2: Parisian Café & Dining",
                            description = "Order coffee, pastries, pay the bill, and express culinary preferences.",
                            lessons = listOf(
                                Lesson("l_fr_5", "u_fr_2", "5. At the Café", "Order espresso, croissants, and sparkling water", 25, 5, 1, status = LessonStatus.LOCKED),
                                Lesson("l_fr_6", "u_fr_2", "6. Asking for the Bill", "L'addition s'il vous plaît & payments", 25, 5, 2, status = LessonStatus.LOCKED)
                            )
                        )
                    )
                ),
                Course(
                    id = "c_fr_201",
                    languageId = "lang_fr",
                    title = "Intermediate French B1",
                    description = "Express nuanced opinions, past tense (Passé Composé & Imparfait), and professional dialogue.",
                    level = "B1 Intermediate",
                    totalLessons = 12,
                    units = listOf(
                        UnitItem(
                            id = "u_fr_3",
                            courseId = "c_fr_201",
                            title = "Unit 3: Travel & Transit",
                            description = "Navigate train stations, airports, and city directions.",
                            lessons = listOf(
                                Lesson("l_fr_7", "u_fr_3", "7. Taking the TGV Train", "Tickets, platforms, seat reservations", 30, 8, 1, status = LessonStatus.LOCKED)
                            )
                        )
                    )
                )
            )
            "es" -> listOf(
                Course(
                    id = "c_es_101",
                    languageId = "lang_es",
                    title = "Spanish Essentials A1",
                    description = "Build foundational Spanish fluency with greetings, ser vs estar, and everyday conversations.",
                    level = "A1 Beginner",
                    totalLessons = 8,
                    units = listOf(
                        UnitItem(
                            id = "u_es_1",
                            courseId = "c_es_101",
                            title = "Unidad 1: ¡Hola y Saludos!",
                            description = "Learn greetings, pleasantries and self-introductions in Spanish.",
                            lessons = listOf(
                                Lesson("l_es_1", "u_es_1", "1. ¡Hola! ¿Cómo estás?", "Essential greetings and basic courtesy phrases", 20, 4, 1, status = if (completedLessonIds.contains("l_es_1")) LessonStatus.COMPLETED else LessonStatus.CURRENT),
                                Lesson("l_es_2", "u_es_1", "2. Me llamo...", "State your name, origin, and profession", 25, 5, 2, status = LessonStatus.CURRENT),
                                Lesson("l_es_3", "u_es_1", "3. Ser vs Estar", "Understand the two verbs for 'to be'", 30, 6, 3, status = LessonStatus.LOCKED)
                            )
                        ),
                        UnitItem(
                            id = "u_es_2",
                            courseId = "c_es_101",
                            title = "Unidad 2: En el Restaurante",
                            description = "Ordering tapas, asking for recommendations and paying.",
                            lessons = listOf(
                                Lesson("l_es_4", "u_es_2", "4. Pedir Comida", "Order dishes, drinks and special requests", 25, 5, 1, status = LessonStatus.LOCKED)
                            )
                        )
                    )
                )
            )
            "de" -> listOf(
                Course(
                    id = "c_de_101",
                    languageId = "lang_de",
                    title = "German Foundations A1",
                    description = "Understand German pronunciation, noun genders (der, die, das), and everyday phrases.",
                    level = "A1 Beginner",
                    totalLessons = 8,
                    units = listOf(
                        UnitItem(
                            id = "u_de_1",
                            courseId = "c_de_101",
                            title = "Kapitel 1: Hallo & Begrüßung",
                            description = "Learn Guten Tag, Wie geht's, and formal vs informal Sie/du.",
                            lessons = listOf(
                                Lesson("l_de_1", "u_de_1", "1. Hallo & Guten Tag", "First greetings and introductions in German", 20, 4, 1, status = LessonStatus.CURRENT),
                                Lesson("l_de_2", "u_de_1", "2. Ich heiße...", "Introduce yourself and ask where someone is from", 25, 5, 2, status = LessonStatus.LOCKED)
                            )
                        )
                    )
                )
            )
            "ja" -> listOf(
                Course(
                    id = "c_ja_101",
                    languageId = "lang_ja",
                    title = "Japanese Starter A1 (Hiragana & Basics)",
                    description = "Master Hiragana characters, essential polite phrases (Desu/Masu), and Japanese culture.",
                    level = "A1 Beginner",
                    totalLessons = 8,
                    units = listOf(
                        UnitItem(
                            id = "u_ja_1",
                            courseId = "c_ja_101",
                            title = "Unit 1: Konnichiwa & Greetings",
                            description = "Master daily greetings, bowing etiquette, and self-introduction.",
                            lessons = listOf(
                                Lesson("l_ja_1", "u_ja_1", "1. こんにちは (Konnichiwa)", "Hello, good morning, and good evening", 20, 5, 1, status = LessonStatus.CURRENT),
                                Lesson("l_ja_2", "u_ja_1", "2. はじめまして (Hajimemashite)", "Nice to meet you and introducing yourself", 25, 6, 2, status = LessonStatus.LOCKED)
                            )
                        )
                    )
                )
            )
            "ko" -> listOf(
                Course(
                    id = "c_ko_101",
                    languageId = "lang_ko",
                    title = "Korean Foundations (Hangul & Phrases)",
                    description = "Learn the scientific Hangul alphabet, polite speech levels, and everyday K-phrases.",
                    level = "A1 Beginner",
                    totalLessons = 8,
                    units = listOf(
                        UnitItem(
                            id = "u_ko_1",
                            courseId = "c_ko_101",
                            title = "Unit 1: 안녕하세요 (Annyeonghaseyo)",
                            description = "First hellos, introductions and polite honorifics.",
                            lessons = listOf(
                                Lesson("l_ko_1", "u_ko_1", "1. 안녕하세요 & Greetings", "Polite daily hellos and gratitude", 20, 5, 1, status = LessonStatus.CURRENT),
                                Lesson("l_ko_2", "u_ko_1", "2. 저는... 입니다 (I am...)", "Introducing your name and profession", 25, 6, 2, status = LessonStatus.LOCKED)
                            )
                        )
                    )
                )
            )
            "it" -> listOf(
                Course(
                    id = "c_it_101",
                    languageId = "lang_it",
                    title = "Italian Essentials A1",
                    description = "Learn melodic Italian greetings, ordering pasta and espresso, and essential verbs.",
                    level = "A1 Beginner",
                    totalLessons = 8,
                    units = listOf(
                        UnitItem(
                            id = "u_it_1",
                            courseId = "c_it_101",
                            title = "Unità 1: Ciao & Saluti",
                            description = "Greetings, introducing yourself and polite expressions.",
                            lessons = listOf(
                                Lesson("l_it_1", "u_it_1", "1. Ciao & Buongiorno", "Daily greetings and introductions", 20, 4, 1, status = LessonStatus.CURRENT)
                            )
                        )
                    )
                )
            )
            "tr" -> listOf(
                Course(
                    id = "c_tr_101",
                    languageId = "lang_tr",
                    title = "Turkish Starter A1",
                    description = "Master Turkish vowel harmony, pleasantries, tea culture, and market bargaining.",
                    level = "A1 Beginner",
                    totalLessons = 8,
                    units = listOf(
                        UnitItem(
                            id = "u_tr_1",
                            courseId = "c_tr_101",
                            title = "Bölüm 1: Merhaba & Tanışma",
                            description = "Hello, nice to meet you, and daily Turkish greetings.",
                            lessons = listOf(
                                Lesson("l_tr_1", "u_tr_1", "1. Merhaba & Nasılsın?", "Saying hello and asking how someone is doing", 20, 4, 1, status = LessonStatus.CURRENT)
                            )
                        )
                    )
                )
            )
            else -> listOf(
                Course(
                    id = "c_en_101",
                    languageId = "lang_en",
                    title = "English for Global Communication",
                    description = "Build a strong foundation in conversational English, vocabulary, and active listening.",
                    level = "A1 Beginner",
                    totalLessons = 8,
                    units = listOf(
                        UnitItem(
                            id = "u_en_1",
                            courseId = "c_en_101",
                            title = "Unit 1: First Impressions & Small Talk",
                            description = "Master hellos, polite icebreakers, occupations and hobbies.",
                            lessons = listOf(
                                Lesson("l_en_1", "u_en_1", "1. Essential Hellos & Greetings", "Saying hello, goodbye, and nice to meet you", 20, 4, 1, status = if (completedLessonIds.contains("l_en_1")) LessonStatus.COMPLETED else LessonStatus.CURRENT),
                                Lesson("l_en_2", "u_en_1", "2. Talking About Your Day", "Describe daily routines, professions and interests", 25, 5, 2, status = LessonStatus.CURRENT),
                                Lesson("l_en_3", "u_en_1", "3. Ordering Coffee & Food", "Order drinks politely with 'I would like...'", 20, 5, 3, status = LessonStatus.LOCKED)
                            )
                        )
                    )
                )
            )
        }
    }

    suspend fun getVocabulary(languageCode: String): Resource<List<VocabularyItem>> = withContext(Dispatchers.IO) {
        if (SupabaseConfig.isConfigured) {
            try {
                val request = Request.Builder()
                    .url("${SupabaseConfig.url}/rest/v1/vocabulary?language_code=eq.$languageCode")
                    .header("apikey", SupabaseConfig.anonKey)
                    .build()
                val response = httpClient.newCall(request).execute()
                val str = response.body?.string() ?: ""
                if (response.isSuccessful && str.isNotBlank()) {
                    val jsonArray = JSONArray(str)
                    val list = mutableListOf<VocabularyItem>()
                    for (i in 0 until jsonArray.length()) {
                        val obj = jsonArray.getJSONObject(i)
                        val id = obj.getString("id")
                        list.add(
                            VocabularyItem(
                                id = id,
                                word = obj.getString("word"),
                                translation = obj.getString("translation"),
                                phonetic = obj.optNullableString("phonetic"),
                                partOfSpeech = obj.optString("part_of_speech", "Noun"),
                                exampleSentence = obj.optNullableString("example_sentence"),
                                languageCode = obj.optString("language_code", languageCode),
                                audioUrl = obj.optNullableString("audio_url"),
                                masteryLevel = obj.optInt("mastery_level", 3),
                                isBookmarked = bookmarkedVocabIds.contains(id)
                            )
                        )
                    }
                    if (list.isNotEmpty()) return@withContext Resource.Success(list)
                }
            } catch (_: Exception) {}
        }

        val vocabList = generateVocabularyForLanguage(languageCode)
        Resource.Success(vocabList)
    }

    private fun generateVocabularyForLanguage(code: String): List<VocabularyItem> {
        return when (code.lowercase()) {
            "ar" -> listOf(
                VocabularyItem("v_ar_1", "مرحباً", "Hello / Welcome", "Marhaban", "Greeting", "مرحباً بك في تطبيق LinguaX", "ar", null, 5, bookmarkedVocabIds.contains("v_ar_1")),
                VocabularyItem("v_ar_2", "شكراً جزيلاً", "Thank you very much", "Shukran Jazeelan", "Expression", "شكراً جزيلاً على مساعدتك الكريمة", "ar", null, 5, bookmarkedVocabIds.contains("v_ar_2")),
                VocabularyItem("v_ar_3", "كيف حالك؟", "How are you?", "Kayfa Haluk?", "Phrase", "أهلاً يا صديقي، كيف حالك اليوم؟", "ar", null, 4, bookmarkedVocabIds.contains("v_ar_3")),
                VocabularyItem("v_ar_4", "الفرصة السعيدة", "Pleased to meet you", "Forsa Sa'eeda", "Expression", "فرصة سعيدة جداً بلقائك", "ar", null, 3, false),
                VocabularyItem("v_ar_5", "كتاب", "Book", "Kitab", "Noun", "أقرأ كتاباً ممتعاً كل مساء", "ar", null, 4, false),
                VocabularyItem("v_ar_6", "قهوة", "Coffee", "Qahwa", "Noun", "أشرب قهوة عربية بالهيل في الصباح", "ar", null, 5, false)
            )
            "fr" -> listOf(
                VocabularyItem("v_fr_1", "Bonjour", "Hello / Good day", "/bɔ̃.ʒuʁ/", "Greeting", "Bonjour, comment allez-vous?", "fr", null, 5, bookmarkedVocabIds.contains("v_fr_1")),
                VocabularyItem("v_fr_2", "Enchanté", "Nice to meet you", "/ɑ̃.ʃɑ̃.te/", "Expression", "Enchanté de faire votre connaissance.", "fr", null, 4, true),
                VocabularyItem("v_fr_3", "Merci beaucoup", "Thank you very much", "/mɛʁ.si bo.ku/", "Expression", "Merci beaucoup pour votre accueil chaleureux.", "fr", null, 5, false),
                VocabularyItem("v_fr_4", "S'il vous plaît", "Please (formal)", "/sil vu plɛ/", "Expression", "Un café noir, s'il vous plaît.", "fr", null, 4, true),
                VocabularyItem("v_fr_5", "Comprendre", "To understand", "/kɔ̃.pʁɑ̃dʁ/", "Verb", "Je commence à bien comprendre le français.", "fr", null, 3, false),
                VocabularyItem("v_fr_6", "L'eau", "Water", "/lo/", "Noun", "Une carafe d'eau, s'il vous plaît.", "fr", null, 5, false)
            )
            "es" -> listOf(
                VocabularyItem("v_es_1", "¡Hola!", "Hello!", "/ˈo.la/", "Greeting", "¡Hola! ¿Qué tal tu día?", "es", null, 5, true),
                VocabularyItem("v_es_2", "Mucho gusto", "Nice to meet you", "/ˈmu.tʃo ˈɣus.to/", "Expression", "Mucho gusto en conocerte.", "es", null, 4, true),
                VocabularyItem("v_es_3", "Por favor", "Please", "/poɾ faˈβoɾ/", "Expression", "La cuenta, por favor.", "es", null, 5, false),
                VocabularyItem("v_es_4", "Gracias", "Thank you", "/ˈɡɾa.sjas/", "Expression", "Muchas gracias por tu ayuda.", "es", null, 5, true)
            )
            "de" -> listOf(
                VocabularyItem("v_de_1", "Guten Tag", "Good day / Hello", "/ˌɡuːtn̩ ˈtaːk/", "Greeting", "Guten Tag, Herr Müller!", "de", null, 5, true),
                VocabularyItem("v_de_2", "Dankeschön", "Thank you very much", "/ˈdaŋkəʃøːn/", "Expression", "Vielen Dank für Ihre Hilfe!", "de", null, 5, true),
                VocabularyItem("v_de_3", "Entschuldigung", "Excuse me / Sorry", "/ɛntˈʃʊldɪɡʊŋ/", "Expression", "Entschuldigung, wo ist der Bahnhof?", "de", null, 4, false)
            )
            "ja" -> listOf(
                VocabularyItem("v_ja_1", "こんにちは", "Hello / Good day", "Konnichiwa", "Greeting", "皆さん、こんにちは！", "ja", null, 5, true),
                VocabularyItem("v_ja_2", "ありがとう", "Thank you", "Arigatou", "Expression", "どうもありがとうございます。", "ja", null, 5, true),
                VocabularyItem("v_ja_3", "すみません", "Excuse me / Sorry", "Sumimasen", "Expression", "すみません、お会計をお願いします。", "ja", null, 4, false)
            )
            "ko" -> listOf(
                VocabularyItem("v_ko_1", "안녕하세요", "Hello (Polite)", "Annyeonghaseyo", "Greeting", "안녕하세요! 반갑습니다.", "ko", null, 5, true),
                VocabularyItem("v_ko_2", "감사합니다", "Thank you (Formal)", "Gamsahamnida", "Expression", "진심으로 감사드립니다.", "ko", null, 5, true)
            )
            else -> listOf(
                VocabularyItem("v_en_1", "Fluency", "طلاقة / Éloquence", "/ˈfluː.ən.si/", "Noun", "Daily practice leads directly to speaking fluency.", "en", null, 5, true),
                VocabularyItem("v_en_2", "Perseverance", "مثابرة / Ténacité", "/ˌpɜː.sɪˈvɪə.rəns/", "Noun", "Language mastery requires consistent perseverance.", "en", null, 4, true),
                VocabularyItem("v_en_3", "Eloquent", "فصيح / Éloquent", "/ˈel.ə.kwənt/", "Adjective", "She gave an eloquent presentation in English.", "en", null, 4, false),
                VocabularyItem("v_en_4", "Immersion", "انغماس / Immersion", "/ɪˈmɜː.ʃən/", "Noun", "Surround yourself with language immersion daily.", "en", null, 5, true)
            )
        }
    }

    suspend fun getDailyChallenges(): Resource<List<DailyChallenge>> = withContext(Dispatchers.IO) {
        if (SupabaseConfig.isConfigured) {
            try {
                val request = Request.Builder()
                    .url("${SupabaseConfig.url}/rest/v1/challenges?is_active=eq.true")
                    .header("apikey", SupabaseConfig.anonKey)
                    .build()
                val response = httpClient.newCall(request).execute()
                val str = response.body?.string() ?: ""
                if (response.isSuccessful && str.isNotBlank()) {
                    val jsonArray = JSONArray(str)
                    val list = mutableListOf<DailyChallenge>()
                    for (i in 0 until jsonArray.length()) {
                        val obj = jsonArray.getJSONObject(i)
                        list.add(
                            DailyChallenge(
                                id = obj.getString("id"),
                                title = obj.getString("title"),
                                description = obj.getString("description"),
                                rewardXp = obj.optInt("reward_xp", 30),
                                rewardCoins = obj.optInt("reward_coins", 15),
                                target = obj.optInt("target", 2),
                                currentProgress = 1,
                                isCompleted = false
                            )
                        )
                    }
                    if (list.isNotEmpty()) return@withContext Resource.Success(list)
                }
            } catch (_: Exception) {}
        }

        val challenges = listOf(
            DailyChallenge("ch_1", "Master 2 Lessons Today", "Complete any two active lessons to earn bonus XP", 35, 15, 2, true, 1, false),
            DailyChallenge("ch_2", "Vocabulary Explorer", "Review 5 flashcards in the vocabulary lab", 25, 10, 5, true, 5, true),
            DailyChallenge("ch_3", "Streak Champion", "Achieve at least 25 XP to preserve your flame", 30, 12, 25, true, 25, true)
        )
        Resource.Success(challenges)
    }

    suspend fun getAchievements(): Resource<List<AchievementItem>> = withContext(Dispatchers.IO) {
        if (SupabaseConfig.isConfigured) {
            try {
                val request = Request.Builder()
                    .url("${SupabaseConfig.url}/rest/v1/achievements?select=*")
                    .header("apikey", SupabaseConfig.anonKey)
                    .build()
                val response = httpClient.newCall(request).execute()
                val str = response.body?.string() ?: ""
                if (response.isSuccessful && str.isNotBlank()) {
                    val jsonArray = JSONArray(str)
                    val list = mutableListOf<AchievementItem>()
                    for (i in 0 until jsonArray.length()) {
                        val obj = jsonArray.getJSONObject(i)
                        list.add(
                            AchievementItem(
                                id = obj.getString("id"),
                                title = obj.getString("title"),
                                description = obj.getString("description"),
                                iconName = obj.optString("icon", "star"),
                                category = obj.optString("category", "General"),
                                maxProgress = obj.optInt("max_progress", 5),
                                isUnlocked = obj.optBoolean("is_unlocked", true),
                                progress = obj.optInt("progress", 5)
                            )
                        )
                    }
                    if (list.isNotEmpty()) return@withContext Resource.Success(list)
                }
            } catch (_: Exception) {}
        }

        val list = listOf(
            AchievementItem("ach_1", "Genesis Learner", "Completed your very first interactive lesson", "star", "Beginner", 1, true, "2026-08-01", 1),
            AchievementItem("ach_2", "7-Day Flame", "Maintained an unbroken 7-day learning streak", "fire", "Streak", 7, true, "2026-08-10", 7),
            AchievementItem("ach_3", "Polyglot Apprentice", "Explored 3 distinct world languages", "globe", "Explorer", 3, true, "2026-08-12", 3),
            AchievementItem("ach_4", "XP Master 500", "Accumulated over 500 total mastery XP", "trophy", "Milestone", 500, false, null, 420),
            AchievementItem("ach_5", "Vocabulary Titan", "Bookmarked and mastered 20 vocabulary cards", "book", "Vocabulary", 20, false, null, 12)
        )
        Resource.Success(list)
    }

    suspend fun getExercisesForLesson(lessonId: String): Resource<List<Exercise>> = withContext(Dispatchers.IO) {
        if (SupabaseConfig.isConfigured) {
            try {
                val request = Request.Builder()
                    .url("${SupabaseConfig.url}/rest/v1/exercises?lesson_id=eq.$lessonId&order=sort_order.asc")
                    .header("apikey", SupabaseConfig.anonKey)
                    .build()
                val response = httpClient.newCall(request).execute()
                val str = response.body?.string() ?: ""
                if (response.isSuccessful && str.isNotBlank()) {
                    val jsonArray = JSONArray(str)
                    val list = mutableListOf<Exercise>()
                    for (i in 0 until jsonArray.length()) {
                        val obj = jsonArray.getJSONObject(i)
                        val optArray = obj.optJSONArray("options") ?: JSONArray()
                        val opts = mutableListOf<String>()
                        for (o in 0 until optArray.length()) {
                            opts.add(optArray.getString(o))
                        }
                        list.add(
                            Exercise(
                                id = obj.getString("id"),
                                lessonId = lessonId,
                                type = obj.optString("type", "MULTIPLE_CHOICE"),
                                question = obj.getString("question"),
                                options = opts,
                                correctAnswer = obj.getString("correct_answer"),
                                explanation = obj.optNullableString("explanation"),
                                audioUrl = obj.optNullableString("audio_url"),
                                imageUrl = obj.optNullableString("image_url"),
                                sortOrder = obj.optInt("sort_order", i + 1)
                            )
                        )
                    }
                    if (list.isNotEmpty()) return@withContext Resource.Success(list)
                }
            } catch (_: Exception) {}
        }

        // Generate tailored dynamic exercise sequence depending on lesson ID
        val exercises = generateExercisesForLesson(lessonId)
        Resource.Success(exercises)
    }

    private fun generateExercisesForLesson(lessonId: String): List<Exercise> {
        return when {
            lessonId.contains("ar") -> listOf(
                Exercise(
                    id = "ex_ar_1",
                    lessonId = lessonId,
                    type = "MULTIPLE_CHOICE",
                    question = "ما هو الرد الصحيح والمهذب على تحية 'السلام عليكم'؟",
                    options = listOf("وعليكم السلام ورحمة الله", "صباح الخير", "شكراً جزيلاً", "أهلاً بك"),
                    correctAnswer = "وعليكم السلام ورحمة الله",
                    explanation = "'وعليكم السلام ورحمة الله' هي التحية الإسلامية الكاملة رداً على السلام."
                ),
                Exercise(
                    id = "ex_ar_2",
                    lessonId = lessonId,
                    type = "VOCABULARY",
                    question = "اختر المعنى الدقيق لكلمة 'Enchanté' باللغة العربية:",
                    options = listOf("فرصة سعيدة / تشرفت بمعرفتك", "إلى اللقاء", "من فضلك", "عفواً"),
                    correctAnswer = "فرصة سعيدة / تشرفت بمعرفتك",
                    explanation = "تُستخدم عند التعارف لأول مرة للتعبير عن السرور باللقاء."
                ),
                Exercise(
                    id = "ex_ar_3",
                    lessonId = lessonId,
                    type = "FILL_IN_BLANK",
                    question = "أكمل الجملة: 'أنا مسافر إلى مكة المكرمة ___ الطائرة.'",
                    options = listOf("على متن", "تحت", "بجانب", "خلف"),
                    correctAnswer = "على متن",
                    explanation = "نقول 'على متن الطائرة' أو 'بواسطة الطائرة'."
                ),
                Exercise(
                    id = "ex_ar_4",
                    lessonId = lessonId,
                    type = "PRONUNCIATION",
                    question = "استمع وردد العبارة التالية بنطق سليم: 'أهلاً وسهلاً بكم في عالم LinguaX'",
                    options = listOf("أهلاً وسهلاً بكم في عالم LinguaX", "مع السلامة", "شكراً", "صباح الخير"),
                    correctAnswer = "أهلاً وسهلاً بكم في عالم LinguaX",
                    explanation = "انتبه لمخارج الحروف والمد الطبيعي في الكلمات."
                )
            )
            lessonId.contains("fr") -> listOf(
                Exercise(
                    id = "ex_fr_1",
                    lessonId = lessonId,
                    type = "MULTIPLE_CHOICE",
                    question = "How do you say 'Good morning' and polite daytime 'Hello' in French?",
                    options = listOf("Bonjour", "Bonsoir", "Au revoir", "Merci"),
                    correctAnswer = "Bonjour",
                    explanation = "'Bonjour' is the universal daytime greeting in French (Bon + Jour = Good day)."
                ),
                Exercise(
                    id = "ex_fr_2",
                    lessonId = lessonId,
                    type = "VOCABULARY",
                    question = "Translate 'Nice to meet you' into French:",
                    options = listOf("Enchanté", "S'il vous plaît", "De rien", "Pardon"),
                    correctAnswer = "Enchanté",
                    explanation = "'Enchanté' is the standard polite expression when meeting someone for the first time."
                ),
                Exercise(
                    id = "ex_fr_3",
                    lessonId = lessonId,
                    type = "TRANSLATION",
                    question = "Select the formal expression for 'Please':",
                    options = listOf("S'il vous plaît", "Merci beaucoup", "Comment allez-vous", "Excusez-moi"),
                    correctAnswer = "S'il vous plaît",
                    explanation = "'S'il vous plaît' literally means 'If it pleases you' in formal address."
                ),
                Exercise(
                    id = "ex_fr_4",
                    lessonId = lessonId,
                    type = "FILL_IN_BLANK",
                    question = "Complete the sentence: 'Je voudrais un café, ___.'",
                    options = listOf("s'il vous plaît", "merci", "bonjour", "au revoir"),
                    correctAnswer = "s'il vous plaît",
                    explanation = "Polite ordering at French cafés always finishes with 's'il vous plaît'."
                )
            )
            lessonId.contains("es") -> listOf(
                Exercise(
                    id = "ex_es_1",
                    lessonId = lessonId,
                    type = "MULTIPLE_CHOICE",
                    question = "How do you ask 'How are you?' in Spanish?",
                    options = listOf("¿Cómo estás?", "Mucho gusto", "Hasta luego", "Buenas noches"),
                    correctAnswer = "¿Cómo estás?",
                    explanation = "'¿Cómo estás?' is the informal way to ask someone how they are doing."
                ),
                Exercise(
                    id = "ex_es_2",
                    lessonId = lessonId,
                    type = "VOCABULARY",
                    question = "Translate 'Thank you very much':",
                    options = listOf("Muchas gracias", "De nada", "Por favor", "Perdón"),
                    correctAnswer = "Muchas gracias",
                    explanation = "'Muchas gracias' adds emphasis to the standard 'Gracias'."
                )
            )
            else -> listOf(
                Exercise(
                    id = "ex_en_1",
                    lessonId = lessonId,
                    type = "MULTIPLE_CHOICE",
                    question = "What is the most natural way to introduce yourself in English?",
                    options = listOf("Hi, my name is Alex, nice to meet you!", "Goodbye, I go.", "I have name.", "What time is it?"),
                    correctAnswer = "Hi, my name is Alex, nice to meet you!",
                    explanation = "'My name is...' followed by 'Nice to meet you' is the standard polite intro."
                ),
                Exercise(
                    id = "ex_en_2",
                    lessonId = lessonId,
                    type = "FILL_IN_BLANK",
                    question = "Complete the polite coffee order: 'I ___ like a cappuccino with oat milk.'",
                    options = listOf("would", "can", "am", "do"),
                    correctAnswer = "would",
                    explanation = "'I would like' is the most polite and natural way to order food or drink."
                ),
                Exercise(
                    id = "ex_en_3",
                    lessonId = lessonId,
                    type = "TRANSLATION",
                    question = "Select the best synonym for 'Perseverance':",
                    options = listOf("Persistence / Tenacity", "Giving up easily", "Hesitation", "Sloth"),
                    correctAnswer = "Persistence / Tenacity",
                    explanation = "Perseverance means steadfastness in doing something despite difficulty or delay."
                )
            )
        }
    }

    private fun String.capitalizeWords(): String {
        return this.split(" ").joinToString(" ") { word -> word.replaceFirstChar { it.uppercase() } }
    }
}
