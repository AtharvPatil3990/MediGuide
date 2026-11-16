package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.localization.AppLanguage
import com.example.core.security.BiometricAuthManager
import com.example.data.local.preferences.AppPreferencesDataStore
import com.example.domain.model.UserProfile
import com.example.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MainAppUiState(
    val activeProfile: UserProfile? = null,
    val allProfiles: List<UserProfile> = emptyList(),
    val language: AppLanguage = AppLanguage.ENGLISH,
    val biometricLockEnabled: Boolean = false,
    val isLocked: Boolean = false,
    val canUseBiometrics: Boolean = false
)

@HiltViewModel
class MainViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val preferences: AppPreferencesDataStore,
    private val biometricAuthManager: BiometricAuthManager
) : ViewModel() {

    private val _isLocked = MutableStateFlow(false)

    val uiState: StateFlow<MainAppUiState> = combine(
        userRepository.observeActiveProfile(),
        userRepository.observeProfiles(),
        preferences.languageFlow,
        preferences.biometricLockEnabledFlow,
        _isLocked
    ) { active, profiles, language, lockEnabled, isLocked ->
        MainAppUiState(
            activeProfile = active,
            allProfiles = profiles,
            language = language,
            biometricLockEnabled = lockEnabled,
            isLocked = isLocked,
            canUseBiometrics = biometricAuthManager.canAuthenticate()
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = MainAppUiState(canUseBiometrics = biometricAuthManager.canAuthenticate())
    )

    init {
        viewModelScope.launch {
            userRepository.ensureDefaultProfilesSeeded()
            val lockOnStartup = preferences.biometricLockEnabledFlow.first()
            if (lockOnStartup && biometricAuthManager.canAuthenticate()) {
                _isLocked.value = true
            }
        }
    }

    fun setLanguage(language: AppLanguage) {
        viewModelScope.launch {
            preferences.setLanguage(language)
        }
    }

    fun switchProfile(userId: Long) {
        viewModelScope.launch {
            userRepository.setActiveProfile(userId)
        }
    }

    fun setBiometricLockEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferences.setBiometricLockEnabled(enabled)
            if (!enabled) {
                _isLocked.value = false
            }
        }
    }

    fun lockVaultNow() {
        _isLocked.value = true
    }

    fun unlockVault() {
        _isLocked.value = false
    }
}
