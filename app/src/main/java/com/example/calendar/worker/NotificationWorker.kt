package com.example.calendar.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.calendar.R
import java.time.LocalDate

class NotificationWorker(context: Context, workerParams: WorkerParameters) :
    Worker(context, workerParams) {

    override fun doWork(): Result {
        val settingsPrefs = applicationContext.getSharedPreferences("notification_settings", Context.MODE_PRIVATE)
        val enabled = settingsPrefs.getBoolean("enabled", false)
        
        if (!enabled) return Result.success()

        val advanceDays = settingsPrefs.getInt("advanceDays", 1)
        val targetDate = LocalDate.now().plusDays(advanceDays.toLong())
        
        val notesPrefs = applicationContext.getSharedPreferences("calendar_notes", Context.MODE_PRIVATE)
        val note = notesPrefs.getString(targetDate.toString(), null)

        if (note != null) {
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
                channelId,
                "Lembretes do Calendário",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notificações de notas e eventos agendados"
            }
            notificationManager.createNotificationChannel(channel)
        }

        val title = if (LocalDate.now().plusDays(1) == date) {
            "Lembrete para amanhã"
        } else {
            "Lembrete para o dia ${date.dayOfMonth}/${date.monthValue}"
        }

        val builder = NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info) // Usando ícone padrão por enquanto
            .setContentTitle(title)
            .setContentText(note)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)

        notificationManager.notify(date.hashCode(), builder.build())
    }
}
