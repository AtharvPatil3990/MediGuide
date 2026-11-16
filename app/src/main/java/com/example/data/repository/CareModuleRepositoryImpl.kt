package com.example.data.repository

import com.example.data.local.dao.CareModuleDao
import com.example.data.local.entity.BabyMilestoneEntity
import com.example.data.local.entity.CareModuleEntity
import com.example.data.local.entity.CareModuleType
import com.example.data.local.entity.ElderAppointmentEntity
import com.example.data.local.entity.VaccinationEntity
import com.example.domain.repository.CareModuleRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CareModuleRepositoryImpl @Inject constructor(
    private val careModuleDao: CareModuleDao
) : CareModuleRepository {

    override fun observeMotherBabyModule(userId: Long): Flow<CareModuleEntity?> =
        careModuleDao.observeCareModule(userId, CareModuleType.MOTHER_BABY)

    override fun observeBabyMilestones(userId: Long): Flow<List<BabyMilestoneEntity>> =
        careModuleDao.observeMilestones(userId)

    override fun observeVaccinations(userId: Long): Flow<List<VaccinationEntity>> =
        careModuleDao.observeVaccinations(userId)

    override fun observeElderAppointments(userId: Long): Flow<List<ElderAppointmentEntity>> =
        careModuleDao.observeElderAppointments(userId)

    override suspend fun ensureCareDefaultsForUser(userId: Long) {
        if (careModuleDao.getCareModule(userId, CareModuleType.MOTHER_BABY) == null) {
            careModuleDao.upsertCareModule(
                CareModuleEntity(
                    userId = userId,
                    moduleType = CareModuleType.MOTHER_BABY,
                    pregnancyWeek = 24,
                    maternalWeightKg = 62.5f,
                    bloodGroup = "A+"
                )
            )
        }

        if (careModuleDao.observeMilestones(userId).first().isEmpty()) {
            careModuleDao.insertMilestones(
                listOf(
                    BabyMilestoneEntity(
                        userId = userId,
                        title = "Social Smile & Eye Tracking",
                        expectedAgeMonths = 2,
                        achieved = true,
                        achievedAgeMonths = 2
                    ),
                    BabyMilestoneEntity(
                        userId = userId,
                        title = "Steady Head Control",
                        expectedAgeMonths = 4,
                        achieved = false
                    ),
                    BabyMilestoneEntity(
                        userId = userId,
                        title = "Sitting Without Support",
                        expectedAgeMonths = 6,
                        achieved = false
                    ),
                    BabyMilestoneEntity(
                        userId = userId,
                        title = "First Words & Standing",
                        expectedAgeMonths = 12,
                        achieved = false
                    )
                )
            )
        }

        if (careModuleDao.observeVaccinations(userId).first().isEmpty()) {
            careModuleDao.insertVaccinations(
                listOf(
                    VaccinationEntity(
                        userId = userId,
                        vaccineName = "BCG + Oral Polio (OPV-0)",
                        dueDateLabel = "At Birth",
                        completed = true
                    ),
                    VaccinationEntity(
                        userId = userId,
                        vaccineName = "Pentavalent-1 + Rotavirus",
                        dueDateLabel = "6 Weeks",
                        completed = false
                    ),
                    VaccinationEntity(
                        userId = userId,
                        vaccineName = "Pentavalent-2 + IPV",
                        dueDateLabel = "10 Weeks",
                        completed = false
                    ),
                    VaccinationEntity(
                        userId = userId,
                        vaccineName = "MMR-1 (Measles, Mumps, Rubella)",
                        dueDateLabel = "9 Months",
                        completed = false
                    )
                )
            )
        }
    }

    override suspend fun savePregnancyProgress(
        userId: Long,
        week: Int,
        weightKg: Float,
        bloodGroup: String
    ) {
        val existing = careModuleDao.getCareModule(userId, CareModuleType.MOTHER_BABY)
        careModuleDao.upsertCareModule(
            CareModuleEntity(
                id = existing?.id ?: 0L,
                userId = userId,
                moduleType = CareModuleType.MOTHER_BABY,
                pregnancyWeek = week.coerceIn(1, 42),
                maternalWeightKg = weightKg.coerceIn(30f, 180f),
                bloodGroup = bloodGroup.ifBlank { "O+" },
                updatedAtMillis = System.currentTimeMillis()
            )
        )
    }

    override suspend fun toggleMilestoneAchieved(milestone: BabyMilestoneEntity) {
        val next = !milestone.achieved
        careModuleDao.updateMilestone(
            milestone.copy(
                achieved = next,
                achievedAgeMonths = if (next) milestone.expectedAgeMonths else null
            )
        )
    }

    override suspend fun toggleVaccinationCompleted(vaccination: VaccinationEntity) {
        careModuleDao.updateVaccination(vaccination.copy(completed = !vaccination.completed))
    }

    override suspend fun addElderAppointment(
        userId: Long,
        doctorName: String,
        specialty: String,
        dateTimeLabel: String,
        notes: String
    ) {
        if (doctorName.isBlank()) return
        careModuleDao.insertAppointment(
            ElderAppointmentEntity(
                userId = userId,
                doctorName = doctorName.trim(),
                specialty = specialty.trim().ifBlank { "General Physician" },
                dateTimeLabel = dateTimeLabel.trim().ifBlank { "Upcoming" },
                notes = notes.trim()
            )
        )
    }

    override suspend fun toggleAppointmentCompleted(appointment: ElderAppointmentEntity) {
        careModuleDao.updateAppointment(appointment.copy(completed = !appointment.completed))
    }
}
