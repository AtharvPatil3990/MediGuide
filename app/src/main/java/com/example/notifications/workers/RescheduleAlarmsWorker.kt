package com.example.notifications.workers

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.domain.repository.MedicationRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class RescheduleAlarmsWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val medicationRepository: MedicationRepository
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        medicationRepository.reschedulePendingAlarms()
        return Result.success()
    }
}
