package com.example.notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.MainActivity
import com.example.R
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        const val CHANNEL_DOSES = "mediguide_doses"
        const val CHANNEL_REFILLS = "mediguide_refills"
        const val EXTRA_REMINDER_ID = "extra_reminder_id"
        const val EXTRA_MEDICINE_NAME = "extra_medicine_name"
        const val EXTRA_DOSAGE = "extra_dosage"
        const val ACTION_TAKEN = "com.example.mediguide.ACTION_DOSE_TAKEN"
        const val ACTION_SKIPPED = "com.example.mediguide.ACTION_DOSE_SKIPPED"
    }

    fun ensureChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(NotificationManager::class.java) ?: return
            val doseChannel = NotificationChannel(
                CHANNEL_DOSES,
                "Medication Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Exact-time offline medication schedule alerts"
                enableVibration(true)
            }
            val refillChannel = NotificationChannel(
                CHANNEL_REFILLS,
                "Refill Alerts",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Low medication inventory alerts"
            }
            manager.createNotificationChannel(doseChannel)
            manager.createNotificationChannel(refillChannel)
        }
    }

    fun hasNotificationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    fun showDoseReminderNotification(
        reminderId: Long,
        medicineName: String,
        dosage: String
    ) {
        ensureChannels()
        if (!hasNotificationPermission()) return

        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val openPendingIntent = PendingIntent.getActivity(
            context,
            reminderId.toInt(),
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val takenIntent = Intent(context, DoseActionReceiver::class.java).apply {
            action = ACTION_TAKEN
            putExtra(EXTRA_REMINDER_ID, reminderId)
        }
        val takenPendingIntent = PendingIntent.getBroadcast(
            context,
            (reminderId * 10 + 1).toInt(),
            takenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val skipIntent = Intent(context, DoseActionReceiver::class.java).apply {
            action = ACTION_SKIPPED
            putExtra(EXTRA_REMINDER_ID, reminderId)
        }
        val skipPendingIntent = PendingIntent.getBroadcast(
            context,
            (reminderId * 10 + 2).toInt(),
            skipIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_DOSES)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("MediGuide: $medicineName")
            .setContentText("Scheduled dose: $dosage")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setAutoCancel(true)
            .setContentIntent(openPendingIntent)
            .addAction(0, "Taken", takenPendingIntent)
            .addAction(0, "Skip", skipPendingIntent)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(reminderId.toInt(), notification)
        } catch (_: SecurityException) {
            // Permission may have been revoked at runtime
        }
    }

    fun showRefillAlertNotification(count: Int, summaryNames: String) {
        ensureChannels()
        if (!hasNotificationPermission()) return

        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val openPendingIntent = PendingIntent.getActivity(
            context,
            9901,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_REFILLS)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("MediGuide Refill Alert ($count)")
            .setContentText("Low stock: $summaryNames")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(openPendingIntent)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(9901, notification)
        } catch (_: SecurityException) {
        }
    }

    fun dismissNotification(reminderId: Long) {
        NotificationManagerCompat.from(context).cancel(reminderId.toInt())
    }
}
