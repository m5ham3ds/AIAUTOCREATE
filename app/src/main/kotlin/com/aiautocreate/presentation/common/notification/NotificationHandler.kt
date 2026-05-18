package com.aiautocreate.presentation.common.notification

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
import com.aiautocreate.MainActivity
import com.aiautocreate.R
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * معالج الإشعارات على مستوى النظام (Status Bar).
 * ينشئ قنوات إشعار للأجهزة الحديثة ويرسل إشعارات بسيطة أو تفاعلية.
 */
@Singleton
class NotificationHandler @Inject constructor(
    @ApplicationContext private val context: Context
) {

    companion object {
        const val CHANNEL_VIDEO_GENERATION = "video_generation_channel"
        const val CHANNEL_SYNC = "sync_channel"
        const val CHANNEL_SYSTEM = "system_channel"
        const val NOTIFICATION_ID_VIDEO = 1001
        const val NOTIFICATION_ID_SYNC = 1002
    }

    init {
        createNotificationChannels()
    }

    /**
     * يرسل إشعاراً عند اكتمال توليد فيديو.
     */
    fun showVideoGeneratedNotification(projectTitle: String, projectId: Long) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
                return // بدون إذن، لا نرسل
            }
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("projectId", projectId)
        }
        val pendingIntent = PendingIntent.getActivity(
            context, projectId.toInt(), intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_VIDEO_GENERATION)
            .setSmallIcon(R.drawable.ic_notification)  // سنضيف أيقونة
            .setContentTitle(context.getString(R.string.notif_video_ready_title))
            .setContentText(context.getString(R.string.notif_video_ready_text, projectTitle))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context).notify(NOTIFICATION_ID_VIDEO, notification)
    }

    /**
     * يرسل إشعاراً عند فشل التوليد.
     */
    fun showVideoFailedNotification(projectTitle: String, reason: String) {
        val notification = NotificationCompat.Builder(context, CHANNEL_VIDEO_GENERATION)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(context.getString(R.string.notif_video_failed_title))
            .setContentText(context.getString(R.string.notif_video_failed_text, projectTitle, reason))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context).notify(NOTIFICATION_ID_VIDEO + 1, notification)
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val videoChannel = NotificationChannel(
                CHANNEL_VIDEO_GENERATION,
                context.getString(R.string.channel_video_name),
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = context.getString(R.string.channel_video_desc)
            }

            val syncChannel = NotificationChannel(
                CHANNEL_SYNC,
                context.getString(R.string.channel_sync_name),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = context.getString(R.string.channel_sync_desc)
            }

            val systemChannel = NotificationChannel(
                CHANNEL_SYSTEM,
                context.getString(R.string.channel_system_name),
                NotificationManager.IMPORTANCE_MIN
            ).apply {
                description = context.getString(R.string.channel_system_desc)
            }

            val manager = context.getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(videoChannel)
            manager.createNotificationChannel(syncChannel)
            manager.createNotificationChannel(systemChannel)
        }
    }
}