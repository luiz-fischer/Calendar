package com.example.calendar.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.calendar.data.local.AppDatabase
import java.time.LocalDate

class NotificationWorker(context: Context, workerParams: WorkerParameters) :
    CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val settingsPrefs = applicationContext.getSharedPreferences("notification_settings", Context.MODE_PRIVATE)
        val enabled = settingsPrefs.getBoolean("enabled", false)
        
        if (!enabled) return Result.success()

        val advanceDays = settingsPrefs.getInt("advanceDays", 1)
        val targetDate = LocalDate.now().plusDays(advanceDays.toLong())
        
        // Recupera dados do armazenamento persistente
        val database = AppDatabase.getDatabase(applicationContext)
        val events = database.eventDao().getEventsByDate(targetDate.toString())

        if (events.isNotEmpty()) {
            val note = events.first().title
            sendNotification(targetDate, note)
        }

        return Result.success()
    }

    private fun sendNotification(date: LocalDate, note: String) {
        val channelId = "calendar_notifications"
        val notificationManager =
            applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId, "Notificações de Agenda", NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }

        val builder = NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Compromisso em ${date.dayOfMonth}/${date.monthValue}")
            .setContentText(note)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)

        notificationManager.notify(date.hashCode(), builder.build())
    }
}
