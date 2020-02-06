package labs.next.argos.service

import android.content.Intent
import android.content.Context
import android.app.PendingIntent
import android.app.NotificationManager
import android.media.RingtoneManager
import androidx.core.app.NotificationCompat

import com.google.firebase.messaging.RemoteMessage
import com.google.firebase.messaging.FirebaseMessagingService

import labs.next.argos.R
import labs.next.argos.libs.Utils
import labs.next.argos.libs.Database
import labs.next.argos.activities.MainActivity

class NotificationService : FirebaseMessagingService() {
    private val channelID = Utils.getClassChannel(this.javaClass)

    private lateinit var utils: Utils
    private lateinit var database: Database

    override fun onCreate() {
        super.onCreate()

        utils = Utils(this)
        database = Database(utils.deviceID)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        remoteMessage.notification?.let { sendNotification(it.title, it.body) }
    }

    override fun onNewToken(token: String) {
        database.saveUserToken(token)
    }

    private fun sendNotification(title: String?, messageBody: String?) {
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)

        val pendingIntent = PendingIntent.getActivity(this, 0 , intent,
            PendingIntent.FLAG_ONE_SHOT)

        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notification = NotificationCompat.Builder(this, channelID)
            .setSmallIcon(R.drawable.ic_app_logo)
            .setContentTitle(title)
            .setContentText(messageBody)
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setContentIntent(pendingIntent)
            .build()

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(0, notification)
    }
}