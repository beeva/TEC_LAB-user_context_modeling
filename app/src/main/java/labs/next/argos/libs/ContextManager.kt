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
        wifi = Wifi(context, minRefreshRate)
        movement = Movement(context, minRefreshRate)
    }

    private fun encryptValue(raw: Any) : Any {
        return when (raw) {
            is String -> Utils.getHash(raw)
            is Iterable<*> -> raw.map { Utils.getHash(it as String) }
            else -> ""
        }
    }

    private fun startBattery() {
        if (!battery.isAvailable()) {
            database.saveBattery("available", false)
            return
        }

        battery.start { (status, level) ->
            database.saveBattery("current_level", level.toString())
            database.saveBattery("is_charging", status.toString())
        }
    }

    private fun startBluetooth() {
        if (!bluetooth.isAvailable()) {
            database.saveBluetooth("available", false)
            return
        }

        bluetooth.start { (nearDevices, connectedDevices) ->
            database.saveBluetooth("near_devices", encryptValue(nearDevices))
            database.saveBluetooth("connected_device", encryptValue(connectedDevices))
        }
    }

    private fun startLocation() {
        if (!location.isAvailable()) {
            database.saveLocation("available", false)
            return
        }

        location.start { lastLocation ->
            if (lastLocation != null && lastLocation.isNotEmpty()) {
                database.saveLocation(
                    "current_location", hashMapOf(
                        "lat" to lastLocation?.get("lat"),
                        "long" to lastLocation?.get("long")
                    )
                )
            }
        }
    }

    private fun startMovement() {
        if (!movement.isAvailable()) {
            database.saveMovement("available", false)
            return
        }

        movement.start { state ->
            database.saveMovement("moving", state.toString())
        }
    }

    private fun startNetwork() {
        if (!network.isAvailable()) {
            database.saveNetwork("available", false)
            return
        }

        network.start { connectionType ->
            database.saveNetwork("connection_type", connectionType)
        }
    }

    private fun startUsageStats() {
        if (!usageStats.isAvailable()) {
            database.saveUsage("available", false)
            return
        }

        usageStats.start { stats ->
            if (stats != null && stats.isNotEmpty())
                database.saveUsage("current_stats", stats.toString())
        }
    }

    private fun startWifi() {
        if (!wifi.isAvailable() || !location.isAvailable()) {
            database.saveWifi("available", false)
            return
        }

        wifi.start { (nearNetworks, connectedNetwork) ->
            if (connectedNetwork != null) {
                database.saveWifi("current_network", encryptValue(connectedNetwork))
            } else database.saveWifi("current_network", false)

            if (nearNetworks != null && nearNetworks.isNotEmpty())
                database.saveWifi("available_networks", encryptValue(nearNetworks))
        }
    }

    fun startListening() {
        database.saveDeviceModel(Build.MODEL)
        database.saveIncognito(false)

        startBattery()
        startBluetooth()
        startLocation()
        startMovement()
        startNetwork()
        startUsageStats()
        startWifi()
    }

    fun stopListening() {
        database.saveIncognito(true)

        battery.stop()
        bluetooth.stop()
        location.stop()
        network.stop()
        usageStats.stop()
        wifi.stop()
        movement.stop()
    }
}

