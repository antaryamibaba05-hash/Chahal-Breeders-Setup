package com.highflyerpro.tracker.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.highflyerpro.tracker.data.local.AppDatabase
import com.highflyerpro.tracker.data.repository.HighFlyerRepository
import com.highflyerpro.tracker.data.repository.PreferencesRepository
import kotlinx.coroutines.flow.first
import java.util.concurrent.TimeUnit

class DailyBackupWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    companion object {
        private const val WORK_NAME = "chahal_breeders_daily_backup"
        private const val CHANNEL_ID = "chahal_breeders_backup_channel"

        fun scheduleDailyBackup(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiresStorageNotLow(true)
                .build()

            val workRequest = PeriodicWorkRequestBuilder<DailyBackupWorker>(24, TimeUnit.HOURS)
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                workRequest
            )
        }
    }

    override suspend fun doWork(): Result {
        val prefs = PreferencesRepository(context)
        val isAutoEnabled = prefs.autoBackupEnabledFlow.first()
        if (!isAutoEnabled) return Result.success()

        val folderUriStr = prefs.backupFolderUriFlow.first() ?: return Result.success()

        val folderUri = try {
            Uri.parse(folderUriStr)
        } catch (e: Exception) {
            return Result.failure()
        }

        val database = AppDatabase.getDatabase(context)
        val repository = HighFlyerRepository(database)

        return when (val result = BackupRestoreManager.createBackup(context, database, repository, folderUri)) {
            is BackupOperationResult.Success -> {
                prefs.setLastBackupTimestamp(System.currentTimeMillis())
                showNotification(
                    title = "Daily Backup Completed",
                    message = "Successfully backed up ${result.metadata.totalPigeons} pigeons to ${result.metadata.fileName}"
                )
                Result.success()
            }
            is BackupOperationResult.Error -> {
                showNotification(
                    title = "Daily Backup Failed",
                    message = result.message
                )
                Result.retry()
            }
        }
    }

    private fun showNotification(title: String, message: String) {
        try {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
                ?: return

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    CHANNEL_ID,
                    "High Flyer Pro Backup Notifications",
                    NotificationManager.IMPORTANCE_LOW
                )
                notificationManager.createNotificationChannel(channel)
            }

            val notification = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_menu_save)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setAutoCancel(true)
                .build()

            notificationManager.notify(1001, notification)
        } catch (e: Exception) {
            android.util.Log.e("DailyBackupWorker", "Failed to show notification: ${e.message}", e)
        }
    }
}
