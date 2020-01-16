package labs.next.argos.libs

import android.content.Context
import android.os.Build
import android.util.Log
import labs.next.argos.sensors.*

class ContextManager {
    private var context: Context
    private var utils: Utils
    private var database: Database

    private var battery: Battery
    private var bluetooth: Bluetooth
    private var location: Location
    private var network: Network
    private var usageStats: UsageStats
    //private var userActivity: UserActivity
    private var wifi: Wifi
    private var movement: Movement

    constructor(ctx: Context, minRefreshRate: Long = 60000) {
        context = ctx

        utils = Utils(context)
        database = Database(utils.deviceID, false)

        battery = Battery(context, minRefreshRate)
        bluetooth = Bluetooth(context, minRefreshRate)
        location = Location(context, minRefreshRate)
        network = Network(context, minRefreshRate)
        usageStats = UsageStats(context, minRefreshRate)
        //userActivity = UserActivity(context, minRefreshRate)
        wifi = Wifi(context, minRefreshRate)
        movement = Movement(context, minRefreshRate)
    }

    fun startListening() {
        database.setValue("device", Build.MODEL)

        battery.start { (status, level) ->
            database.saveBattery("current_level", level.toString())
            database.saveBattery("is_charging", status.toString())
        }

        Log.d("----", bluetooth.isAvailable().toString())
        if (bluetooth.isAvailable()){
            bluetooth.start { (nearDevices, connectedDevices) ->
                database.saveBluetooth("near_devices", nearDevices.toString())
                database.saveBluetooth("connected_device", connectedDevices.toString())
                //database.saveBluetooth("connected_device_type", bluetooth.connectionType.toString())
            }
        }

        location.start { lastLocation ->
            val location = hashMapOf(
                "lat" to lastLocation?.get("lat"),
                "long" to lastLocation?.get("long")
            )


            if (lastLocation != null && lastLocation.isNotEmpty())
                database.saveLocation("current_location", location)
        }

        network.start { connectionType ->
            database.saveNetwork("connection_type", connectionType)
        }

        usageStats.start { stats ->
            if (stats != null && stats.isNotEmpty())
                database.saveUsage("current_stats", stats.toString())
        }

        /*userActivity.start { activity ->
            if (activity != null && activity.isNotEmpty())
                database.saveActivity("current_activity", activity.toString())
        }*/

        wifi.start { (nearNetworks, connectedNetwork) ->
            if (connectedNetwork != null)
                database.saveWifi("current_network", connectedNetwork)

            if (nearNetworks != null && nearNetworks.isNotEmpty())
                database.saveWifi("available_networks", nearNetworks)
        }
        Log.d("----", movement.isAvailable().toString())
        if (movement.isAvailable()){
            movement.start { state ->
                database.saveMovement("moving", state.toString())
            }
        }


    }

    fun stopListening() {
        battery.stop()
        bluetooth.stop()
        location.stop()
        network.stop()
        usageStats.stop()
        //userActivity.stop()
        wifi.stop()
        movement.stop()
    }
}

