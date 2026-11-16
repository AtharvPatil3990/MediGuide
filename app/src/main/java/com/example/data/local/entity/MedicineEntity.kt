package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

enum class MedicationFrequency(val defaultTimes: List<String>) {
    ONCE_DAILY(listOf("08:00")),
    TWICE_DAILY(listOf("08:00", "20:00")),
    THRICE_DAILY(listOf("08:00", "14:00", "20:00")),
    CUSTOM(listOf("09:00"))
}

@Entity(
    tableName = "medicines",
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
data class MedicineEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long,
    val name: String,
    val dosage: String,
    val frequency: MedicationFrequency,
    val scheduleTimes: List<String>,
    val inventoryCount: Int,
    val refillThreshold: Int = 5,
    val instructions: String = "",
    val isActive: Boolean = true,
    val createdAtMillis: Long = System.currentTimeMillis()
)
