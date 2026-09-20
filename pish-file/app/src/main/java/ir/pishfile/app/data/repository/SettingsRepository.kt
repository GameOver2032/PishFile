package ir.pishfile.app.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "pishfile_settings")

/**
 * تنظیمات برنامه + محل نگه‌داری «زمان آخرین همگام‌سازی».
 */
class SettingsRepository(private val context: Context) {

    private object Keys {
        val OFFICE_NAME = stringPreferencesKey("office_name")
        val AGENT_NAME = stringPreferencesKey("agent_name")
        val AGENT_PHONE = stringPreferencesKey("agent_phone")
        val DEFAULT_CITY = stringPreferencesKey("default_city")
        val DEFAULT_PAYMENT_TYPE = stringPreferencesKey("default_payment_type")
        val DEFAULT_INSTALLMENT_COUNT = stringPreferencesKey("default_installment_count")
        val DEFAULT_INTEREST_NOTE = stringPreferencesKey("default_interest_note")
        val SHOW_AMOUNTS_IN_MILLIONS = booleanPreferencesKey("show_amounts_in_millions")
        val PERSIAN_DIGITS = booleanPreferencesKey("persian_digits")
        val AUTO_GENERATE_INSTALLMENTS = booleanPreferencesKey("auto_generate_installments")
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val LAST_SYNC_AT = stringPreferencesKey("last_sync_at")
        val SERVER_URL = stringPreferencesKey("server_url")
        val SYNC_ENABLED = booleanPreferencesKey("sync_enabled")
    }

    val officeName: Flow<String> = context.dataStore.data.map { it[Keys.OFFICE_NAME] ?: "" }
    val agentName: Flow<String> = context.dataStore.data.map { it[Keys.AGENT_NAME] ?: "" }
    val agentPhone: Flow<String> = context.dataStore.data.map { it[Keys.AGENT_PHONE] ?: "" }
    val defaultCity: Flow<String> = context.dataStore.data.map { it[Keys.DEFAULT_CITY] ?: "" }
    val defaultPaymentType: Flow<String> = context.dataStore.data.map { it[Keys.DEFAULT_PAYMENT_TYPE] ?: "INSTALLMENT" }
    val defaultInstallmentCount: Flow<String> = context.dataStore.data.map { it[Keys.DEFAULT_INSTALLMENT_COUNT] ?: "24" }
    val showAmountsInMillions: Flow<Boolean> = context.dataStore.data.map { it[Keys.SHOW_AMOUNTS_IN_MILLIONS] ?: true }
    val persianDigits: Flow<Boolean> = context.dataStore.data.map { it[Keys.PERSIAN_DIGITS] ?: true }
    val autoGenerateInstallments: Flow<Boolean> = context.dataStore.data.map { it[Keys.AUTO_GENERATE_INSTALLMENTS] ?: true }
    val themeMode: Flow<String> = context.dataStore.data.map { it[Keys.THEME_MODE] ?: "system" }
    val lastSyncAt: Flow<String> = context.dataStore.data.map { it[Keys.LAST_SYNC_AT] ?: "" }
    val serverUrl: Flow<String> = context.dataStore.data.map { it[Keys.SERVER_URL] ?: "" }
    val syncEnabled: Flow<Boolean> = context.dataStore.data.map { it[Keys.SYNC_ENABLED] ?: false }

    suspend fun setOfficeName(value: String) = context.dataStore.edit { it[Keys.OFFICE_NAME] = value }
    suspend fun setAgentName(value: String) = context.dataStore.edit { it[Keys.AGENT_NAME] = value }
    suspend fun setAgentPhone(value: String) = context.dataStore.edit { it[Keys.AGENT_PHONE] = value }
    suspend fun setDefaultCity(value: String) = context.dataStore.edit { it[Keys.DEFAULT_CITY] = value }
    suspend fun setDefaultPaymentType(value: String) = context.dataStore.edit { it[Keys.DEFAULT_PAYMENT_TYPE] = value }
    suspend fun setDefaultInstallmentCount(value: String) = context.dataStore.edit { it[Keys.DEFAULT_INSTALLMENT_COUNT] = value }
    suspend fun setShowAmountsInMillions(value: Boolean) = context.dataStore.edit { it[Keys.SHOW_AMOUNTS_IN_MILLIONS] = value }
    suspend fun setPersianDigits(value: Boolean) = context.dataStore.edit { it[Keys.PERSIAN_DIGITS] = value }
    suspend fun setAutoGenerateInstallments(value: Boolean) = context.dataStore.edit { it[Keys.AUTO_GENERATE_INSTALLMENTS] = value }
    suspend fun setThemeMode(value: String) = context.dataStore.edit { it[Keys.THEME_MODE] = value }
    suspend fun setLastSyncAt(value: String) = context.dataStore.edit { it[Keys.LAST_SYNC_AT] = value }
    suspend fun setServerUrl(value: String) = context.dataStore.edit { it[Keys.SERVER_URL] = value }
    suspend fun setSyncEnabled(value: Boolean) = context.dataStore.edit { it[Keys.SYNC_ENABLED] = value }
}
