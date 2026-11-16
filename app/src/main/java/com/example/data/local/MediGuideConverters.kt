package com.example.data.local

import androidx.room.TypeConverter
import com.example.data.local.entity.CareModuleType
import com.example.data.local.entity.MedicationFrequency
import com.example.data.local.entity.ReminderStatus
import com.example.data.local.entity.UserRole

class MediGuideConverters {
    @TypeConverter
    fun fromStringList(value: List<String>): String = value.joinToString(",")

    @TypeConverter
    fun toStringList(value: String): List<String> =
        if (value.isBlank()) emptyList() else value.split(",").map { it.trim() }.filter { it.isNotEmpty() }

    @TypeConverter
    fun fromUserRole(role: UserRole): String = role.name

    @TypeConverter
    fun toUserRole(value: String): UserRole = UserRole.valueOf(value)

    @TypeConverter
    fun fromFrequency(freq: MedicationFrequency): String = freq.name

    @TypeConverter
    fun toFrequency(value: String): MedicationFrequency = MedicationFrequency.valueOf(value)

    @TypeConverter
    fun fromReminderStatus(status: ReminderStatus?): String? = status?.name

    @TypeConverter
    fun toReminderStatus(value: String?): ReminderStatus? =
        value?.let { ReminderStatus.valueOf(it) }

    @TypeConverter
    fun fromCareModuleType(type: CareModuleType): String = type.name

    @TypeConverter
    fun toCareModuleType(value: String): CareModuleType = CareModuleType.valueOf(value)
}
