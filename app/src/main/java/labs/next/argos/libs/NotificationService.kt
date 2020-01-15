package labs.next.argos.libs

import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import labs.next.argos.R
import labs.next.argos.activities.MainActivity

class NotificationService{

    fun createNotification(context: Context, channel: String, title: String, text: String) {
        val notificationIntent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0)

        val notification = NotificationCompat.Builder(context, channel)
            .setContentTitle(title)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(text))
            .setSmallIcon(R.drawable.ic_app_logo)
            .setContentIntent(pendingIntent)

        with(NotificationManagerCompat.from(context)) {
            // notificationId is a unique int for each notification that you must define
            notify(1, notification.build())
        }

        //context.getString(R.string.notification_title)

    }

}