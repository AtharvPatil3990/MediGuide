package com.example.domain.usecase

import com.example.data.local.entity.ReminderStatus
import com.example.domain.repository.MedicationRepository
import javax.inject.Inject

/**
 * Enforces the strict Dose Tracking state machine:
 * Scheduled -> Reminder Sent -> User Response (Taken/Skipped) -> Completed
 */
class ProcessDoseStateTransitionUseCase @Inject constructor(
    private val medicationRepository: MedicationRepository
) {
    suspend fun triggerReminderSent(reminderId: Long) {
        medicationRepository.markReminderSent(reminderId)
    }

    suspend fun markTaken(reminderId: Long) {
        medicationRepository.recordUserDoseResponse(reminderId, ReminderStatus.TAKEN)
    }

    suspend fun markSkipped(reminderId: Long) {
        medicationRepository.recordUserDoseResponse(reminderId, ReminderStatus.SKIPPED)
    }
}
