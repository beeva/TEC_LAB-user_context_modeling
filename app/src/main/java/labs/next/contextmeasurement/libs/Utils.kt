package labs.next.contextmeasurement.modules.libs

import android.content.Context
import android.util.Log
import com.google.firebase.iid.FirebaseInstanceId

class Utils(var context: Context) {
    fun getDeviceID(callback: (String?) -> Unit) {
        FirebaseInstanceId.getInstance().instanceId
            .addOnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Log.w("Module Utils", task.exception)
                    return@addOnCompleteListener
                }

                callback(task.result?.token)
            }
    }
}