package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Strict Dose Tracking State Machine:
 * SCHEDULED -> REMINDER_SENT -> (TAKEN | SKIPPED) -> COMPLETED
 */
enum class ReminderStatus {
    SCHEDULED,
    REMINDER_SENT,
    TAKEN,
    SKIPPED,
    COMPLETED;

    fun canTransitionTo(next: ReminderStatus): Boolean = when (this) {
        SCHEDULED -> next == REMINDER_SENT || next == TAKEN || next == SKIPPED
        REMINDER_SENT -> next == TAKEN || next == SKIPPED
        TAKEN, SKIPPED -> next == COMPLETED
        COMPLETED -> false
    }
}

@Entity(
    tableName = "reminders",
    foreignKeys = [
        ForeignKey(
            entity = MedicineEntity::class,
            parentColumns = ["id"],
            childColumns = ["medicineId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("medicineId"), Index("userId"), Index("scheduledTimeMillis")]
)
data class ReminderEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val medicineId: Long,
    val userId: Long,
    val scheduledTimeMillis: Long,
    val status: ReminderStatus = ReminderStatus.SCHEDULED,
    val responseType: ReminderStatus? = null,
    val respondedAtMillis: Long? = null,
    val completedAtMillis: Long? = null
)

@Entity(
    tableName = "dose_logs",
    foreignKeys = [
        ForeignKey(
            entity = MedicineEntity::class,
            parentColumns = ["id"],
            childColumns = ["medicineId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("medicineId"), Index("userId"), Index("loggedAtMillis")]
)
data class DoseLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val reminderId: Long,
    val medicineId: Long,
    val userId: Long,
    val medicineName: String,
    val dosage: String,
    val finalResponse: ReminderStatus,
    val scheduledTimeMillis: Long,
    val loggedAtMillis: Long = System.currentTimeMillis()
)
