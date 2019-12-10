package labs.next.argos.libs

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.google.firebase.iid.FirebaseInstanceId
import java.time.LocalTime
import java.util.*

class Utils(context: Context) {
    private var pref: SharedPreferences = context.getSharedPreferences("metada", Context.MODE_PRIVATE)

    val deviceID: String
        get() {
            val candidate = pref.getString("device_id", null)

            return if (candidate != null) candidate
            else {
                val uuid = UUID.randomUUID()
                with(pref.edit()) {
                    putString("device_id", uuid.toString())
                    commit()
                }

                uuid.toString()
            }
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