package com.example.data.i18n

import androidx.compose.ui.unit.LayoutDirection

enum class AppLanguage(val code: String, val displayName: String, val flag: String, val isRtl: Boolean) {
    ENGLISH("en", "English", "🇺🇸", false),
    FRENCH("fr", "Français", "🇫🇷", false),
    ARABIC("ar", "العربية", "🇸🇦", true);

    val layoutDirection: LayoutDirection
        get() = if (isRtl) LayoutDirection.Rtl else LayoutDirection.Ltr

    companion object {
        fun fromCode(code: String): AppLanguage {
            return values().find { it.code.equals(code, ignoreCase = true) } ?: ENGLISH
        }
    }
}
