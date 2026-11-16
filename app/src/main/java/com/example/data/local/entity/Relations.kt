package com.example.data.local.entity

import androidx.room.Embedded
import androidx.room.Relation

data class UserWithMedicines(
    @Embedded val user: UserEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "userId"
    )
    val medicines: List<MedicineEntity>
)

data class MedicineWithReminders(
    @Embedded val medicine: MedicineEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "medicineId"
    )
    val reminders: List<ReminderEntity>
)

data class UserWithCareModules(
    @Embedded val user: UserEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "userId"
    )
    val careModules: List<CareModuleEntity>
)

data class ReminderWithMedicine(
    @Embedded val reminder: ReminderEntity,
    @Relation(
        parentColumn = "medicineId",
        entityColumn = "id"
    )
    val medicine: MedicineEntity
)
