package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.BabyMilestoneEntity
import com.example.data.local.entity.CareModuleEntity
import com.example.data.local.entity.CareModuleType
import com.example.data.local.entity.ElderAppointmentEntity
import com.example.data.local.entity.VaccinationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CareModuleDao {
    @Query("SELECT * FROM care_modules WHERE userId = :userId AND moduleType = :type LIMIT 1")
    fun observeCareModule(userId: Long, type: CareModuleType): Flow<CareModuleEntity?>

    @Query("SELECT * FROM care_modules WHERE userId = :userId AND moduleType = :type LIMIT 1")
    suspend fun getCareModule(userId: Long, type: CareModuleType): CareModuleEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertCareModule(module: CareModuleEntity): Long

    @Query("SELECT * FROM baby_milestones WHERE userId = :userId ORDER BY expectedAgeMonths ASC")
    fun observeMilestones(userId: Long): Flow<List<BabyMilestoneEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMilestones(items: List<BabyMilestoneEntity>)

    @Update
    suspend fun updateMilestone(item: BabyMilestoneEntity)

    @Query("SELECT * FROM vaccinations WHERE userId = :userId ORDER BY id ASC")
    fun observeVaccinations(userId: Long): Flow<List<VaccinationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVaccinations(items: List<VaccinationEntity>)

    @Update
    suspend fun updateVaccination(item: VaccinationEntity)

    @Query("SELECT * FROM elder_appointments WHERE userId = :userId ORDER BY completed ASC, id DESC")
    fun observeElderAppointments(userId: Long): Flow<List<ElderAppointmentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAppointment(appointment: ElderAppointmentEntity): Long

    @Update
    suspend fun updateAppointment(appointment: ElderAppointmentEntity)

    @Query("DELETE FROM elder_appointments WHERE id = :id")
    suspend fun deleteAppointmentById(id: Long)
}
