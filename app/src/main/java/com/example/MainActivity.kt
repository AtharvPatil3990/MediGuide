package com.example

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.core.localization.LocalMediStrings
import com.example.core.localization.stringsFor
import com.example.core.security.BiometricAuthManager
import com.example.core.security.BiometricResult
import com.example.ui.MainViewModel
import com.example.ui.components.BiometricLockOverlay
import com.example.ui.navigation.MediGuideShell
import com.example.ui.theme.MediGuideTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : FragmentActivity() {

    private val mainViewModel: MainViewModel by viewModels()

    @Inject lateinit var biometricAuthManager: BiometricAuthManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val mainState by mainViewModel.uiState.collectAsStateWithLifecycle()
            val localizedStrings = remember(mainState.language) { stringsFor(mainState.language) }
            var biometricError by remember { mutableStateOf<String?>(null) }

            val triggerBiometricPrompt = remember(localizedStrings) {
                {
                    biometricError = null
                    biometricAuthManager.authenticate(
                        activity = this@MainActivity,
                        title = localizedStrings.biometricPromptTitle,
                        subtitle = localizedStrings.biometricPromptSubtitle,
                        cancelLabel = localizedStrings.cancel
                    ) { result ->
                        when (result) {
                            BiometricResult.Success -> mainViewModel.unlockVault()
                            BiometricResult.Cancelled -> Unit
                            BiometricResult.Unavailable -> mainViewModel.unlockVault()
                            is BiometricResult.Error -> biometricError = result.message
                        }
                    }
                }
            }

            LaunchedEffect(mainState.isLocked) {
                if (mainState.isLocked && mainState.canUseBiometrics) {
                    triggerBiometricPrompt()
                }
            }

            CompositionLocalProvider(LocalMediStrings provides localizedStrings) {
                MediGuideTheme {
                    if (mainState.isLocked) {
                        BiometricLockOverlay(
                            canUseBiometrics = mainState.canUseBiometrics,
                            errorMessage = biometricError,
                            onRequestBiometricUnlock = triggerBiometricPrompt,
                            onFallbackUnlock = { mainViewModel.unlockVault() }
                        )
                    } else {
                        MediGuideShell(
                            mainState = mainState,
                            onSelectProfile = mainViewModel::switchProfile,
                            onSelectLanguage = mainViewModel::setLanguage,
                            onToggleBiometricLock = mainViewModel::setBiometricLockEnabled,
                            onTestLockScreen = mainViewModel::lockVaultNow
                        )
                    }
                }
            }
        }
    }
}
