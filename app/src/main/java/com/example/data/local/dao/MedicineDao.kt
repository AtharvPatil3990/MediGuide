package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.data.local.entity.MedicineEntity
import com.example.data.local.entity.MedicineWithReminders
import kotlinx.coroutines.flow.Flow

@Dao
interface MedicineDao {
    @Query("SELECT * FROM medicines WHERE userId = :userId AND isActive = 1 ORDER BY createdAtMillis DESC")
    fun observeMedicinesForUser(userId: Long): Flow<List<MedicineEntity>>

    @Transaction
    @Query("SELECT * FROM medicines WHERE userId = :userId AND isActive = 1 ORDER BY createdAtMillis DESC")
    fun observeMedicinesWithReminders(userId: Long): Flow<List<MedicineWithReminders>>

    @Query("SELECT * FROM medicines WHERE id = :medicineId")
    suspend fun getMedicineById(medicineId: Long): MedicineEntity?

    @Query("SELECT * FROM medicines WHERE isActive = 1 AND inventoryCount <= refillThreshold")
    suspend fun getLowInventoryMedicines(): List<MedicineEntity>

    @Query("SELECT * FROM medicines WHERE userId = :userId AND isActive = 1 AND inventoryCount <= refillThreshold")
    fun observeLowInventoryForUser(userId: Long): Flow<List<MedicineEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedicine(medicine: MedicineEntity): Long

    @Update
    suspend fun updateMedicine(medicine: MedicineEntity)

    @Query("UPDATE medicines SET inventoryCount = MAX(0, inventoryCount - 1) WHERE id = :medicineId")
    suspend fun decrementInventory(medicineId: Long)

    @Query("UPDATE medicines SET inventoryCount = inventoryCount + :delta WHERE id = :medicineId")
    suspend fun addInventory(medicineId: Long, delta: Int)

    @Query("DELETE FROM medicines WHERE id = :medicineId")
    suspend fun deleteMedicineById(medicineId: Long)
}
