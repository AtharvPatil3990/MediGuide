package com.example.ui.screens.medications

import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.core.localization.LocalMediStrings
import com.example.data.local.entity.MedicationFrequency
import com.example.data.local.entity.ReminderStatus
import com.example.domain.model.Medicine
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun MedicationsScreen(
    openAddDialogInitially: Boolean = false,
    onDismissInitialAdd: () -> Unit = {},
    viewModel: MedicationsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val strings = LocalMediStrings.current
    var showAddDialog by rememberSaveable { mutableStateOf(openAddDialogInitially) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("fab_add_medicine")
            ) {
                Icon(Icons.Filled.Add, contentDescription = strings.addMedicine)
            }
        }
    ) { innerPadding ->
        LazyColumn(
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = innerPadding.calculateTopPadding() + 8.dp,
                bottom = innerPadding.calculateBottomPadding() + 88.dp
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp),
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
                    Text(
                        text = strings.medicinesTitle,
                        style = MaterialTheme.typography.titleLarge
                    )
                }
            }

            if (uiState.medicines.isEmpty()) {
                item {
                    Card(
                        shape = RoundedCornerShape(22.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier
                            .fillMaxWidth()
                            .widthIn(max = 680.dp)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Medication,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(44.dp)
                            )
                            Text(strings.noDosesToday, style = MaterialTheme.typography.titleMedium)
                            Text(
                                text = strings.addFirstMedicineHint,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                items(uiState.medicines, key = { it.id }) { medicine ->
                    MedicineCard(
                        medicine = medicine,
                        onRefill = { viewModel.refillStock(medicine.id) },
                        onDelete = { viewModel.deleteMedicine(medicine.id) },
                        modifier = Modifier.widthIn(max = 680.dp)
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = strings.adherenceHistoryTitle,
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 680.dp)
                )
            }

            if (uiState.doseLogs.isEmpty()) {
                item {
                    Text(
                        text = strings.noHistoryYet,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .fillMaxWidth()
                            .widthIn(max = 680.dp)
                    )
                }
            } else {
                items(uiState.doseLogs.take(12), key = { it.id }) { log ->
                    val fmt = remember { SimpleDateFormat("MMM d, hh:mm a", Locale.getDefault()) }
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surface,
                        modifier = Modifier
                            .fillMaxWidth()
                            .widthIn(max = 680.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(14.dp)
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(log.medicineName, style = MaterialTheme.typography.titleMedium)
                                Text(
                                    text = "${log.dosage} • ${fmt.format(Date(log.loggedAtMillis))}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = if (log.finalResponse == ReminderStatus.TAKEN)
                                    MaterialTheme.colorScheme.primaryContainer
                                else MaterialTheme.colorScheme.secondaryContainer
                            ) {
                                Text(
                                    text = if (log.finalResponse == ReminderStatus.TAKEN)
                                        strings.statusTaken else strings.statusSkipped,
                                    style = MaterialTheme.typography.labelSmall,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddMedicationDialog(
            saveError = uiState.saveError,
            onDismiss = {
                showAddDialog = false
                viewModel.clearError()
                onDismissInitialAdd()
            },
            onSave = { name, dosage, freq, times, inventory, threshold, instructions ->
                viewModel.addMedicine(
                    name = name,
                    dosage = dosage,
                    frequency = freq,
                    scheduleTimesRaw = times,
                    inventoryCount = inventory,
                    refillThreshold = threshold,
                    instructions = instructions,
                    onSuccess = {
                        showAddDialog = false
                        onDismissInitialAdd()
                    }
                )
            }
        )
    }
}

@Composable
private fun MedicineCard(
    medicine: Medicine,
    onRefill: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = LocalMediStrings.current
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(medicine.name, style = MaterialTheme.typography.titleMedium)
                    Text(
                        text = buildString {
                            append(medicine.dosage)
                            if (medicine.instructions.isNotBlank()) {
                                append(" • ")
                                append(medicine.instructions)
                            }
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.testTag("delete_medicine_${medicine.id}")
                ) {
                    Icon(
                        imageVector = Icons.Filled.DeleteOutline,
                        contentDescription = strings.deleteLabel,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.horizontalScroll(rememberScrollState())
            ) {
                medicine.scheduleTimes.forEach { time ->
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Schedule,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = time,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
            }

            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Inventory2,
                        contentDescription = null,
                        tint = if (medicine.isLowStock) MaterialTheme.colorScheme.secondary
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "${medicine.inventoryCount} ${strings.stockRemaining}",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = if (medicine.isLowStock) MaterialTheme.colorScheme.secondary
                        else MaterialTheme.colorScheme.onSurface
                    )
                    if (medicine.isLowStock) {
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = MaterialTheme.colorScheme.secondaryContainer
                        ) {
                            Text(
                                text = strings.lowStockBadge,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                TextButton(
                    onClick = onRefill,
                    modifier = Modifier.testTag("refill_medicine_${medicine.id}")
                ) {
                    Text(strings.refillPlus10)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMedicationDialog(
    saveError: String?,
    onDismiss: () -> Unit,
    onSave: (
        name: String,
        dosage: String,
        frequency: MedicationFrequency,
        scheduleTimesRaw: String,
        inventoryCount: Int,
        refillThreshold: Int,
        instructions: String
    ) -> Unit
) {
    val strings = LocalMediStrings.current
    var name by rememberSaveable { mutableStateOf("") }
    var dosage by rememberSaveable { mutableStateOf("") }
    var frequency by rememberSaveable { mutableStateOf(MedicationFrequency.TWICE_DAILY) }
    var timesText by rememberSaveable { mutableStateOf(MedicationFrequency.TWICE_DAILY.defaultTimes.joinToString(", ")) }
    var inventoryText by rememberSaveable { mutableStateOf("30") }
    var thresholdText by rememberSaveable { mutableStateOf("5") }
    var instructions by rememberSaveable { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(strings.addMedicine) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(strings.medicineNameLabel) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_medicine_name")
                )
                OutlinedTextField(
                    value = dosage,
                    onValueChange = { dosage = it },
                    label = { Text(strings.dosageLabel) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_medicine_dosage")
                )
                Text(strings.frequencyLabel, style = MaterialTheme.typography.labelMedium)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.horizontalScroll(rememberScrollState())
                ) {
                    MedicationFrequency.entries.forEach { freq ->
                        val label = when (freq) {
                            MedicationFrequency.ONCE_DAILY -> strings.freqOnceDaily
                            MedicationFrequency.TWICE_DAILY -> strings.freqTwiceDaily
                            MedicationFrequency.THRICE_DAILY -> strings.freqThriceDaily
                            MedicationFrequency.CUSTOM -> strings.freqCustom
                        }
                        FilterChip(
                            selected = frequency == freq,
                            onClick = {
                                frequency = freq
                                if (freq != MedicationFrequency.CUSTOM) {
                                    timesText = freq.defaultTimes.joinToString(", ")
                                }
                            },
                            label = { Text(label) }
                        )
                    }
                }
                OutlinedTextField(
                    value = timesText,
                    onValueChange = { timesText = it },
                    label = { Text(strings.scheduleTimesLabel) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_schedule_times")
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = inventoryText,
                        onValueChange = { inventoryText = it.filter { ch -> ch.isDigit() } },
                        label = { Text(strings.inventoryCountLabel) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("input_inventory_count")
                    )
                    OutlinedTextField(
                        value = thresholdText,
                        onValueChange = { thresholdText = it.filter { ch -> ch.isDigit() } },
                        label = { Text(strings.refillThresholdLabel) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
                OutlinedTextField(
                    value = instructions,
                    onValueChange = { instructions = it },
                    label = { Text(strings.instructionsLabel) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                if (!saveError.isNullOrBlank()) {
                    Text(
                        text = saveError,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        name,
                        dosage,
                        frequency,
                        timesText,
                        inventoryText.toIntOrNull() ?: 30,
                        thresholdText.toIntOrNull() ?: 5,
                        instructions
                    )
                },
                modifier = Modifier.testTag("save_medicine_btn")
            ) {
                Text(strings.saveAndSchedule)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(strings.cancel)
            }
        }
    )
}
