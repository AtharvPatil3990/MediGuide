package com.example.ui.screens.profiles

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.core.localization.AppLanguage
import com.example.core.localization.LocalMediStrings
import com.example.data.local.entity.UserRole
import com.example.ui.MainAppUiState
import com.example.ui.components.ProfileAvatarBadge

@Composable
fun ProfilesSecurityScreen(
    mainState: MainAppUiState,
    onSelectProfile: (Long) -> Unit,
    onSelectLanguage: (AppLanguage) -> Unit,
    onToggleBiometricLock: (Boolean) -> Unit,
    onTestLockScreen: () -> Unit,
    viewModel: ProfilesSecurityViewModel = hiltViewModel()
) {
    val strings = LocalMediStrings.current
    var showAddProfileDialog by rememberSaveable { mutableStateOf(false) }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 680.dp)
            ) {
                Text(strings.profilesTitle, style = MaterialTheme.typography.titleLarge)
                FilledTonalButton(
                    onClick = { showAddProfileDialog = true },
                    modifier = Modifier.testTag("add_profile_btn")
                ) {
                    Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(strings.addProfile)
                }
            }
        }

        items(mainState.allProfiles, key = { it.id }) { profile ->
            val isActive = profile.id == mainState.activeProfile?.id
            val roleLabel = when (profile.role) {
                UserRole.PRIMARY -> strings.rolePrimary
                UserRole.ELDER -> strings.roleElder
                UserRole.CHILD -> strings.roleChild
                UserRole.MOTHER -> strings.roleMother
            }
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = if (isActive) MaterialTheme.colorScheme.primaryContainer
                else MaterialTheme.colorScheme.surface,
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 680.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .clickable { onSelectProfile(profile.id) }
                    .testTag("profile_card_${profile.id}")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(14.dp)
                ) {
                    ProfileAvatarBadge(name = profile.name, isActive = isActive)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(profile.name, style = MaterialTheme.typography.titleMedium)
                        Text(
                            text = "$roleLabel • ${strings.ageLabel} ${profile.age} • ${profile.bloodGroup}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (isActive) {
                        Icon(
                            imageVector = Icons.Filled.CheckCircle,
                            contentDescription = strings.activeProfileLabel,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 680.dp)
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Language, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(strings.languageSectionTitle, style = MaterialTheme.typography.titleMedium)
                    }
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.horizontalScroll(rememberScrollState())
                    ) {
                        AppLanguage.entries.forEach { lang ->
                            FilterChip(
                                selected = mainState.language == lang,
                                onClick = { onSelectLanguage(lang) },
                                label = { Text("${lang.nativeName} (${lang.englishName})") },
                                modifier = Modifier.testTag("lang_chip_${lang.code}")
                            )
                        }
                    }
                }
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 680.dp)
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Fingerprint, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(strings.securitySectionTitle, style = MaterialTheme.typography.titleMedium)
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(strings.biometricLockTitle, style = MaterialTheme.typography.titleMedium)
                            Text(
                                text = strings.biometricLockSubtitle,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Switch(
                            checked = mainState.biometricLockEnabled,
                            onCheckedChange = onToggleBiometricLock,
                            modifier = Modifier.testTag("biometric_lock_switch")
                        )
                    }

                    OutlinedButton(
                        onClick = onTestLockScreen,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("test_lock_screen_btn")
                    ) {
                        Icon(Icons.Filled.Lock, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(strings.lockNowButton)
                    }
                }
            }
        }
    }

    if (showAddProfileDialog) {
        var name by rememberSaveable { mutableStateOf("") }
        var ageText by rememberSaveable { mutableStateOf("30") }
        var bloodGroup by rememberSaveable { mutableStateOf("O+") }
        var role by rememberSaveable { mutableStateOf(UserRole.PRIMARY) }

        AlertDialog(
            onDismissRequest = { showAddProfileDialog = false },
            title = { Text(strings.addProfile) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text(strings.profileNameLabel) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(strings.profileRoleLabel, style = MaterialTheme.typography.labelMedium)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.horizontalScroll(rememberScrollState())
                    ) {
                        UserRole.entries.forEach { r ->
                            val rLabel = when (r) {
                                UserRole.PRIMARY -> strings.rolePrimary
                                UserRole.ELDER -> strings.roleElder
                                UserRole.CHILD -> strings.roleChild
                                UserRole.MOTHER -> strings.roleMother
                            }
                            FilterChip(
                                selected = role == r,
                                onClick = { role = r },
                                label = { Text(rLabel) }
                            )
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = ageText,
                            onValueChange = { ageText = it.filter { c -> c.isDigit() } },
                            label = { Text(strings.ageLabel) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = bloodGroup,
                            onValueChange = { bloodGroup = it },
                            label = { Text(strings.bloodGroupLabel) },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    viewModel.createProfile(
                        name = name,
                        role = role,
                        age = ageText.toIntOrNull() ?: 30,
                        bloodGroup = bloodGroup
                    )
                    showAddProfileDialog = false
                }) {
                    Text(strings.addProfile)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddProfileDialog = false }) {
                    Text(strings.cancel)
                }
            }
        )
    }
}
