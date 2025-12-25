package com.example.rumafrontend.receiver

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.rumafrontend.MainActivity
import com.example.rumafrontend.R
import com.example.rumafrontend.data.entity.Notifikasi
import com.example.rumafrontend.ui.theme.notification.NotifikasiHolder

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val notificationType = intent.getStringExtra("notificationType") ?: "agenda"

        when (notificationType) {
            "agenda" -> handleAgendaReminder(context, intent)
            "tagihan" -> handleTagihanReminder(context, intent)
        }
    }

    private fun handleAgendaReminder(context: Context, intent: Intent) {
        val agendaId = intent.getLongExtra("agendaId", -1L)
        val title = intent.getStringExtra("title") ?: "Pengingat Agenda"
        val message = intent.getStringExtra("message") ?: "Waktunya agenda dimulai"

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "agenda_reminder"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Pengingat Agenda",
                NotificationManager.IMPORTANCE_HIGH
            )
            manager.createNotificationChannel(channel)
        }

        
        val notificationIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("agendaId", agendaId)
            putExtra("openAgendaDetail", true)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            agendaId.toInt(),
            notificationIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(message)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        manager.notify(agendaId.toInt(), notification)

        
        NotifikasiHolder.add(
            Notifikasi(
                id = agendaId.toInt(),
                pesan = message,
                timestamp = System.currentTimeMillis(),
                jenisNotifikasi = "pengingat_agenda",
                isRead = false,
                referenceId = agendaId.toInt()
            ),
            context 
        )
    }

    private fun handleTagihanReminder(context: Context, intent: Intent) {
        val tagihanId = intent.getIntExtra("tagihanId", -1)
        val title = intent.getStringExtra("title") ?: "Pengingat Tagihan"
        val message = intent.getStringExtra("message") ?: "Tagihan akan jatuh tempo"
        val daysBeforeDue = intent.getIntExtra("daysBeforeDue", 1)
        val dueDateString = intent.getStringExtra("dueDateString") ?: ""

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "tagihan_reminder"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Pengingat Tagihan",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifikasi pengingat untuk tagihan yang akan jatuh tempo"
                enableLights(true)
                enableVibration(true)
                setShowBadge(true)
            }
            manager.createNotificationChannel(channel)
        }

        
        val notificationIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("tagihanId", tagihanId)
            putExtra("openTagihanDetail", true)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            tagihanId,
            notificationIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        
        val notificationText = "Tagihan \"$title\" akan jatuh tempo dalam $daysBeforeDue hari"
        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("⏰ Pengingat Tagihan")
            .setContentText(notificationText)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("Tagihan \"$title\" akan jatuh tempo pada $dueDateString.\n\nSegera lakukan pembayaran untuk menghindari keterlambatan.")
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setVibrate(longArrayOf(0, 500, 250, 500))
            .setDefaults(NotificationCompat.DEFAULT_SOUND)
            .build()

        manager.notify(tagihanId, notification)

        
        NotifikasiHolder.add(
            Notifikasi(
                id = tagihanId,
                pesan = "Tagihan \"$title\" akan jatuh tempo dalam $daysBeforeDue hari (${dueDateString})",
                timestamp = System.currentTimeMillis(),
                jenisNotifikasi = "pengingat_tagihan",
                isRead = false,
                referenceId = tagihanId,
                additionalData = dueDateString
            ),
            context 
        )

        
    }
}