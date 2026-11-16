package com.example.notifications.workers

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.domain.repository.MedicationRepository
import com.example.notifications.NotificationHelper
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class RefillCheckWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val medicationRepository: MedicationRepository,
    private val notificationHelper: NotificationHelper
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val lowStock = medicationRepository.checkLowInventoryForAlerts()
        if (lowStock.isNotEmpty()) {
            val names = lowStock.joinToString(", ") { it.name }
            notificationHelper.showRefillAlertNotification(lowStock.size, names)
        }
        return Result.success()
    }
}
