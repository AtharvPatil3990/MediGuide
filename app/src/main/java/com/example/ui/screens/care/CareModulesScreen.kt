package com.example.ui.screens.care

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ChildCare
import androidx.compose.material.icons.filled.Elderly
import androidx.compose.material.icons.filled.Vaccines
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.core.localization.LocalMediStrings
import kotlin.math.roundToInt

@Composable
fun CareModulesScreen(
    viewModel: CareModulesViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val strings = LocalMediStrings.current
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    var showAddAppointmentDialog by rememberSaveable { mutableStateOf(false) }

    var weekValue by rememberSaveable { mutableIntStateOf(24) }
    var weightText by rememberSaveable { mutableStateOf("62.5") }
    var bloodGroupText by rememberSaveable { mutableStateOf("A+") }

    LaunchedEffect(uiState.motherBabyModule) {
        uiState.motherBabyModule?.let { mod ->
            weekValue = mod.pregnancyWeek
            weightText = mod.maternalWeightKg.toString()
            bloodGroupText = mod.bloodGroup
        }
    }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            Text(
                text = strings.careModulesTitle,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 680.dp)
            )
        }

        item {
            SingleChoiceSegmentedButtonRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 680.dp)
            ) {
                SegmentedButton(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                    icon = { Icon(Icons.Filled.ChildCare, contentDescription = null) },
                    modifier = Modifier.testTag("tab_mother_baby")
                ) {
                    Text(strings.motherBabyTab)
                }
                SegmentedButton(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                    icon = { Icon(Icons.Filled.Elderly, contentDescription = null) },
                    modifier = Modifier.testTag("tab_elder_care")
                ) {
                    Text(strings.elderCareTab)
                }
            }
        }

        if (selectedTab == 0) {
            item {
                Card(
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 680.dp)
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.img_care_mother_baby),
                            contentDescription = strings.motherBabyTab,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                                .clip(RoundedCornerShape(16.dp))
                        )
                        Text(
                            text = strings.pregnancyTrackerTitle,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "${strings.gestationalWeekLabel}: $weekValue / 40",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                        var sliderPos by rememberSaveable(weekValue) {
                            mutableFloatStateOf(weekValue.toFloat())
                        }
                        Slider(
                            value = sliderPos,
                            onValueChange = {
                                sliderPos = it
                                weekValue = it.roundToInt()
                            },
                            valueRange = 1f..42f,
                            modifier = Modifier.testTag("pregnancy_week_slider")
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = weightText,
                                onValueChange = { weightText = it },
                                label = { Text(strings.weightKgLabel) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = bloodGroupText,
                                onValueChange = { bloodGroupText = it },
                                label = { Text(strings.bloodGroupLabel) },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Button(
                            onClick = {
                                viewModel.savePregnancyProgress(
                                    week = weekValue,
                                    weightKg = weightText.toFloatOrNull() ?: 62.5f,
                                    bloodGroup = bloodGroupText
                                )
                            },
                            modifier = Modifier
                                .align(Alignment.End)
                                .testTag("save_pregnancy_btn")
                        ) {
                            Text(strings.updatePregnancyInfo)
                        }
                    }
                }
            }

            item {
                Text(
                    text = strings.babyMilestonesTitle,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 680.dp)
                )
            }

            items(uiState.milestones, key = { "m_${it.id}" }) { milestone ->
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 680.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { viewModel.toggleMilestone(milestone) }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Checkbox(
                            checked = milestone.achieved,
                            onCheckedChange = { viewModel.toggleMilestone(milestone) }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(milestone.title, style = MaterialTheme.typography.titleMedium)
                            Text(
                                text = "${strings.expectedAgePrefix} ${milestone.expectedAgeMonths} mo",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            item {
                Text(
                    text = strings.vaccinationScheduleTitle,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 680.dp)
                )
            }

            items(uiState.vaccinations, key = { "v_${it.id}" }) { vac ->
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 680.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { viewModel.toggleVaccination(vac) }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Checkbox(
                            checked = vac.completed,
                            onCheckedChange = { viewModel.toggleVaccination(vac) }
                        )
                        Icon(
                            imageVector = Icons.Filled.Vaccines,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(vac.vaccineName, style = MaterialTheme.typography.titleMedium)
                            Text(
                                text = "${strings.dueDatePrefix} ${vac.dueDateLabel}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        } else {
            item {
                Card(
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 680.dp)
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.img_care_elder),
                            contentDescription = strings.elderCareTab,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                                .clip(RoundedCornerShape(16.dp))
                        )
                        Text(
                            text = strings.targetedMedSummaryTitle,
                            style = MaterialTheme.typography.titleMedium
                        )
                        if (uiState.targetedMedicines.isEmpty()) {
                            Text(
                                text = strings.noDosesToday,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            uiState.targetedMedicines.forEach { med ->
                                Row(
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = "${med.name} (${med.dosage})",
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = med.scheduleTimes.joinToString(", "),
                                        style = MaterialTheme.typography.labelLarge,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 680.dp)
                ) {
                    Text(
                        text = strings.elderAppointmentsTitle,
                        style = MaterialTheme.typography.titleMedium
                    )
                    FilledTonalButton(
                        onClick = { showAddAppointmentDialog = true },
                        modifier = Modifier.testTag("add_appointment_btn")
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(strings.addAppointment)
                    }
                }
            }

            items(uiState.elderAppointments, key = { "a_${it.id}" }) { appt ->
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 680.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { viewModel.toggleAppointmentCompleted(appt) }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Checkbox(
                            checked = appt.completed,
                            onCheckedChange = { viewModel.toggleAppointmentCompleted(appt) }
                        )
                        Icon(
                            imageVector = Icons.Filled.CalendarMonth,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.tertiary
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(appt.doctorName, style = MaterialTheme.typography.titleMedium)
                            Text(
                                text = "${appt.specialty} • ${appt.dateTimeLabel}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }

    if (showAddAppointmentDialog) {
        var doctor by rememberSaveable { mutableStateOf("") }
        var specialty by rememberSaveable { mutableStateOf("") }
        var dateTime by rememberSaveable { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showAddAppointmentDialog = false },
            title = { Text(strings.addAppointment) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = doctor,
                        onValueChange = { doctor = it },
                        label = { Text(strings.doctorNameLabel) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = specialty,
                        onValueChange = { specialty = it },
                        label = { Text(strings.specialtyLabel) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = dateTime,
                        onValueChange = { dateTime = it },
                        label = { Text(strings.appointmentDateLabel) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    viewModel.addElderAppointment(doctor, specialty, dateTime, "")
                    showAddAppointmentDialog = false
                }) {
                    Text(strings.addAppointment)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddAppointmentDialog = false }) {
                    Text(strings.cancel)
                }
            }
        )
    }
}
