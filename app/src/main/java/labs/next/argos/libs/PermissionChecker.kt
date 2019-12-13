package labs.next.argos.libs

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat

class PermissionChecker {
    companion object {
        fun check(context: Context, permissions: HashMap<String, Int>) : Pair<Boolean, Array<String>> {
            val currentVersion = Build.VERSION.SDK_INT
            val permissionToRequest = arrayListOf<String>()

            permissions.forEach { (permission: String, minVersion: Int) ->
                if (
                    ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED
                    && currentVersion >= minVersion
                ) permissionToRequest.add(permission)
            }

            return Pair(permissionToRequest.isEmpty(), permissionToRequest.toTypedArray())
        }
    }

}