package com.example.domain.repository

import com.example.core.localization.AppLanguage
import com.example.data.local.entity.BabyMilestoneEntity
import com.example.data.local.entity.CareModuleEntity
import com.example.data.local.entity.DoseLogEntity
import com.example.data.local.entity.ElderAppointmentEntity
import com.example.data.local.entity.MedicationFrequency
import com.example.data.local.entity.ReminderStatus
import com.example.data.local.entity.UserRole
import com.example.data.local.entity.VaccinationEntity
import com.example.domain.model.Medicine
import com.example.domain.model.ScheduledReminderItem
import com.example.domain.model.UserProfile
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    fun observeProfiles(): Flow<List<UserProfile>>
    fun observeActiveProfile(): Flow<UserProfile?>
    suspend fun ensureDefaultProfilesSeeded()
    suspend fun setActiveProfile(userId: Long)
    suspend fun createProfile(
        name: String,
        role: UserRole,
        age: Int,
        bloodGroup: String
    ): Long
}

interface MedicationRepository {
    fun observeMedicinesForUser(userId: Long): Flow<List<Medicine>>
    fun observeLowInventoryMedicines(userId: Long): Flow<List<Medicine>>
    fun observeTodayReminders(userId: Long): Flow<List<ScheduledReminderItem>>
    fun observeDoseLogs(userId: Long): Flow<List<DoseLogEntity>>
    suspend fun addMedicineAndScheduleReminders(
        userId: Long,
        name: String,
        dosage: String,
        frequency: MedicationFrequency,
        scheduleTimes: List<String>,
        inventoryCount: Int,
        refillThreshold: Int,
        instructions: String,
        reminderTimestampsMillis: List<Long>
    ): Long
    suspend fun refillMedicine(medicineId: Long, addedUnits: Int)
    suspend fun deleteMedicine(medicineId: Long)
    suspend fun markReminderSent(reminderId: Long)
    suspend fun recordUserDoseResponse(reminderId: Long, response: ReminderStatus)
    suspend fun checkLowInventoryForAlerts(): List<Medicine>
    suspend fun reschedulePendingAlarms()
}

interface CareModuleRepository {
    fun observeMotherBabyModule(userId: Long): Flow<CareModuleEntity?>
    fun observeBabyMilestones(userId: Long): Flow<List<BabyMilestoneEntity>>
    fun observeVaccinations(userId: Long): Flow<List<VaccinationEntity>>
    fun observeElderAppointments(userId: Long): Flow<List<ElderAppointmentEntity>>
    suspend fun ensureCareDefaultsForUser(userId: Long)
    suspend fun savePregnancyProgress(
        userId: Long,
        week: Int,
        weightKg: Float,
        bloodGroup: String
    )
    suspend fun toggleMilestoneAchieved(milestone: BabyMilestoneEntity)
    suspend fun toggleVaccinationCompleted(vaccination: VaccinationEntity)
    suspend fun addElderAppointment(
        userId: Long,
        doctorName: String,
        specialty: String,
        dateTimeLabel: String,
        notes: String
    )
    suspend fun toggleAppointmentCompleted(appointment: ElderAppointmentEntity)
}

interface HealthAssistantRepository {
    suspend fun generateReply(
        userId: Long,
        query: String,
        language: AppLanguage
    ): String
}
