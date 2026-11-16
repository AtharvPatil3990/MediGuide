package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.data.local.entity.UserEntity
import com.example.data.local.entity.UserWithCareModules
import com.example.data.local.entity.UserWithMedicines
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users ORDER BY id ASC")
    fun observeAllUsers(): Flow<List<UserEntity>>

    @Query("SELECT * FROM users WHERE id = :userId")
    fun observeUserById(userId: Long): Flow<UserEntity?>

    @Query("SELECT * FROM users WHERE id = :userId")
    suspend fun getUserById(userId: Long): UserEntity?

    @Query("SELECT COUNT(*) FROM users")
    suspend fun getUserCount(): Int

    @Transaction
    @Query("SELECT * FROM users WHERE id = :userId")
    fun observeUserWithMedicines(userId: Long): Flow<UserWithMedicines?>

    @Transaction
    @Query("SELECT * FROM users WHERE id = :userId")
    fun observeUserWithCareModules(userId: Long): Flow<UserWithCareModules?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity): Long

    @Update
    suspend fun updateUser(user: UserEntity)
}
