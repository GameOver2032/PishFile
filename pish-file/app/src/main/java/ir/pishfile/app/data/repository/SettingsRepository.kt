package ir.pishfile.app.data.repository

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "pishfile_settings")

/**
 * تنظیمات محلی مشاور املاک.
 */
class SettingsRepository(private val context: Context) {

    private object Keys {
        val OFFICE_NAME = stringPreferencesKey("office_name")
        val AGENT_NAME = stringPreferencesKey("agent_name")
        val AGENT_PHONE = stringPreferencesKey("agent_phone")
        val DEFAULT_CITY = stringPreferencesKey("default_city")
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val PERSIAN_DIGITS = booleanPreferencesKey("persian_digits")
    }

    val officeName: Flow<String> = context.dataStore.data.map { it[Keys.OFFICE_NAME] ?: "" }
    val agentName: Flow<String> = context.dataStore.data.map { it[Keys.AGENT_NAME] ?: "" }
    val agentPhone: Flow<String> = context.dataStore.data.map { it[Keys.AGENT_PHONE] ?: "" }
    val defaultCity: Flow<String> = context.dataStore.data.map { it[Keys.DEFAULT_CITY] ?: "تهران" }
    val themeMode: Flow<String> = context.dataStore.data.map { it[Keys.THEME_MODE] ?: "system" }
    val persianDigits: Flow<Boolean> = context.dataStore.data.map { it[Keys.PERSIAN_DIGITS] ?: true }

    suspend fun setOfficeName(value: String) = context.dataStore.edit { it[Keys.OFFICE_NAME] = value }
    suspend fun setAgentName(value: String) = context.dataStore.edit { it[Keys.AGENT_NAME] = value }
    suspend fun setAgentPhone(value: String) = context.dataStore.edit { it[Keys.AGENT_PHONE] = value }
    suspend fun setDefaultCity(value: String) = context.dataStore.edit { it[Keys.DEFAULT_CITY] = value }
    suspend fun setThemeMode(value: String) = context.dataStore.edit { it[Keys.THEME_MODE] = value }
    suspend fun setPersianDigits(value: Boolean) = context.dataStore.edit { it[Keys.PERSIAN_DIGITS] = value }
}
