package labs.next.contextmeasurement.modules

import android.content.Context
import android.util.Log
import labs.next.contextmeasurement.modules.sensors.*

class ContextManager {
    private var context: Context

    private var wifi: Wifi
    private var network: Network
    private var bluetooth: Bluetooth
    private var location: Location
    private var userActivity: UserActivity
    private var usageStats: UsageStats

    constructor(ctx: Context) {
        context = ctx
        wifi = Wifi(context)
        network = Network(context)
        bluetooth = Bluetooth(context)
        location = Location(context)
        userActivity = UserActivity(context)
        usageStats = UsageStats(context)
    }

    fun startListening() {
        network.start { connectionType ->
            Log.d("Service: Network - Connected network type", connectionType)
        }

        wifi.start { networks ->
            Log.d("Service: Wifi - Connected network SSID", wifi.connectedNetwork)
            Log.d("Service: Wifi - Available networks", networks.toString())
        }

        bluetooth.start { devices ->
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