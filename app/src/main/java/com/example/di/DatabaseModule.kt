package com.example.di

import android.content.Context
import androidx.room.Room
import com.example.data.local.MediGuideDatabase
import com.example.data.local.dao.CareModuleDao
import com.example.data.local.dao.MedicineDao
import com.example.data.local.dao.ReminderDao
import com.example.data.local.dao.UserDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideMediGuideDatabase(
        @ApplicationContext context: Context
    ): MediGuideDatabase {
        return Room.databaseBuilder(
            context,
            MediGuideDatabase::class.java,
            "mediguide_offline.db"
        ).fallbackToDestructiveMigration(dropAllTables = true).build()
    }

    @Provides
    fun provideUserDao(db: MediGuideDatabase): UserDao = db.userDao()

    @Provides
    fun provideMedicineDao(db: MediGuideDatabase): MedicineDao = db.medicineDao()

    @Provides
    fun provideReminderDao(db: MediGuideDatabase): ReminderDao = db.reminderDao()

    @Provides
    fun provideCareModuleDao(db: MediGuideDatabase): CareModuleDao = db.careModuleDao()
}
