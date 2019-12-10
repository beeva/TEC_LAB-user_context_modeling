package labs.next.argos.service

import android.app.*
import android.os.Binder
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.util.Log
import android.content.Intent
import android.content.Context
import android.content.ServiceConnection

import androidx.core.app.NotificationCompat

import labs.next.argos.R
import labs.next.argos.libs.ContextManager
import labs.next.argos.activities.MainActivity


class ForegroundService : Service() {
    var isListening : Boolean = false

    private val channelID = "ForegroundService"
    private val tag = "Context Measure Service"
    private lateinit var contextManager: ContextManager
    private lateinit var listenCallback : (Boolean) -> Unit

    private var binder : ServiceBinder = ServiceBinder()
    inner class ServiceBinder : Binder() {
        fun getService() : ForegroundService {
            return this@ForegroundService
        }
    }

    companion object {
        var isAlive: Boolean = false

        fun init(context: Context, connection: ServiceConnection, listenOnBoot: Boolean = true) {
            isAlive = true

            val intent = Intent(context, ForegroundService::class.java)
            intent.putExtra("listenOnBoot", listenOnBoot)

            context.startService(intent)
            context.bindService(intent, connection, Context.BIND_AUTO_CREATE)
        }

        fun connect(context: Context, connection: ServiceConnection) {
            val intent = Intent(context, ForegroundService::class.java)
            context.bindService(intent, connection, Context.BIND_AUTO_CREATE)
        }

        fun disconnect(context: Context, connection: ServiceConnection) {
            if (connection != null) {
                try {
                    context.unbindService(connection)
                } catch (e: Exception) {
                    Log.d("Foreground Service - Error:", e.message)
                }
            }
        }
    }

    override fun onCreate() {
        super.onCreate()

        contextManager = ContextManager(this)
    }

    override fun onBind(intent: Intent): IBinder? { return binder }

    override fun onDestroy() {
        super.onDestroy()

        isAlive = false
        contextManager.stopListening()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        createNotificationChannel()
        createNotification()

        val listenOnBoot = intent?.getBooleanExtra("listenOnBoot", true)
        if (listenOnBoot == null || listenOnBoot) startListening()

        return START_STICKY
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(channelID, tag, NotificationManager.IMPORTANCE_DEFAULT)
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(serviceChannel)
        }
    }

    private fun createNotification() {
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0)

        val notification = NotificationCompat.Builder(this, channelID)
            .setContentTitle(getString(R.string.notification_title))
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText(getString(R.string.notification_message)))
            .setSmallIcon(R.drawable.ic_stat_name)
            .setContentIntent(pendingIntent)
            .build()

        startForeground(1, notification)
    }

    fun onListenChange(fn: (Boolean) -> Unit) { listenCallback = fn }

    fun startListening() {
        isListening = true
        if (::listenCallback.isInitialized) listenCallback(true)

        contextManager.startListening()
    }

    fun stopListening(delay: Long = 60000) : Long {
        isListening = false
        if (::listenCallback.isInitialized) listenCallback(false)

        val endTime = System.currentTimeMillis() + delay

        contextManager.stopListening()
        Handler().postDelayed({
            startListening()
        }, delay)

        return endTime
    }
}
