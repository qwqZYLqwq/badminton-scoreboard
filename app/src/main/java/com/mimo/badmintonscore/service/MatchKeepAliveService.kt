package com.mimo.badmintonscore.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.mimo.badmintonscore.MainActivity
import com.mimo.badmintonscore.R

class MatchKeepAliveService : Service() {

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent == null) return START_NOT_STICKY

        val action = intent.action ?: ACTION_START_OR_UPDATE
        if (action == ACTION_STOP) {
            stopForegroundCompat()
            stopSelf()
            return START_NOT_STICKY
        }

        val title = intent.getStringExtra(EXTRA_TITLE) ?: "🏸 羽毛球比赛计分进行中"
        val content = intent.getStringExtra(EXTRA_CONTENT) ?: "后台保护服务已激活，防杀进程保活中"
        val subText = intent.getStringExtra(EXTRA_SUBTEXT)

        val notification = buildNotification(title, content, subText)
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                startForeground(
                    NOTIFICATION_ID,
                    notification,
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
                )
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                startForeground(NOTIFICATION_ID, notification, 0)
            } else {
                startForeground(NOTIFICATION_ID, notification)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return START_STICKY
    }

    private fun stopForegroundCompat() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            stopForeground(STOP_FOREGROUND_REMOVE)
        } else {
            @Suppress("DEPRECATION")
            stopForeground(true)
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "羽毛球比赛后台保护与实时比分",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "比赛进行中常驻通知，提升系统后台保活优先级，避免被系统清理"
                setShowBadge(false)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(title: String, content: String, subText: String?): Notification {
        val clickIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_FROM_NOTIFICATION, true)
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            clickIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(content)
            .setSmallIcon(R.drawable.ic_notification_shuttlecock)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)

        if (!subText.isNullOrBlank()) {
            builder.setSubText(subText)
        }

        return builder.build()
    }

    companion object {
        const val CHANNEL_ID = "badminton_match_keep_alive_channel"
        const val NOTIFICATION_ID = 2026

        const val ACTION_START_OR_UPDATE = "com.mimo.badmintonscore.action.START_OR_UPDATE"
        const val ACTION_STOP = "com.mimo.badmintonscore.action.STOP"

        const val EXTRA_TITLE = "extra_title"
        const val EXTRA_CONTENT = "extra_content"
        const val EXTRA_SUBTEXT = "extra_subtext"
        const val EXTRA_FROM_NOTIFICATION = "extra_from_notification"

        fun startOrUpdate(
            context: Context,
            leftName: String,
            rightName: String,
            leftScore: Int,
            rightScore: Int,
            gameIndex: Int,
            leftGames: Int,
            rightGames: Int,
            servingSideName: String
        ) {
            val intent = Intent(context, MatchKeepAliveService::class.java).apply {
                action = ACTION_START_OR_UPDATE
                putExtra(EXTRA_TITLE, "🏸 计分中: $leftName VS $rightName")
                putExtra(EXTRA_CONTENT, "比分 $leftScore : $rightScore · $servingSideName 发球")
                putExtra(EXTRA_SUBTEXT, "第${gameIndex}局 (大比分 $leftGames:$rightGames)")
            }
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(intent)
                } else {
                    context.startService(intent)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        fun stop(context: Context) {
            val intent = Intent(context, MatchKeepAliveService::class.java).apply {
                action = ACTION_STOP
            }
            try {
                context.startService(intent)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
