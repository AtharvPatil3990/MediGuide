package com.example.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.notifications.workers.RescheduleAlarmsWorker

class BootCompletedReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        if (action == Intent.ACTION_BOOT_COMPLETED ||
            action == Intent.ACTION_TIME_CHANGED ||
            action == Intent.ACTION_TIMEZONE_CHANGED
        ) {
            val workRequest = OneTimeWorkRequestBuilder<RescheduleAlarmsWorker>().build()
            WorkManager.getInstance(context).enqueueUniqueWork(
                "reschedule_alarms_on_boot",
                ExistingWorkPolicy.REPLACE,
                workRequest
            )
        }
    }
}
