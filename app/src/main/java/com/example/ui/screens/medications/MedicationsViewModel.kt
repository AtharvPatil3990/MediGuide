package com.example.ui.screens.medications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.entity.DoseLogEntity
import com.example.data.local.entity.MedicationFrequency
import com.example.domain.model.Medicine
import com.example.domain.repository.MedicationRepository
import com.example.domain.repository.UserRepository
import com.example.domain.usecase.ScheduleMedicationUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MedicationsUiState(
    val activeUserId: Long? = null,
    val medicines: List<Medicine> = emptyList(),
    val doseLogs: List<DoseLogEntity> = emptyList(),
    val saveError: String? = null
)

@HiltViewModel
class MedicationsViewModel @Inject constructor(
    userRepository: UserRepository,
    private val medicationRepository: MedicationRepository,
    private val scheduleMedicationUseCase: ScheduleMedicationUseCase
) : ViewModel() {

    private val _saveError = MutableStateFlow<String?>(null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<MedicationsUiState> = userRepository.observeActiveProfile()
        .flatMapLatest { profile ->
            if (profile == null) {
                flowOf(MedicationsUiState())
            } else {
                combine(
                    medicationRepository.observeMedicinesForUser(profile.id),
                    medicationRepository.observeDoseLogs(profile.id),
                    _saveError
                ) { meds, logs, error ->
                    MedicationsUiState(
                        activeUserId = profile.id,
                        medicines = meds,
                        doseLogs = logs,
                        saveError = error
                    )
                }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = MedicationsUiState()
        )

    fun addMedicine(
        name: String,
        dosage: String,
        frequency: MedicationFrequency,
        scheduleTimesRaw: String,
        inventoryCount: Int,
        refillThreshold: Int,
        instructions: String,
        onSuccess: () -> Unit
    ) {
        val userId = uiState.value.activeUserId ?: return
        viewModelScope.launch {
            val result = scheduleMedicationUseCase(
                userId = userId,
                name = name,
                dosage = dosage,
                frequency = frequency,
                rawTimesInput = scheduleTimesRaw,
                inventoryCount = inventoryCount,
                refillThreshold = refillThreshold,
                instructions = instructions
            )
            result.fold(
                onSuccess = {
                    _saveError.value = null
                    onSuccess()
                },
                onFailure = { err ->
                    _saveError.value = err.message
                }
            )
        }
    }

    fun refillStock(medicineId: Long, addedUnits: Int = 10) {
        viewModelScope.launch {
            medicationRepository.refillMedicine(medicineId, addedUnits)
        }
    }

    fun deleteMedicine(medicineId: Long) {
        viewModelScope.launch {
            medicationRepository.deleteMedicine(medicineId)
        }
    }

    fun clearError() {
        _saveError.value = null
    }
}
