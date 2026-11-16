package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.data.local.entity.DoseLogEntity
import com.example.data.local.entity.ReminderEntity
import com.example.data.local.entity.ReminderStatus
import com.example.data.local.entity.ReminderWithMedicine
import kotlinx.coroutines.flow.Flow

@Dao
interface ReminderDao {
    @Transaction
    @Query(
        """
        SELECT * FROM reminders
        WHERE userId = :userId AND scheduledTimeMillis BETWEEN :startMillis AND :endMillis
        ORDER BY scheduledTimeMillis ASC
        """
    )
    fun observeRemindersForWindow(
        userId: Long,
        startMillis: Long,
        endMillis: Long
    ): Flow<List<ReminderWithMedicine>>

    @Transaction
    @Query("SELECT * FROM reminders WHERE userId = :userId ORDER BY scheduledTimeMillis ASC")
    fun observeAllRemindersForUser(userId: Long): Flow<List<ReminderWithMedicine>>

    @Query("SELECT * FROM reminders WHERE id = :reminderId")
    suspend fun getReminderById(reminderId: Long): ReminderEntity?

    @Transaction
    @Query("SELECT * FROM reminders WHERE id = :reminderId")
    suspend fun getReminderWithMedicine(reminderId: Long): ReminderWithMedicine?

    @Query("SELECT * FROM reminders WHERE status = :status AND scheduledTimeMillis > :afterMillis")
    suspend fun getRemindersByStatusAfter(
        status: ReminderStatus,
        afterMillis: Long
    ): List<ReminderEntity>

    @Query("SELECT * FROM reminders WHERE medicineId = :medicineId")
    suspend fun getRemindersForMedicine(medicineId: Long): List<ReminderEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminder(reminder: ReminderEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminders(reminders: List<ReminderEntity>): List<Long>

    @Update
    suspend fun updateReminder(reminder: ReminderEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDoseLog(log: DoseLogEntity): Long

    @Query("SELECT * FROM dose_logs WHERE userId = :userId ORDER BY loggedAtMillis DESC LIMIT 50")
    fun observeDoseLogsForUser(userId: Long): Flow<List<DoseLogEntity>>
}
