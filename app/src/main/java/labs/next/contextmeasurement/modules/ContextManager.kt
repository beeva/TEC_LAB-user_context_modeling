package labs.next.contextmeasurement.modules

import android.content.Context
import android.util.Log
import labs.next.contextmeasurement.modules.sensors.Bluetooth
import labs.next.contextmeasurement.modules.sensors.Location
import labs.next.contextmeasurement.modules.sensors.Network
import labs.next.contextmeasurement.modules.sensors.Wifi

class ContextManager {
    private var context: Context
    private var wifi: Wifi
    private var network: Network
    private var bluetooth: Bluetooth
    private var location: Location

    constructor(ctx: Context) {
        context = ctx
        wifi = Wifi(context)
        network = Network(context)
        bluetooth = Bluetooth(context)
        location = Location(context)
    }

    fun startListening() {
        network.start { connectionType ->
            Log.d("Service: Network - Connected network type:", connectionType)
        }

        wifi.start { networks ->
            Log.d("Service: Wifi - Connected network SSID:", this.wifi.connectedNetwork)
            Log.d("Service: Wifi - Available networks:", networks.toString())
        }

        bluetooth.start { devices ->
            Log.d("Service: Bluetooth - Devices List:", devices.toString())
            Log.d("Service: Bluetooth - Connected to device:", this.bluetooth.isConnected.toString())
            Log.d("Service: Bluetooth - Connected device type", this.bluetooth.connectionType.toString())
        }

        location.start { lastLocation ->
            val lat = lastLocation?.get("lat")
            val long = lastLocation?.get("long")
            Log.d("Service: Location - Current location:", "$lat, $long")
        }
    }

    fun stopListening() {
        wifi.stop()
        network.stop()
        bluetooth.stop()
        location.stop()
    }
}