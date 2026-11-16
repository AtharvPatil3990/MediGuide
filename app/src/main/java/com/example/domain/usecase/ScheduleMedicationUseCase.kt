package com.example.domain.usecase

import com.example.data.local.entity.MedicationFrequency
import com.example.domain.repository.MedicationRepository
import java.util.Calendar
import javax.inject.Inject

/**
 * Custom scheduling logic that parses HH:mm schedule times (or frequency defaults),
 * computes upcoming trigger timestamps for today and tomorrow, persists the Medicine
 * and Reminder entities in Room, and schedules exact alarms via AlarmManager.
 */
class ScheduleMedicationUseCase @Inject constructor(
    private val medicationRepository: MedicationRepository
) {
    suspend operator fun invoke(
        userId: Long,
        name: String,
        dosage: String,
        frequency: MedicationFrequency,
        rawTimesInput: String,
        inventoryCount: Int,
        refillThreshold: Int,
        instructions: String
    ): Result<Long> {
        val trimmedName = name.trim()
        val trimmedDosage = dosage.trim()
        if (trimmedName.isEmpty() || trimmedDosage.isEmpty()) {
            return Result.failure(IllegalArgumentException("Name and dosage are required"))
        }

        val parsedTimes = parseScheduleTimes(frequency, rawTimesInput)
        val reminderTimestamps = generateUpcomingTriggerTimestamps(parsedTimes)

        val medicineId = medicationRepository.addMedicineAndScheduleReminders(
            userId = userId,
            name = trimmedName,
            dosage = trimmedDosage,
            frequency = frequency,
            scheduleTimes = parsedTimes,
            inventoryCount = inventoryCount.coerceAtLeast(0),
            refillThreshold = refillThreshold.coerceAtLeast(1),
            instructions = instructions.trim(),
            reminderTimestampsMillis = reminderTimestamps
        )
        return Result.success(medicineId)
    }

    fun parseScheduleTimes(frequency: MedicationFrequency, rawInput: String): List<String> {
        val candidates = rawInput.split(",")
            .map { it.trim() }
            .filter { timeRegex.matches(it) }
            .distinct()
            .sorted()

        return if (candidates.isNotEmpty()) candidates else frequency.defaultTimes
    }

    fun generateUpcomingTriggerTimestamps(
        times: List<String>,
        nowMillis: Long = System.currentTimeMillis()
    ): List<Long> {
        val timestamps = mutableListOf<Long>()
        val baseCal = Calendar.getInstance().apply { timeInMillis = nowMillis }

        times.forEach { timeStr ->
            val parts = timeStr.split(":")
            val hour = parts[0].toInt().coerceIn(0, 23)
            val minute = parts[1].toInt().coerceIn(0, 59)

            // Today's slot
            val todayCal = (baseCal.clone() as Calendar).apply {
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, minute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            if (todayCal.timeInMillis > nowMillis + 15_000L) {
                timestamps.add(todayCal.timeInMillis)
            } else {
                // Schedule today's slot immediately actionable (+1 min) if within today, plus tomorrow's exact slot
                val soonCal = (baseCal.clone() as Calendar).apply {
                    add(Calendar.MINUTE, 1)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                timestamps.add(soonCal.timeInMillis)
            }

            // Next day's slot so tomorrow's alarm is already armed offline
            val tomorrowCal = (baseCal.clone() as Calendar).apply {
                add(Calendar.DAY_OF_YEAR, 1)
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, minute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            timestamps.add(tomorrowCal.timeInMillis)
        }
        return timestamps.sorted()
    }

    private companion object {
        val timeRegex = Regex("^([01]?\\d|2[0-3]):[0-5]\\d$")
    }
}
