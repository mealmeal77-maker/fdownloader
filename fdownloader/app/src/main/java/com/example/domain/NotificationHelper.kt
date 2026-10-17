package com.example.domain

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

class NotificationHelper(private val context: Context) {

    companion object {
        const val CHANNEL_DOWNLOADS = "streamvault_downloads_channel"
        const val CHANNEL_PLAYBACK = "streamvault_playback_channel"
    }

    init {
        createChannels()
    }

    private fun createChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val downloadChannel = NotificationChannel(
                CHANNEL_DOWNLOADS,
                context.getString(R.string.notification_channel_name),
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = context.getString(R.string.notification_channel_desc)
            }

            val playbackChannel = NotificationChannel(
                CHANNEL_PLAYBACK,
                "Fdownloader Arka Plan Oynatıcı",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Arka planda çalan medya kontrolleri"
            }

            manager.createNotificationChannel(downloadChannel)
            manager.createNotificationChannel(playbackChannel)
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

    fun sendDownloadCompletedNotification(
        taskId: Long,
        title: String,
        qualityLabel: String,
        platformName: String
    ) {
        if (!hasNotificationPermission()) return

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            taskId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_DOWNLOADS)
            .setSmallIcon(android.R.drawable.stat_sys_download_done)
            .setContentTitle("İndirme Tamamlandı • $platformName")
            .setContentText("$title ($qualityLabel) çevrimdışı oynatmaya hazır.")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        runCatching {
            NotificationManagerCompat.from(context).notify(1000 + taskId.toInt(), notification)
        }
    }

    fun updateBackgroundPlaybackNotification(
        title: String,
        author: String,
        isPlaying: Boolean
    ) {
        if (!hasNotificationPermission()) return

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            9999,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val statusText = if (isPlaying) "Arka planda çalıyor" else "Duraklatıldı"
        val notification = NotificationCompat.Builder(context, CHANNEL_PLAYBACK)
            .setSmallIcon(if (isPlaying) android.R.drawable.ic_media_play else android.R.drawable.ic_media_pause)
            .setContentTitle(title)
            .setContentText("$author • $statusText")
            .setOngoing(isPlaying)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()

        runCatching {
            NotificationManagerCompat.from(context).notify(9999, notification)
        }
    }

    fun cancelPlaybackNotification() {
        runCatching {
            NotificationManagerCompat.from(context).cancel(9999)
        }
    }
}
