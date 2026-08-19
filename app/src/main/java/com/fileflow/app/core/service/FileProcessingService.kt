package com.fileflow.app.core.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.fileflow.app.R

class FileProcessingService : Service() {

    companion object {
        const val CHANNEL_ID = "fileflow_processing_channel"
        const val NOTIFICATION_ID = 1001
        const val ACTION_START = "ACTION_START"
        const val ACTION_UPDATE = "ACTION_UPDATE"
        const val ACTION_STOP = "ACTION_STOP"
        const val EXTRA_TITLE = "EXTRA_TITLE"
        const val EXTRA_PROGRESS = "EXTRA_PROGRESS"
        const val EXTRA_TOTAL = "EXTRA_TOTAL"

        fun start(context: Context, title: String) {
            val intent = Intent(context, FileProcessingService::class.java).apply {
                action = ACTION_START
                putExtra(EXTRA_TITLE, title)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun updateProgress(context: Context, title: String, current: Int, total: Int) {
            val intent = Intent(context, FileProcessingService::class.java).apply {
                action = ACTION_UPDATE
                putExtra(EXTRA_TITLE, title)
                putExtra(EXTRA_PROGRESS, current)
                putExtra(EXTRA_TOTAL, total)
            }
            context.startService(intent)
        }

        fun stop(context: Context) {
            val intent = Intent(context, FileProcessingService::class.java).apply {
                action = ACTION_STOP
            }
            context.startService(intent)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                val title = intent.getStringExtra(EXTRA_TITLE) ?: "Processing document..."
                startForeground(NOTIFICATION_ID, buildNotification(title, 0, 0, true))
            }
            ACTION_UPDATE -> {
                val title = intent.getStringExtra(EXTRA_TITLE) ?: "Processing..."
                val progress = intent.getIntExtra(EXTRA_PROGRESS, 0)
                val total = intent.getIntExtra(EXTRA_TOTAL, 0)
                val notification = buildNotification(title, progress, total, false)
                val manager = getSystemService(NotificationManager::class.java)
                manager.notify(NOTIFICATION_ID, notification)
            }
            ACTION_STOP -> {
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
        }
        return START_NOT_STICKY
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "File Processing",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows progress during document and image processing"
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(
        title: String,
        progress: Int,
        total: Int,
        indeterminate: Boolean
    ): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("FileFlow")
            .setContentText(title)
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .setProgress(total, progress, indeterminate)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .build()
    }
}
