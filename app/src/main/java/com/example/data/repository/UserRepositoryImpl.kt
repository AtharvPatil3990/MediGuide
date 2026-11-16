package com.example.data.repository

import com.example.data.local.dao.UserDao
import com.example.data.local.entity.UserEntity
import com.example.data.local.entity.UserRole
import com.example.data.local.preferences.AppPreferencesDataStore
import com.example.domain.model.UserProfile
import com.example.domain.repository.CareModuleRepository
import com.example.domain.repository.UserRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepositoryImpl @Inject constructor(
    private val userDao: UserDao,
    private val preferences: AppPreferencesDataStore,
    private val careModuleRepository: CareModuleRepository
) : UserRepository {

    override fun observeProfiles(): Flow<List<UserProfile>> =
        userDao.observeAllUsers().map { list -> list.map { it.toDomain() } }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun observeActiveProfile(): Flow<UserProfile?> {
        return combine(
            preferences.activeUserIdFlow,
            userDao.observeAllUsers()
        ) { activeId, users ->
            val targetId = activeId ?: users.firstOrNull()?.id
            targetId
        }.flatMapLatest { targetId ->
            if (targetId == null) flowOf(null)
            else userDao.observeUserById(targetId).map { it?.toDomain() }
        }
    }

    override suspend fun ensureDefaultProfilesSeeded() {
        if (userDao.getUserCount() == 0) {
            val primaryId = userDao.insertUser(
                UserEntity(
                    name = "Aarav (Self)",
                    role = UserRole.PRIMARY,
                    age = 32,
                    bloodGroup = "O+"
                )
            )
            val elderId = userDao.insertUser(
                UserEntity(
                    name = "Dadi (Elder)",
                    role = UserRole.ELDER,
                    age = 71,
                    bloodGroup = "B+"
                )
            )
            val motherId = userDao.insertUser(
                UserEntity(
                    name = "Meera (Mother)",
                    role = UserRole.MOTHER,
                    age = 29,
                    bloodGroup = "A+"
                )
            )
            preferences.setActiveUserId(primaryId)
            careModuleRepository.ensureCareDefaultsForUser(primaryId)
            careModuleRepository.ensureCareDefaultsForUser(elderId)
            careModuleRepository.ensureCareDefaultsForUser(motherId)
        }
    }

    override suspend fun setActiveProfile(userId: Long) {
        preferences.setActiveUserId(userId)
        careModuleRepository.ensureCareDefaultsForUser(userId)
    }

    override suspend fun createProfile(
        name: String,
        role: UserRole,
        age: Int,
        bloodGroup: String
    ): Long {
        val newId = userDao.insertUser(
            UserEntity(
                name = name.trim(),
                role = role,
                age = age.coerceIn(0, 120),
                bloodGroup = bloodGroup.trim().ifEmpty { "O+" }
            )
        )
        careModuleRepository.ensureCareDefaultsForUser(newId)
        preferences.setActiveUserId(newId)
        return newId
    }

    private fun UserEntity.toDomain() = UserProfile(
        id = id,
        name = name,
        role = role,
        age = age,
        bloodGroup = bloodGroup,
        preferredLanguageCode = preferredLanguageCode,
        biometricEnabled = biometricEnabled
    )
}
