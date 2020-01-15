package labs.next.argos.libs

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat.getSystemService
import labs.next.argos.R
import labs.next.argos.activities.MainActivity

import labs.next.argos.libs.ContextManager

//TODO crear accion global
// accion para lanzar notificaciones
const val ACTION_SETNOTIFICATION = "labs.next.argos.SETNOTIFICATION"

class NotificationBroadcast: BroadcastReceiver() {

    //TODO definir channels fuera para usar en esta clase y en foregroundService
    private val channelID = "Daily activity"
    private val tag = "Daily activity"

    override fun onReceive(context: Context, intent: Intent?) {

        when (intent?.action) {
            ACTION_SETNOTIFICATION -> setNotification(context)
        }


    }

    fun setNotification(context: Context){
        val notificationService = NotificationService()
        notificationService.createNotification(context, channelID, context.getString(R.string.notification_title), context.getString(R.string.notification_message))

    }



}
