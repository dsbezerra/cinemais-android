package com.diegobezerra.cinemaisapp

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.NotificationManager.IMPORTANCE_DEFAULT
import android.content.Context
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES

class NotificationHelper {

    companion object {

        private const val PREMIERE_KEY = "premiere_notifications"

        @JvmStatic
        fun createChannels(context: Context) {
            if (VERSION.SDK_INT >= VERSION_CODES.O) {
                with(context.getSystemService(NotificationManager::class.java)) {
                    createNotificationChannel(
                        NotificationChannel(
                            PREMIERE_KEY,
                            context.getString(R.string.notification_channel_premiere),
                            IMPORTANCE_DEFAULT
                        )
                    )
                }
            }
        }
    }

}