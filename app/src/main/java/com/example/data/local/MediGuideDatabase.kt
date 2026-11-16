package com.example.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.data.local.dao.CareModuleDao
import com.example.data.local.dao.MedicineDao
import com.example.data.local.dao.ReminderDao
import com.example.data.local.dao.UserDao
import com.example.data.local.entity.BabyMilestoneEntity
import com.example.data.local.entity.CareModuleEntity
import com.example.data.local.entity.DoseLogEntity
import com.example.data.local.entity.ElderAppointmentEntity
import com.example.data.local.entity.MedicineEntity
import com.example.data.local.entity.ReminderEntity
import com.example.data.local.entity.UserEntity
import com.example.data.local.entity.VaccinationEntity

@Database(
    entities = [
        UserEntity::class,
        MedicineEntity::class,
        ReminderEntity::class,
        DoseLogEntity::class,
        CareModuleEntity::class,
        BabyMilestoneEntity::class,
        VaccinationEntity::class,
        ElderAppointmentEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(MediGuideConverters::class)
abstract class MediGuideDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun medicineDao(): MedicineDao
    abstract fun reminderDao(): ReminderDao
    abstract fun careModuleDao(): CareModuleDao
}
