package com.example

import com.example.core.localization.AppLanguage
import com.example.core.localization.stringsFor
import com.example.data.local.entity.MedicationFrequency
import com.example.data.local.entity.ReminderStatus
import com.example.domain.usecase.ScheduleMedicationUseCase
import com.example.domain.repository.MedicationRepository
import com.example.data.local.entity.DoseLogEntity
import com.example.domain.model.Medicine
import com.example.domain.model.ScheduledReminderItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MediGuideLogicTest {

    @Test
    fun reminderStatus_enforcesStrictLifecycleStateMachine() {
        assertTrue(ReminderStatus.SCHEDULED.canTransitionTo(ReminderStatus.REMINDER_SENT))
        assertTrue(ReminderStatus.REMINDER_SENT.canTransitionTo(ReminderStatus.TAKEN))
        assertTrue(ReminderStatus.REMINDER_SENT.canTransitionTo(ReminderStatus.SKIPPED))
        assertTrue(ReminderStatus.TAKEN.canTransitionTo(ReminderStatus.COMPLETED))
        assertTrue(ReminderStatus.SKIPPED.canTransitionTo(ReminderStatus.COMPLETED))

        assertFalse(ReminderStatus.SCHEDULED.canTransitionTo(ReminderStatus.COMPLETED))
        assertFalse(ReminderStatus.COMPLETED.canTransitionTo(ReminderStatus.SCHEDULED))
        assertFalse(ReminderStatus.COMPLETED.canTransitionTo(ReminderStatus.TAKEN))
    }

    @Test
    fun scheduleUseCase_parsesCustomTimesAndFallsBackToFrequencyDefaults() {
        val useCase = ScheduleMedicationUseCase(FakeMedicationRepo())

        val parsedCustom = useCase.parseScheduleTimes(
            MedicationFrequency.CUSTOM,
            "21:30, 07:15, invalid"
        )
        assertEquals(listOf("07:15", "21:30"), parsedCustom)

        val fallback = useCase.parseScheduleTimes(
            MedicationFrequency.TWICE_DAILY,
            ""
        )
        assertEquals(listOf("08:00", "20:00"), fallback)

        val triggers = useCase.generateUpcomingTriggerTimestamps(parsedCustom, nowMillis = 1_700_000_000_000L)
        assertEquals(4, triggers.size) // 2 today + 2 tomorrow
    }

    @Test
    fun localizedStrings_provideEnglishHindiAndMarathi() {
        val en = stringsFor(AppLanguage.ENGLISH)
        val hi = stringsFor(AppLanguage.HINDI)
        val mr = stringsFor(AppLanguage.MARATHI)

        assertEquals("MediGuide", en.appName)
        assertEquals("मेडीगाइड", hi.appName)
        assertEquals("मेडीगाइड", mr.appName)
        assertTrue(hi.navMedicines.isNotBlank())
        assertTrue(mr.navMedicines.isNotBlank())
    }

    private class FakeMedicationRepo : MedicationRepository {
        override fun observeMedicinesForUser(userId: Long): Flow<List<Medicine>> = flowOf(emptyList())
        override fun observeLowInventoryMedicines(userId: Long): Flow<List<Medicine>> = flowOf(emptyList())
        override fun observeTodayReminders(userId: Long): Flow<List<ScheduledReminderItem>> = flowOf(emptyList())
        override fun observeDoseLogs(userId: Long): Flow<List<DoseLogEntity>> = flowOf(emptyList())
        override suspend fun addMedicineAndScheduleReminders(
            userId: Long,
            name: String,
            dosage: String,
            frequency: MedicationFrequency,
            scheduleTimes: List<String>,
            inventoryCount: Int,
            refillThreshold: Int,
            instructions: String,
            reminderTimestampsMillis: List<Long>
        ): Long = 1L
        override suspend fun refillMedicine(medicineId: Long, addedUnits: Int) = Unit
        override suspend fun deleteMedicine(medicineId: Long) = Unit
        override suspend fun markReminderSent(reminderId: Long) = Unit
        override suspend fun recordUserDoseResponse(reminderId: Long, response: ReminderStatus) = Unit
        override suspend fun checkLowInventoryForAlerts(): List<Medicine> = emptyList()
        override suspend fun reschedulePendingAlarms() = Unit
    }
}
