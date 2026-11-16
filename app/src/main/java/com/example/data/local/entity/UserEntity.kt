package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class UserRole {
    PRIMARY,
    ELDER,
    CHILD,
    MOTHER
}

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val role: UserRole,
    val age: Int,
    val bloodGroup: String,
    val preferredLanguageCode: String = "en",
    val biometricEnabled: Boolean = false,
    val createdAtMillis: Long = System.currentTimeMillis()
)
