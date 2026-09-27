package dev.xykal.nabungin.notify

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
import dev.xykal.nabungin.MainActivity
import dev.xykal.nabungin.R
import dev.xykal.nabungin.domain.format.Money

object Notifications {
    const val CHANNEL_REMINDER = "reminder"

    fun ensureChannel(context: Context) {
        val manager = context.getSystemService(NotificationManager::class.java) ?: return
        val channel = NotificationChannel(
            CHANNEL_REMINDER,
            context.getString(R.string.channel_reminder_name),
            NotificationManager.IMPORTANCE_DEFAULT,
        ).apply {
            description = context.getString(R.string.channel_reminder_desc)
        }
        manager.createNotificationChannel(channel)
    }

    private fun allowed(context: Context): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
            if (granted != PackageManager.PERMISSION_GRANTED) return false
        }
        return NotificationManagerCompat.from(context).areNotificationsEnabled()
    }

    fun reminder(
        context: Context,
        id: Int,
        title: String,
        body: String,
        streak: Int,
        todayTotal: Long,
    ) {
        if (!allowed(context)) return
        ensureChannel(context)
        val text = buildString {
            append(body)
            if (streak > 0) append("\nStreak: $streak hari berturut-turut")
            if (todayTotal > 0) append("\nHari ini: ${Money.format(todayTotal)}")
        }
        notify(context, id, title, text)
    }

    fun autoSaveSummary(context: Context, inserted: List<Pair<String, Long>>) {
        if (inserted.isEmpty() || !allowed(context)) return
        ensureChannel(context)
        val total = inserted.sumOf { it.second }
        val title = "Auto-save jalan"
        val body = buildString {
            append("${inserted.size} tujuan kena setor otomatis, total ${Money.format(total)}")
            inserted.take(4).forEach { (name, amount) ->
                append("\n- $name: ${Money.format(amount)}")
            }
        }
        notify(context, ID_AUTOSAVE, title, body)
    }

    private fun notify(context: Context, id: Int, title: String, text: String) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pending = PendingIntent.getActivity(
            context,
            id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val notification = NotificationCompat.Builder(context, CHANNEL_REMINDER)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(text.lineSequence().first())
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setContentIntent(pending)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()
        runCatching { NotificationManagerCompat.from(context).notify(id, notification) }
    }

    const val ID_REMINDER = 1001
    const val ID_AUTOSAVE = 1002
}
