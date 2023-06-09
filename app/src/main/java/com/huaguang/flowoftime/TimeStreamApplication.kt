package com.huaguang.flowoftime

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import androidx.room.Room
import com.ardakaplan.rdalogger.RDALogger
import com.huaguang.flowoftime.data.EventDatabase
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class TimeStreamApplication @Inject constructor() : Application() {
    companion object {
        const val NOTIFICATION_CHANNEL_ID = "my_service_channel"
    }

    val database: EventDatabase by lazy {
        Room.databaseBuilder(
            this,
            EventDatabase::class.java,
            "event_database"
        ).build()
    }

    override fun onCreate() {
        super.onCreate()

        createNotificationChannel()

        RDALogger.start("打标签喽").enableLogging(true)

    }

    private fun createNotificationChannel() {
        val channelName = "My Service Channel"
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, importance)
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }
}

