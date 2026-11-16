package com.example.data.local.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.core.localization.AppLanguage
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.mediPrefsDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "mediguide_preferences"
)

@Singleton
class AppPreferencesDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private object Keys {
        val ACTIVE_USER_ID = longPreferencesKey("active_user_id")
        val LANGUAGE_CODE = stringPreferencesKey("language_code")
        val BIOMETRIC_LOCK_ENABLED = booleanPreferencesKey("biometric_lock_enabled")
    }

    val activeUserIdFlow: Flow<Long?> = context.mediPrefsDataStore.data.map { prefs ->
        prefs[Keys.ACTIVE_USER_ID]
    }

    val languageFlow: Flow<AppLanguage> = context.mediPrefsDataStore.data.map { prefs ->
        AppLanguage.fromCode(prefs[Keys.LANGUAGE_CODE])
    }

    val biometricLockEnabledFlow: Flow<Boolean> = context.mediPrefsDataStore.data.map { prefs ->
        prefs[Keys.BIOMETRIC_LOCK_ENABLED] ?: false
    }

    suspend fun setActiveUserId(userId: Long) {
        context.mediPrefsDataStore.edit { prefs ->
            prefs[Keys.ACTIVE_USER_ID] = userId
        }
    }

    suspend fun setLanguage(language: AppLanguage) {
        context.mediPrefsDataStore.edit { prefs ->
            prefs[Keys.LANGUAGE_CODE] = language.code
        }
    }

    suspend fun setBiometricLockEnabled(enabled: Boolean) {
        context.mediPrefsDataStore.edit { prefs ->
            prefs[Keys.BIOMETRIC_LOCK_ENABLED] = enabled
        }
    }
}
