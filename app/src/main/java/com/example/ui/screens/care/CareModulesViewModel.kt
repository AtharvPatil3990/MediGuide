package com.example.ui.screens.care

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.entity.BabyMilestoneEntity
import com.example.data.local.entity.CareModuleEntity
import com.example.data.local.entity.ElderAppointmentEntity
import com.example.data.local.entity.VaccinationEntity
import com.example.domain.model.Medicine
import com.example.domain.repository.CareModuleRepository
import com.example.domain.repository.MedicationRepository
import com.example.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CareModulesUiState(
    val activeUserId: Long? = null,
    val motherBabyModule: CareModuleEntity? = null,
    val milestones: List<BabyMilestoneEntity> = emptyList(),
    val vaccinations: List<VaccinationEntity> = emptyList(),
    val elderAppointments: List<ElderAppointmentEntity> = emptyList(),
    val targetedMedicines: List<Medicine> = emptyList()
)

@HiltViewModel
class CareModulesViewModel @Inject constructor(
    userRepository: UserRepository,
    private val careModuleRepository: CareModuleRepository,
    private val medicationRepository: MedicationRepository
) : ViewModel() {

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<CareModulesUiState> = userRepository.observeActiveProfile()
        .flatMapLatest { profile ->
            if (profile == null) {
                flowOf(CareModulesUiState())
            } else {
                combine(
                    careModuleRepository.observeMotherBabyModule(profile.id),
                    careModuleRepository.observeBabyMilestones(profile.id),
                    careModuleRepository.observeVaccinations(profile.id),
                    careModuleRepository.observeElderAppointments(profile.id),
                    medicationRepository.observeMedicinesForUser(profile.id)
                ) { mother, milestones, vaccines, appointments, medicines ->
                    CareModulesUiState(
                        activeUserId = profile.id,
                        motherBabyModule = mother,
                        milestones = milestones,
                        vaccinations = vaccines,
                        elderAppointments = appointments,
                        targetedMedicines = medicines
                    )
                }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = CareModulesUiState()
        )

    fun savePregnancyProgress(week: Int, weightKg: Float, bloodGroup: String) {
        val userId = uiState.value.activeUserId ?: return
        viewModelScope.launch {
            careModuleRepository.savePregnancyProgress(userId, week, weightKg, bloodGroup)
        }
    }

    fun toggleMilestone(milestone: BabyMilestoneEntity) {
        viewModelScope.launch {
            careModuleRepository.toggleMilestoneAchieved(milestone)
        }
    }

    fun toggleVaccination(vaccination: VaccinationEntity) {
        viewModelScope.launch {
            careModuleRepository.toggleVaccinationCompleted(vaccination)
        }
    }

    fun addElderAppointment(
        doctorName: String,
        specialty: String,
        dateTimeLabel: String,
        notes: String
    ) {
        val userId = uiState.value.activeUserId ?: return
        viewModelScope.launch {
            careModuleRepository.addElderAppointment(
                userId = userId,
                doctorName = doctorName,
                specialty = specialty,
                dateTimeLabel = dateTimeLabel,
                notes = notes
            )
        }
    }

    fun toggleAppointmentCompleted(appointment: ElderAppointmentEntity) {
        viewModelScope.launch {
            careModuleRepository.toggleAppointmentCompleted(appointment)
        }
    }
}
