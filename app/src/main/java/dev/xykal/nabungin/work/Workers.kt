package dev.xykal.nabungin.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import dev.xykal.nabungin.NabunginApp
import dev.xykal.nabungin.data.prefs.AppSettings
import dev.xykal.nabungin.domain.SavingsMath
import dev.xykal.nabungin.domain.format.Money
import dev.xykal.nabungin.domain.model.DepositSource
import dev.xykal.nabungin.notify.Notifications
import java.time.Duration
import java.time.LocalDate
import java.time.LocalTime
import java.util.concurrent.TimeUnit

/** Eksekutor auto-save: jalan tiap jam, hanya menyetor kalau jadwalnya sudah jatuh tempo. */
class AutoSaveWorker(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val container = (applicationContext as NabunginApp).container
        val repository = container.repository
        val rules = repository.enabledRules()
        if (rules.isEmpty()) return Result.success()

        val today = LocalDate.now()
        val now = LocalTime.now()
        val inserted = mutableListOf<Pair<String, Long>>()
        val goals = repository.rawGoals().associateBy { it.id }

        rules.forEach { rule ->
            val goal = goals[rule.goalId] ?: return@forEach
            val due = rule.lastRun == null ||
                !today.isBefore(rule.lastRun.plusDays(rule.interval.days.toLong()))
            val alreadyNow = now.hour * 60 + now.minute >= rule.hour * 60 + rule.minute
            if (!due || !alreadyNow) return@forEach
            if (rule.amount <= 0L) return@forEach

            repository.addDeposit(
                goalId = rule.goalId,
                amount = rule.amount,
                day = today,
                note = "Auto-save ${rule.interval.label.lowercase()}",
                source = DepositSource.AUTO,
            )
            repository.markRuleRun(rule.goalId, today)
            inserted += goal.name to rule.amount
        }

        Notifications.autoSaveSummary(applicationContext, inserted)
        return Result.success()
    }
}

/** Pengingat harian: dipanggil pada jam yang dipilih user. */
class ReminderWorker(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val container = (applicationContext as NabunginApp).container
        val settings = container.settings.current()
        if (!settings.reminderEnabled) return Result.success()

        val deposits = container.repository.allDepositsOnce()
        val todayTotal = SavingsMath.dayTotal(deposits, LocalDate.now())
        val streak = SavingsMath.streak(deposits)
        val goals = container.repository.rawGoals().size
        val body = when {
            todayTotal > 0L && streak > 1 -> "Streak lu jalan, jangan putus. Tambah setoran lagi?"
            todayTotal > 0L -> "Setoran hari ini sudah masuk. Lanjut besok ya."
            goals == 0 -> "Bikin tujuan tabungan pertama lu, mulai dari nominal kecil."
            else -> "Hari ini belum ada setoran. Sisihkan sedikit dulu, biar target tetap on-track."
        }
        Notifications.reminder(
            context = applicationContext,
            id = Notifications.ID_REMINDER,
            title = "Waktunya nabung",
            body = body,
            streak = streak,
            todayTotal = todayTotal,
        )
        return Result.success()
    }
}

object WorkScheduler {
    private const val AUTO_SAVE = "nabungin.autosave"
    private const val REMINDER = "nabungin.reminder"

    fun syncAutoSave(context: Context) {
        val request = PeriodicWorkRequestBuilder<AutoSaveWorker>(1, TimeUnit.HOURS)
            .setInitialDelay(Duration.ofMinutes(20))
            .addTag(AUTO_SAVE)
            .build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            AUTO_SAVE,
            ExistingPeriodicWorkPolicy.UPDATE,
            request,
        )
    }

    fun syncReminder(context: Context, settings: AppSettings) {
        if (!settings.reminderEnabled) {
            cancelReminder(context)
            return
        }
        val now = LocalTime.now()
        val target = LocalTime.of(settings.reminderHour, settings.reminderMinute)
        val delay = if (target.isAfter(now)) {
            Duration.between(now, target)
        } else {
            Duration.between(now, target).plusDays(1)
        }
        val request = PeriodicWorkRequestBuilder<ReminderWorker>(1, TimeUnit.DAYS)
            .setInitialDelay(delay)
            .addTag(REMINDER)
            .build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            REMINDER,
            ExistingPeriodicWorkPolicy.UPDATE,
            request,
        )
    }

    fun cancelReminder(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(REMINDER)
    }
}
