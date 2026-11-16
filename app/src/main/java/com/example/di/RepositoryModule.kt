package com.example.di

import com.example.data.repository.CareModuleRepositoryImpl
import com.example.data.repository.LocalHealthAssistantRepositoryImpl
import com.example.data.repository.MedicationRepositoryImpl
import com.example.data.repository.UserRepositoryImpl
import com.example.domain.repository.CareModuleRepository
import com.example.domain.repository.HealthAssistantRepository
import com.example.domain.repository.MedicationRepository
import com.example.domain.repository.UserRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindUserRepository(impl: UserRepositoryImpl): UserRepository

    @Binds
    @Singleton
    abstract fun bindMedicationRepository(impl: MedicationRepositoryImpl): MedicationRepository

    @Binds
    @Singleton
    abstract fun bindCareModuleRepository(impl: CareModuleRepositoryImpl): CareModuleRepository

    @Binds
    @Singleton
    abstract fun bindHealthAssistantRepository(
        impl: LocalHealthAssistantRepositoryImpl
    ): HealthAssistantRepository
}
