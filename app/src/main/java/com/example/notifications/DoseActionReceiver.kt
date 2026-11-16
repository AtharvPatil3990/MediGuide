package com.example.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.domain.usecase.ProcessDoseStateTransitionUseCase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class DoseActionReceiver : BroadcastReceiver() {

    @Inject lateinit var notificationHelper: NotificationHelper
    @Inject lateinit var processDoseUseCase: ProcessDoseStateTransitionUseCase

    override fun onReceive(context: Context, intent: Intent) {
        val reminderId = intent.getLongExtra(NotificationHelper.EXTRA_REMINDER_ID, -1L)
        if (reminderId <= 0L) return

        val pendingResult = goAsync()
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                when (intent.action) {
                    NotificationHelper.ACTION_TAKEN -> processDoseUseCase.markTaken(reminderId)
                    NotificationHelper.ACTION_SKIPPED -> processDoseUseCase.markSkipped(reminderId)
                }
                notificationHelper.dismissNotification(reminderId)
            } finally {
                pendingResult.finish()
            }
        }
    }
}
