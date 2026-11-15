package com.example.mathcalc.settings

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.mathcalc.math.L10n

/**
 * In-app settings: language ("ru"/"en") and dark theme, persisted to SharedPreferences.
 * Backed by mutableStateOf so any change triggers recomposition.
 */
object AppSettings {

    private const val PREFS_NAME = "mathcalc_prefs"
    private const val KEY_LANG = "language"
    private const val KEY_DARK = "dark_theme"

    var language: String by mutableStateOf("ru")
        private set

    var darkTheme: Boolean by mutableStateOf(true)
        private set

    private var prefs: SharedPreferences? = null

    /** Loads persisted settings; call once at app start. */
    fun init(context: Context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        language = prefs?.getString(KEY_LANG, "ru") ?: "ru"
        darkTheme = prefs?.getBoolean(KEY_DARK, true) ?: true
        syncL10n()
    }

    @JvmName("setLanguagePref")
    fun setLanguage(lang: String) {
        language = if (lang == "en") "en" else "ru"
        syncL10n()
        prefs?.edit()?.putString(KEY_LANG, language)?.apply()
    }

    @JvmName("setDarkThemePref")
    fun setDarkTheme(dark: Boolean) {
        darkTheme = dark
        prefs?.edit()?.putBoolean(KEY_DARK, dark)?.apply()
    }

    fun toggleLanguage() {
        setLanguage(if (language == "ru") "en" else "ru")
    }

    fun toggleTheme() {
        setDarkTheme(!darkTheme)
    }

    private fun syncL10n() {
        L10n.lang = language
    }
}

/**
 * UI string lookup: `Loc["history"]`. Delegates to math.L10n so both the UI
 * and the math logic share one translation table and stay in sync.
 */
object Loc {
    operator fun get(key: String): String {
        // Reading the state here makes language changes trigger recomposition
        // of every composable that translates a string via Loc[...].
        AppSettings.language
        return L10n.t(key)
    }
}
