package labs.next.contextmeasurement.modules.service

import android.os.Build
import android.os.IBinder
import android.content.Intent
import android.content.Context
import android.app.Service
import android.app.PendingIntent
import android.app.NotificationChannel
import android.app.NotificationManager

import androidx.core.app.NotificationCompat

import labs.next.contextmeasurement.R
import labs.next.contextmeasurement.MainActivity
import labs.next.contextmeasurement.modules.ContextManager

class ForegroundService : Service() {
    private val CHANNEL_ID = "ForegroundService"
    private val TAG = "Context Measure Service"
    private lateinit var contextManager: ContextManager

    companion object {
        var isRunning: Boolean = false

        fun start(context: Context) {
            val intent = Intent(context, ForegroundService::class.java)
            context.startService(intent)
            isRunning = true
        }

        fun stop(context: Context) {
            val intent = Intent(context, ForegroundService::class.java)
            context.stopService(intent)
            isRunning = false
        }
    }

    override fun onCreate() {
        super.onCreate()
        contextManager = ContextManager(this)
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        contextManager.stopListening()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        this.createNotificationChannel()
        this.createNotification()

        contextManager.startListening()
        return START_STICKY
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(CHANNEL_ID, TAG, NotificationManager.IMPORTANCE_DEFAULT)
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(serviceChannel)
        }
    }

    private fun createNotification() {
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0)

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(TAG)
            .setContentText(getString(R.string.notification_message))
            .setSmallIcon(R.drawable.ic_android)
            .setContentIntent(pendingIntent)
            .build()

        startForeground(1, notification)
    }


}
