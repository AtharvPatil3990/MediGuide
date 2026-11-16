package com.example.domain.model

import com.example.data.local.entity.MedicationFrequency
import com.example.data.local.entity.ReminderStatus
import com.example.data.local.entity.UserRole

data class UserProfile(
    val id: Long,
    val name: String,
    val role: UserRole,
    val age: Int,
    val bloodGroup: String,
    val preferredLanguageCode: String,
    val biometricEnabled: Boolean
)

data class Medicine(
    val id: Long,
    val userId: Long,
    val name: String,
    val dosage: String,
    val frequency: MedicationFrequency,
    val scheduleTimes: List<String>,
    val inventoryCount: Int,
    val refillThreshold: Int,
    val instructions: String,
    val isLowStock: Boolean
)

data class ScheduledReminderItem(
    val reminderId: Long,
    val medicineId: Long,
    val userId: Long,
    val medicineName: String,
    val dosage: String,
    val instructions: String,
    val scheduledTimeMillis: Long,
    val status: ReminderStatus,
    val responseType: ReminderStatus?,
    val inventoryCount: Int
)

data class AdherenceStats(
    val totalScheduledToday: Int,
    val completedTakenToday: Int,
    val skippedToday: Int,
    val adherencePercent: Int
)

data class AssistantMessage(
    val id: String,
    val isFromUser: Boolean,
    val text: String,
    val timestampMillis: Long = System.currentTimeMillis()
)
