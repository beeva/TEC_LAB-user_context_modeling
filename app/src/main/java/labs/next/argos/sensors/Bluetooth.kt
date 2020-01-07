package labs.next.argos.sensors

import android.bluetooth.*
import android.util.Log
import android.content.Intent
import android.content.Context
import android.content.IntentFilter
import android.content.BroadcastReceiver
import kotlinx.coroutines.*

class Bluetooth (
    override var context: Context,
    override var minRefreshRate: Long = 5000
) : Sensor<ArrayList<String?>> {

    private var TAG: String = "-------------------------------"
    var MEDIA: String = "media"
    var OTHER: String = "other"
    var connectionType: String? = null
    var isConnected: Boolean = false

    private var run: Boolean = false
    private var scope: CoroutineScope = MainScope()
    private var results: ArrayList<String?> = ArrayList()
    private lateinit var onResult: (ArrayList<String?>) -> Unit

    private var connectedDevices : ArrayList<BluetoothDevice>? = null
    private var manager: BluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    private var adapter: BluetoothAdapter = manager.adapter

    // listen to connections and disconnections from bluetooth service
    private val profileListener = object : BluetoothProfile.ServiceListener {
        override fun onServiceDisconnected(profile: Int) {
            //connectionType = null
            //isConnected = false
            //connectedDevices.add()
            Log.d(TAG, "onServiceDisconnected")

        }

        override fun onServiceConnected(profile: Int, proxy: BluetoothProfile?) {
            Log.d(TAG, "onServiceConnected")
            if (profile == BluetoothProfile.A2DP){
                Log.d(TAG, "A2DP")
                var device: BluetoothA2dp = proxy as BluetoothA2dp
                val devices_list: MutableList<BluetoothDevice> = device.connectedDevices
                devices_list.forEach { when (connectedDevices?.contains(it)) {false -> connectedDevices?.add(it)}  }
                devices_list.forEach { Log.d(TAG, "found: " + it.name) }

                //close proxy
                //adapter.closeProfileProxy(BluetoothProfile.A2DP, device)
            }
            if (profile == BluetoothProfile.HEADSET){
                Log.d(TAG, "HEADSET")
                var device: BluetoothHeadset = proxy as BluetoothHeadset
                val devices_list: MutableList<BluetoothDevice> = device.connectedDevices
                devices_list.forEach { when (connectedDevices?.contains(it)) {false -> connectedDevices?.add(it)}  }
                devices_list.forEach { Log.d(TAG, "found: " + it.name) }

                //close proxy
                //adapter.closeProfileProxy(BluetoothProfile.HEADSET, device)

            }
            if (profile == BluetoothProfile.HID_DEVICE){
                Log.d(TAG, "HID_DEVICE")
                var device: BluetoothHidDevice = proxy as BluetoothHidDevice
                val devices_list: MutableList<BluetoothDevice> = device.connectedDevices
                devices_list.forEach { when (connectedDevices?.contains(it)) {false -> connectedDevices?.add(it)}  }
                devices_list.forEach { Log.d(TAG, "found: " + it.name) }
                //close proxy
                //adapter.closeProfileProxy(BluetoothProfile.HID_DEVICE, device)

            }
            if (profile == BluetoothProfile.HEARING_AID){
                Log.d(TAG, "HEARING_AID")
                var device: BluetoothHearingAid = proxy as BluetoothHearingAid
                val devices_list: MutableList<BluetoothDevice> = device.connectedDevices
                devices_list.forEach { when (connectedDevices?.contains(it)) {false -> connectedDevices?.add(it)}  }
                devices_list.forEach { Log.d(TAG, "found: " + it.name) }
                //close proxy
                //adapter.closeProfileProxy(BluetoothProfile.HEARING_AID, device)
            }
        }
    }

    // listen to connections and disconnections from bluetooth devices
    private var btDeviceReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when(intent?.action) {
                BluetoothDevice.ACTION_ACL_CONNECTED -> {
                    val device: BluetoothDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE) as BluetoothDevice
                    Log.d(TAG, "ONCONNECTED")
                    Log.d(TAG, "found: " + device.name)
                }
                BluetoothDevice.ACTION_ACL_DISCONNECTED -> {
                    val device: BluetoothDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE) as BluetoothDevice
                    Log.d(TAG, "ONDISCONNECTED")
                    Log.d(TAG, "found: " + device.name)
                }

            }
        }
    }

    private var scanReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {

            when(intent?.action) {
                BluetoothDevice.ACTION_FOUND -> {
                    val device: BluetoothDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE) as BluetoothDevice

                    if (device.name != null && !results.contains(device.name))
                        results.add(device.name)
                }
            }
        }
    }

    override fun isAvailable(): Boolean {
        return adapter.isEnabled
    }

    override fun start(onResult: (res: ArrayList<String?>) -> Unit) {
        this.onResult = onResult
        this.run = true

        // register filters and receivers
        //val ifScanReceiver = IntentFilter(BluetoothDevice.ACTION_FOUND)
        val ifBtDeviceReceiver = IntentFilter(BluetoothDevice.ACTION_ACL_CONNECTED)
        //context.registerReceiver(scanReceiver, ifScanReceiver)
        context.registerReceiver(btDeviceReceiver, ifBtDeviceReceiver)

        var bol = adapter.getProfileProxy(context, profileListener, BluetoothProfile.HEADSET)
        bol = adapter.getProfileProxy(context, profileListener, BluetoothProfile.HID_DEVICE)
        bol = adapter.getProfileProxy(context, profileListener, BluetoothProfile.HEARING_AID)
        bol = adapter.getProfileProxy(context, profileListener, BluetoothProfile.A2DP)



        scope.launch { loop() }
    }

    override fun stop() {
        run = false

        try {
            adapter.cancelDiscovery()
            // unregister receivers
            //context.unregisterReceiver(scanReceiver)
            context.unregisterReceiver(btDeviceReceiver)
        } catch (e: Exception) {
            Log.d("Error stopping Bluetooth Service:", e.toString())
        }
    }

    private suspend fun loop() {
        withContext(Dispatchers.IO) {
            while(run) {
                adapter.startDiscovery()

                // get gatt_server devices
                var gattServer_list = manager.getConnectedDevices(BluetoothProfile.GATT_SERVER)
                gattServer_list.forEach { when (connectedDevices?.contains(it)) {false -> connectedDevices?.add(it)}  }
                // get gatt devices
                var gatt_list = manager.getConnectedDevices(BluetoothProfile.GATT)
                gatt_list.forEach { when (connectedDevices?.contains(it)) {false -> connectedDevices?.add(it)}  }
                Log.d(TAG, "onLoop")
                connectedDevices?.forEach { Log.d(TAG, "loop: " + it.name) }

                //TODO eliminar dispositivos una vez no estan conectados
                Thread.sleep(minRefreshRate)
                if (results.isNotEmpty()) {
                    onResult(results)
                    results.clear()
                }
            }
        }
    }
}