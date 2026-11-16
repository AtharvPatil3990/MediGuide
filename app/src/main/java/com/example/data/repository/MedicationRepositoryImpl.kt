package com.example.data.repository

import com.example.data.local.dao.MedicineDao
import com.example.data.local.dao.ReminderDao
import com.example.data.local.entity.DoseLogEntity
import com.example.data.local.entity.MedicationFrequency
import com.example.data.local.entity.MedicineEntity
import com.example.data.local.entity.ReminderEntity
import com.example.data.local.entity.ReminderStatus
import com.example.domain.model.Medicine
import com.example.domain.model.ScheduledReminderItem
import com.example.domain.repository.MedicationRepository
import com.example.notifications.AlarmScheduler
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MedicationRepositoryImpl @Inject constructor(
    private val medicineDao: MedicineDao,
    private val reminderDao: ReminderDao,
    private val alarmScheduler: AlarmScheduler
) : MedicationRepository {

    override fun observeMedicinesForUser(userId: Long): Flow<List<Medicine>> =
        medicineDao.observeMedicinesForUser(userId).map { entities ->
            entities.map { it.toDomain() }
        }

    override fun observeLowInventoryMedicines(userId: Long): Flow<List<Medicine>> =
        medicineDao.observeLowInventoryForUser(userId).map { entities ->
            entities.map { it.toDomain() }
        }

    override fun observeTodayReminders(userId: Long): Flow<List<ScheduledReminderItem>> {
        val (startOfDay, endOfDay) = todayWindowMillis()
        return reminderDao.observeRemindersForWindow(userId, startOfDay, endOfDay).map { list ->
            list.map { item ->
                ScheduledReminderItem(
                    reminderId = item.reminder.id,
                    medicineId = item.medicine.id,
                    userId = item.reminder.userId,
                    medicineName = item.medicine.name,
                    dosage = item.medicine.dosage,
                    instructions = item.medicine.instructions,
                    scheduledTimeMillis = item.reminder.scheduledTimeMillis,
                    status = item.reminder.status,
                    responseType = item.reminder.responseType,
                    inventoryCount = item.medicine.inventoryCount
                )
            }
        }
    }

    override fun observeDoseLogs(userId: Long): Flow<List<DoseLogEntity>> =
        reminderDao.observeDoseLogsForUser(userId)

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
    ): Long {
        val medicineId = medicineDao.insertMedicine(
            MedicineEntity(
                userId = userId,
                name = name,
                dosage = dosage,
                frequency = frequency,
                scheduleTimes = scheduleTimes,
                inventoryCount = inventoryCount,
                refillThreshold = refillThreshold,
                instructions = instructions
            )
        )

        val reminders = reminderTimestampsMillis.map { triggerAt ->
            ReminderEntity(
                medicineId = medicineId,
                userId = userId,
                scheduledTimeMillis = triggerAt,
                status = ReminderStatus.SCHEDULED
            )
        }
        val insertedIds = reminderDao.insertReminders(reminders)

        insertedIds.zip(reminderTimestampsMillis).forEach { (reminderId, triggerAt) ->
            alarmScheduler.scheduleReminderAlarm(
                reminderId = reminderId,
                triggerAtMillis = triggerAt,
                medicineName = name,
                dosage = dosage
            )
        }
        return medicineId
    }

    override suspend fun refillMedicine(medicineId: Long, addedUnits: Int) {
        medicineDao.addInventory(medicineId, addedUnits)
    }

    override suspend fun deleteMedicine(medicineId: Long) {
        val reminders = reminderDao.getRemindersForMedicine(medicineId)
        reminders.forEach { alarmScheduler.cancelReminderAlarm(it.id) }
        medicineDao.deleteMedicineById(medicineId)
    }

    override suspend fun markReminderSent(reminderId: Long) {
        val existing = reminderDao.getReminderById(reminderId) ?: return
        if (existing.status.canTransitionTo(ReminderStatus.REMINDER_SENT)) {
            reminderDao.updateReminder(existing.copy(status = ReminderStatus.REMINDER_SENT))
        }
    }

    /**
     * Strict state machine transition:
     * Scheduled/Reminder Sent -> User Response (Taken/Skipped) -> Completed
     */
    override suspend fun recordUserDoseResponse(reminderId: Long, response: ReminderStatus) {
        if (response != ReminderStatus.TAKEN && response != ReminderStatus.SKIPPED) return
        val reminderWithMedicine = reminderDao.getReminderWithMedicine(reminderId) ?: return
        val current = reminderWithMedicine.reminder
        val medicine = reminderWithMedicine.medicine

        if (!current.status.canTransitionTo(response)) return

        val now = System.currentTimeMillis()
        // Step 1: User Response (TAKEN or SKIPPED)
        val afterResponse = current.copy(
            status = response,
            responseType = response,
            respondedAtMillis = now
        )
        reminderDao.updateReminder(afterResponse)

        // Step 2: Transition to COMPLETED and record adherence log
        if (afterResponse.status.canTransitionTo(ReminderStatus.COMPLETED)) {
            reminderDao.updateReminder(
                afterResponse.copy(
                    status = ReminderStatus.COMPLETED,
                    completedAtMillis = now
                )
            )
        }

        if (response == ReminderStatus.TAKEN) {
            medicineDao.decrementInventory(medicine.id)
        }

        alarmScheduler.cancelReminderAlarm(reminderId)

        reminderDao.insertDoseLog(
            DoseLogEntity(
                reminderId = reminderId,
                medicineId = medicine.id,
                userId = current.userId,
                medicineName = medicine.name,
                dosage = medicine.dosage,
                finalResponse = response,
                scheduledTimeMillis = current.scheduledTimeMillis,
                loggedAtMillis = now
            )
        )
    }

    override suspend fun checkLowInventoryForAlerts(): List<Medicine> {
        return medicineDao.getLowInventoryMedicines().map { it.toDomain() }
    }

    override suspend fun reschedulePendingAlarms() {
        val now = System.currentTimeMillis()
        val pending = reminderDao.getRemindersByStatusAfter(ReminderStatus.SCHEDULED, now)
        pending.forEach { reminder ->
            val med = medicineDao.getMedicineById(reminder.medicineId) ?: return@forEach
            alarmScheduler.scheduleReminderAlarm(
                reminderId = reminder.id,
                triggerAtMillis = reminder.scheduledTimeMillis,
                medicineName = med.name,
                dosage = med.dosage
            )
        }
    }

    private fun todayWindowMillis(): Pair<Long, Long> {
        val start = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        val end = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }.timeInMillis
        return start to end
    }

    private fun MedicineEntity.toDomain() = Medicine(
        id = id,
        userId = userId,
        name = name,
        dosage = dosage,
        frequency = frequency,
        scheduleTimes = scheduleTimes,
        inventoryCount = inventoryCount,
        refillThreshold = refillThreshold,
        instructions = instructions,
        isLowStock = inventoryCount <= refillThreshold
    )
}
