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
class MedicationAlarmReceiver : BroadcastReceiver() {

    @Inject lateinit var notificationHelper: NotificationHelper
    @Inject lateinit var processDoseUseCase: ProcessDoseStateTransitionUseCase

    override fun onReceive(context: Context, intent: Intent) {
        val reminderId = intent.getLongExtra(NotificationHelper.EXTRA_REMINDER_ID, -1L)
        if (reminderId <= 0L) return

        val medicineName = intent.getStringExtra(NotificationHelper.EXTRA_MEDICINE_NAME) ?: "Medication"
        val dosage = intent.getStringExtra(NotificationHelper.EXTRA_DOSAGE) ?: ""

        val pendingResult = goAsync()
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                // Transition strict state machine: SCHEDULED -> REMINDER_SENT
                processDoseUseCase.triggerReminderSent(reminderId)
                notificationHelper.showDoseReminderNotification(
                    reminderId = reminderId,
                    medicineName = medicineName,
                    dosage = dosage
                )
            } finally {
                pendingResult.finish()
            }
        }
    }
}
