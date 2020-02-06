package labs.next.argos.libs

import android.os.Build
import android.content.Context

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
    private var wifi: Wifi
    private var movement: Movement

    constructor(ctx: Context, minRefreshRate: Long = 60000) {
        context = ctx

        utils = Utils(context)
        database = Database(utils.deviceID)

        battery = Battery(context, minRefreshRate)
        bluetooth = Bluetooth(context, minRefreshRate)
        location = Location(context, minRefreshRate)
        network = Network(context, minRefreshRate)
        usageStats = UsageStats(context, minRefreshRate)
        wifi = Wifi(context, location, minRefreshRate)
        movement = Movement(context, minRefreshRate)
    }

    fun startListening() {
        database.saveDeviceModel(Build.MODEL)

        battery.start { (status, level) ->
            database.saveBattery("current_level", level.toString())
            database.saveBattery("is_charging", status.toString())
        }

        if (bluetooth.isAvailable()){
            bluetooth.start { (nearDevices, connectedDevices) ->
                database.saveBluetooth("near_devices", secure(nearDevices))
                database.saveBluetooth("connected_device", secure(connectedDevices))
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

        wifi.start { (nearNetworks, connectedNetwork) ->
            if (connectedNetwork != null)
                database.saveWifi("current_network", secure(connectedNetwork))

            if (nearNetworks != null && nearNetworks.isNotEmpty())
                database.saveWifi("available_networks", secure(nearNetworks))
        }

        if (movement.isAvailable()){
            movement.start { state ->
                database.saveMovement("moving", state.toString())
            }
        }
    }

    private fun secure(raw: Any) : Any {
        return when (raw) {
            is String -> Utils.getHash(raw)
            is Iterable<*> -> raw.map { Utils.getHash(it as String) }
            else -> ""
        }
    }

    fun stopListening() {
        battery.stop()
        bluetooth.stop()
        location.stop()
        network.stop()
        usageStats.stop()
        wifi.stop()
        movement.stop()
    }
}

