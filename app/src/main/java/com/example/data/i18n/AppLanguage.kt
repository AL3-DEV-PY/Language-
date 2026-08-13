package com.example.data.i18n

import androidx.compose.ui.unit.LayoutDirection

enum class AppLanguage(
    val code: String,
    val displayName: String,
    val nativeName: String,
    val flag: String,
    val isRtl: Boolean
) {
    ENGLISH("en", "English", "English", "🇺🇸", false),
    ARABIC("ar", "Arabic", "العربية", "🇸🇦", true),
    FRENCH("fr", "French", "Français", "🇫🇷", false),
    SPANISH("es", "Spanish", "Español", "🇪🇸", false),
    GERMAN("de", "German", "Deutsch", "🇩🇪", false),
    ITALIAN("it", "Italian", "Italiano", "🇮🇹", false),
    TURKISH("tr", "Turkish", "Türkçe", "🇹🇷", false),
    JAPANESE("ja", "Japanese", "日本語", "🇯🇵", false),
    KOREAN("ko", "Korean", "한국어", "🇰🇷", false);

    val layoutDirection: LayoutDirection
        get() = if (isRtl) LayoutDirection.Rtl else LayoutDirection.Ltr

    companion object {
        fun fromCode(code: String): AppLanguage {
            return values().find { it.code.equals(code, ignoreCase = true) } ?: ENGLISH
        }
    }
}
