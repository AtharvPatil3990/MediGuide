package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

enum class CareModuleType {
    MOTHER_BABY,
    ELDER_CARE
}

@Entity(
    tableName = "care_modules",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("userId")]
)
data class CareModuleEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long,
    val moduleType: CareModuleType,
    val pregnancyWeek: Int = 24,
    val maternalWeightKg: Float = 62.5f,
    val bloodGroup: String = "O+",
    val notes: String = "",
    val updatedAtMillis: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "baby_milestones",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("userId")]
)
data class BabyMilestoneEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long,
    val title: String,
    val expectedAgeMonths: Int,
    val achieved: Boolean = false,
    val achievedAgeMonths: Int? = null
)

@Entity(
    tableName = "vaccinations",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("userId")]
)
data class VaccinationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long,
    val vaccineName: String,
    val dueDateLabel: String,
    val completed: Boolean = false
)

@Entity(
    tableName = "elder_appointments",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("userId")]
)
data class ElderAppointmentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long,
    val doctorName: String,
    val specialty: String,
    val dateTimeLabel: String,
    val notes: String = "",
    val completed: Boolean = false
)
