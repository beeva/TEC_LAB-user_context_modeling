package labs.next.argos.libs

import android.app.NotificationChannel
import android.app.NotificationManager
import java.util.*
import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import java.math.BigInteger
import java.security.MessageDigest


class Utils(context: Context) {
    private var pref: SharedPreferences = context.getSharedPreferences("metadata", Context.MODE_PRIVATE)

    companion object {
        private var channels = mapOf(
            "ForegroundService" to "ContextMeasureService",
            "NotificationService" to "ActivityReminderService"
        )

        fun initChannels(context: Context) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                channels.values.forEach { id ->
                    val channel = NotificationChannel(id, id, NotificationManager.IMPORTANCE_DEFAULT)
                    notificationManager.createNotificationChannel(channel)
                }
            }
        }

        fun getClassChannel(current: Class<Any>) : String {
            return channels[current.simpleName] ?: ""
        }

        fun getHash(seed: String) : String {
            val md = MessageDigest.getInstance("SHA-512")
            val digest = md.digest(seed.toByteArray())
            return BigInteger(1, digest).toString(16)
        }
    }

    val deviceID: String
        get() {
            this.javaClass.simpleName
            val candidate = pref.getString("device_id", null)

            return if (candidate == null) {
                val uuid = UUID.randomUUID()
                with(pref.edit()) {
                    putString("device_id", uuid.toString())
                    commit()
                }

                uuid.toString()
            } else candidate
        }

    var incognitoDelay: Long
        get() {
            return pref.getLong("incognito_delay", 0)
        }
        set(value) {
            with(pref.edit()) {
                putLong("incognito_delay", value)
                commit()
            }
        }

    val firstBoot: Boolean
        get() {
            val res = pref.getBoolean("first_boot", true)

            if (res) {
                with(pref.edit()) {
                    putBoolean("first_boot", false)
                    commit()
                }
            }

            return res
        }
}