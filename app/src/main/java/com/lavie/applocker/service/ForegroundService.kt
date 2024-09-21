package com.lavie.applocker.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.lavie.applocker.repository.AppRepository
import com.lavie.applocker.view.LockScreenActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.util.Timer
import java.util.TimerTask

class ForegroundService : Service() {
    private val timer = Timer()
    private var lastForegroundApp = ""
    private lateinit var appRepository: AppRepository
    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.IO + job)

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate() {
        super.onCreate()
        startForegroundService()
        startUsageStatsMonitoring()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun startForegroundService() {
        val channelId = "AppLockChannel"
        val channel = NotificationChannel(
            channelId,
            "App Lock Notification",
            NotificationManager.IMPORTANCE_DEFAULT
        )
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)

        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("App Lock is Running")
            .setContentText("Monitoring all apps")
            .build()

        startForeground(1, notification)
    }

    private fun startUsageStatsMonitoring() {
        timer.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                monitorRunningApps()
            }
        }, 0, 500)
    }

    private fun monitorRunningApps() {
        val usageStatsManager = getSystemService(USAGE_STATS_SERVICE) as UsageStatsManager
        val currentTime = System.currentTimeMillis()

        val usageEvents = usageStatsManager.queryEvents(currentTime - 500, currentTime)
        val event = UsageEvents.Event()

        while (usageEvents.hasNextEvent()) {
            usageEvents.getNextEvent(event)
            val packageName = event.packageName

            Log.d("ForegroundService", "Event detected: type=${event.eventType}, package=$packageName")

            if (event.eventType == UsageEvents.Event.ACTIVITY_RESUMED && packageName != lastForegroundApp) {
                Log.d("ForegroundService", "New foreground app detected: $packageName")
                lastForegroundApp = packageName

                scope.launch {
                    val app = appRepository.getAppByPackage(packageName)
                    if (app != null && app.isLocked) {
                        showLockScreen(packageName)
                    }
                }
                break
            }
        }
    }
    private fun showLockScreen(packageName: String) {
        val lockIntent = Intent(this, LockScreenActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            putExtra("packageName", packageName)
        }
        startActivity(lockIntent)
    }

    override fun onDestroy() {
        super.onDestroy()
        timer.cancel()
    }
}