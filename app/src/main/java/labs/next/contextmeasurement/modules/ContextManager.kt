package labs.next.contextmeasurement.modules

import android.content.Context
import android.os.Build
import android.util.Log
import labs.next.contextmeasurement.modules.sensors.*

class ContextManager {
    private var context: Context
    private lateinit var deviceId: String

    private var utils: Utils
    private lateinit var database: Database


    /*private var bluetooth: Bluetooth
    private var location: Location
    private var network: Network
    private var usageStats: UsageStats
    private var userActivity: UserActivity*/
    private var wifi: Wifi

    val permissions: Array<String>
        get() {
            var res: Array<String> = arrayOf()
            /*res.plus(bluetooth.permissions)
            res.plus(location.permissions)
            res.plus(network.permissions)
            res.plus(usageStats.permissions)
            res.plus(userActivity.permissions)*/
            res.plus(wifi.permissions)

            return res
        }

    constructor(ctx: Context) {
        context = ctx

        utils = Utils(context)
        utils.getDeviceID{ id ->
            if (id == null) {
                throw Exception("Cannot identify device.")
            }

            deviceId = id
            database = Database(deviceId)
            database.setValue("device", Build.MODEL)
        }

        wifi = Wifi(context)
        /*network = Network(context)
        bluetooth = Bluetooth(context)
        location = Location(context)
        userActivity = UserActivity(context)
        usageStats = UsageStats(context)*/
    }

    fun startListening() {
        /*network.start { connectionType ->
            Log.d("Service: Network - Connected network type", connectionType)
        }*/

        wifi.start { networks ->
            Log.d("Service: Wifi - Connected network SSID", wifi.connectedNetwork)
            Log.d("Service: Wifi - Available networks", networks.toString())

            database.saveWifi("current_network", wifi.connectedNetwork)
            database.saveWifi("available_networks", networks.toString())
        }

        /*bluetooth.start { devices ->
            Log.d("Service: Bluetooth - Devices List", devices.toString())
            Log.d("Service: Bluetooth - Connected to device", bluetooth.isConnected.toString())
            Log.d("Service: Bluetooth - Connected device type", bluetooth.connectionType.toString())
        }

        location.start { lastLocation ->
            val lat = lastLocation?.get("lat")
            val long = lastLocation?.get("long")
            Log.d("Service: Location - Current location", "$lat, $long")
        }

        userActivity.start { activity ->
            Log.d("Service: UserActivity - Current activity", activity.toString())
        }

        usageStats.start { stats ->
            Log.d("Service: UsageStats - Current stats", stats.toString())
        }*/
    }

    fun stopListening() {
        wifi.stop()
        /*network.stop()
        bluetooth.stop()
        location.stop()
        userActivity.stop()
        usageStats.stop()*/
    }
}