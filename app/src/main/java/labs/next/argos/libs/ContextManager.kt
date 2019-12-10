package labs.next.argos.libs

import android.os.Build
import android.content.Context

import labs.next.argos.sensors.*

class ContextManager {
    private var context: Context
    private var utils: Utils
    private var database: Database


    private var bluetooth: Bluetooth
    private var location: Location
    private var network: Network
    private var usageStats: UsageStats
    private var userActivity: UserActivity
    private var wifi: Wifi

    constructor(ctx: Context, minRefreshRate : Long = 60000) {
        context = ctx

        utils = Utils(context)
        database = Database(utils.deviceID, false)

        wifi = Wifi(context, 10000)
//        wifi = Wifi(context, minRefreshRate)
        network = Network(context, minRefreshRate)
        bluetooth = Bluetooth(context, minRefreshRate)
        location = Location(context, minRefreshRate)
        userActivity = UserActivity(context, minRefreshRate)
        usageStats = UsageStats(context, minRefreshRate)
    }

    fun startListening() {
        database.setValue("device", Build.MODEL)

        wifi.start { networks ->
            database.saveWifi("current_network", wifi.connectedNetwork)
            database.saveWifi("available_networks", networks.toString())
        }

        network.start { connectionType ->
            database.saveNetwork("connection_type", connectionType)
        }

        bluetooth.start { devices ->
            database.saveBluetooth("near_devices", devices.toString())
            database.saveBluetooth("connected_device", bluetooth.isConnected.toString())
            database.saveBluetooth("connected_device_type", bluetooth.connectionType.toString())
        }

        location.start { lastLocation ->
            val location = object {
                val lat = lastLocation?.get("lat")
                val long = lastLocation?.get("long")
            }

            database.saveLocation("current_location", location)
        }

        userActivity.start { activity ->
            database.saveActivity("current_activity", activity.toString())
        }

        usageStats.start { stats ->
            database.saveUsage("current_stats", stats.toString())
        }
    }

    fun stopListening() {
        wifi.stop()
        network.stop()
        bluetooth.stop()
        location.stop()
        userActivity.stop()
        usageStats.stop()
    }
}