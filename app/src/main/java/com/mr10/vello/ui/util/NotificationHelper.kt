package com.mr10.vello.ui.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import com.mr10.vello.R

object NotificationHelper {
    private const val CHANNEL_ID = "vello_notifications"
    private const val CHANNEL_NAME = "Vello Alerts"

    fun showLoginSuccessNotification(context: Context) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH).apply {
                description = "Vello login alerts"
            }
            notificationManager.createNotificationChannel(channel)
        }

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground) // Use appropriate icon
            .setContentTitle("Congrats!")
            .setContentText("You're logged in successfully")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        notificationManager.notify(1, builder.build())
        
        // Play iPhone-like sound
        playNotificationSound(context)
    }

    fun showMessageNotification(context: Context, senderName: String, messageContent: String) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH).apply {
                description = "Vello message alerts"
            }
            notificationManager.createNotificationChannel(channel)
        }

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(senderName)
            .setContentText(messageContent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        notificationManager.notify(System.currentTimeMillis().toInt(), builder.build())
        playNotificationSound(context)
    }

    private fun playNotificationSound(context: Context) {
        try {
            // Looking for res/raw/iphone_notification.mp3
            // If it doesn't exist, we fallback to a system sound or just don't play
            val soundUri = Uri.parse("android.resource://${context.packageName}/raw/iphone_notification")
            val mediaPlayer = MediaPlayer().apply {
                setDataSource(context, soundUri)
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                        .build()
                )
                prepare()
                start()
            }
            mediaPlayer.setOnCompletionListener { it.release() }
        } catch (e: Exception) {
            // Fallback to system notification sound if custom one fails
            // or just ignore if file missing
        }
    }
}
