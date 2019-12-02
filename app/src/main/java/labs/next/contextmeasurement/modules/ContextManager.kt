package labs.next.contextmeasurement.modules

import android.content.Context
import android.util.Log
import labs.next.contextmeasurement.modules.sensors.Bluetooth
import labs.next.contextmeasurement.modules.sensors.Network
import labs.next.contextmeasurement.modules.sensors.Wifi

class ContextManager {
    private var context: Context
    private var wifi: Wifi
    private var network: Network
    private var bluetooth: Bluetooth

    constructor(ctx: Context) {
        context = ctx
        wifi = Wifi(context)
        network = Network(context)
        bluetooth = Bluetooth(context)
    }

    fun startListening() {
        this.network.start {
            Log.d("Service: Network", it)
        }

        this.wifi.start {
            Log.d("Service: Wifi Connection", this.wifi.connectedNetwork)
            Log.d("Service: Wifi Networks", it.toString())
        }

        this.bluetooth.start {
            Log.d("Service: Bluetooth devices", it.toString())
            Log.d("Service: Bluetooth connection", this.bluetooth.isConnected.toString())
            Log.d("Service: Bluetooth type", this.bluetooth.connectionType)
        }
    }

    fun stopListening() {
        this.wifi.stop()
        this.network.stop()
        this.bluetooth.stop()
    }
}