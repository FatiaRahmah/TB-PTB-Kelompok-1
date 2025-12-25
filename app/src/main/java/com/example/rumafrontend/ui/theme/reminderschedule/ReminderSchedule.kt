package com.example.rumafrontend.ui.theme.reminderschedule

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.rumafrontend.receiver.ReminderReceiver
import java.text.SimpleDateFormat
import java.util.*

fun scheduleReminder(
    context: Context,
    agendaId: Long,
    triggerTime: Long,
    title: String,
    message: String,
) {
    if (triggerTime <= System.currentTimeMillis()) return

    val intent = Intent(context, ReminderReceiver::class.java).apply {
        putExtra("notificationType", "agenda")
        putExtra("title", title)
        putExtra("message", message)
    }

    val requestCode = agendaId.toInt()

    val pendingIntent = PendingIntent.getBroadcast(
        context,
        requestCode,
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                return
            }
        }

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            triggerTime,
            pendingIntent
        )
    } catch (e: SecurityException) {
        e.printStackTrace()
    }
}

fun cancelReminder(context: Context, agendaId: Long) {
    val intent = Intent(context, ReminderReceiver::class.java)
    val requestCode = agendaId.toInt()

    val pendingIntent = PendingIntent.getBroadcast(
        context,
        requestCode,
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    alarmManager.cancel(pendingIntent)
    pendingIntent.cancel()
}

fun scheduleTagihanReminder(
    context: Context,
    tagihanId: Int,
    tagihanTitle: String,
    dueDateMillis: Long,
    reminderDays: Int
) {
    
    
    
    val reminderDurationMillis = if (reminderDays == -1) {
        10 * 60 * 1000L 
    } else {
        reminderDays * 24 * 60 * 60 * 1000L 
    }
    
    val reminderTimeMillis = dueDateMillis - reminderDurationMillis
    val currentTimeMillis = System.currentTimeMillis()

    
    if (reminderTimeMillis <= currentTimeMillis) {
        
        return
    }

    
    val dateFormat = SimpleDateFormat("dd MMMM yyyy", Locale("id", "ID"))
    val dueDateString = dateFormat.format(Date(dueDateMillis))

    val intent = Intent(context, ReminderReceiver::class.java).apply {
        putExtra("notificationType", "tagihan")
        putExtra("tagihanId", tagihanId)
        putExtra("title", tagihanTitle)
        putExtra("message", "Tagihan akan jatuh tempo")
        putExtra("daysBeforeDue", reminderDays)
        putExtra("dueDateString", dueDateString)
    }

    val pendingIntent = PendingIntent.getBroadcast(
        context,
        tagihanId,
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    try {
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                
                return
            }
        }

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            reminderTimeMillis,
            pendingIntent
        )

        
        
        
        val reminderText = if (reminderDays == -1) "10 minutes" else "$reminderDays days"
        
        

    } catch (e: SecurityException) {
        
        e.printStackTrace()
    }
}

fun cancelTagihanReminder(context: Context, tagihanId: Int) {
    val intent = Intent(context, ReminderReceiver::class.java)

    val pendingIntent = PendingIntent.getBroadcast(
        context,
        tagihanId,
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    alarmManager.cancel(pendingIntent)
    pendingIntent.cancel()

    
}

fun rescheduleTagihanReminder(
    context: Context,
    tagihanId: Int,
    tagihanTitle: String,
    dueDateMillis: Long,
    reminderDays: Int
) {
    cancelTagihanReminder(context, tagihanId)
    scheduleTagihanReminder(context, tagihanId, tagihanTitle, dueDateMillis, reminderDays)
    
}