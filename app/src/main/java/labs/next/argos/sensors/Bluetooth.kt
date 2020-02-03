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
) : Sensor<Pair<ArrayList<String>, ArrayList<String>>> {
    private var nearDevices = arrayListOf<String>()
    private var connectedDevices =  arrayListOf<BDevice>()

    private var run: Boolean = false
    private var scope: CoroutineScope = MainScope()
    private lateinit var onResult: (Pair<ArrayList<String>, ArrayList<String>>) -> Unit

    private var manager: BluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    private var adapter: BluetoothAdapter? = manager.adapter


    // listen to connections and disconnections from bluetooth service
    private val profileListener = object : BluetoothProfile.ServiceListener {
        override fun onServiceDisconnected(profile: Int) {
            connectedDevices.clear()
        }

        override fun onServiceConnected(profile: Int, proxy: BluetoothProfile?) {
            var currentProfile = "NONE"
            var devicesList = mutableListOf<BluetoothDevice>()

            when (profile) {
                BluetoothProfile.A2DP -> {
                    var device: BluetoothA2dp = proxy as BluetoothA2dp
                    currentProfile = "A2DP"
                    devicesList = device.connectedDevices
                }
                BluetoothProfile.HEADSET -> {
                    var device: BluetoothHeadset = proxy as BluetoothHeadset
                    currentProfile = "HEADSET"
                    devicesList = device.connectedDevices
                }
                BluetoothProfile.HID_DEVICE -> {
                    var device: BluetoothHidDevice = proxy as BluetoothHidDevice
                    currentProfile = "HID_DEVICE"
                    devicesList = device.connectedDevices
                }
                BluetoothProfile.HEARING_AID -> {
                    var device: BluetoothHearingAid = proxy as BluetoothHearingAid
                    currentProfile = "HEARING_AID"
                    devicesList = device.connectedDevices
                }
            }

            devicesList.forEach { device -> checkDevice(device, currentProfile) }
        }
    }

    // listen to connections and disconnections from bluetooth devices
    private var btDeviceReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when(intent?.action) {
                BluetoothDevice.ACTION_ACL_CONNECTED -> {
                    val device: BluetoothDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE) as BluetoothDevice
                    // add device
                    checkDevice(device, "NONE")
                }
                BluetoothDevice.ACTION_ACL_DISCONNECTED -> {
                    val device: BluetoothDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE) as BluetoothDevice
                    // remove device
                    removeDevice(device)
                }

            }
        }
    }

    private var scanReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent?) {
            when(intent?.action) {
                BluetoothDevice.ACTION_FOUND -> {
                    val device: BluetoothDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE) as BluetoothDevice
                    if (device.name != null && !nearDevices.contains(device.name)) nearDevices.add(device.name)
                }
            }
        }
    }

    override fun isAvailable(): Boolean {
        return adapter?.isEnabled != null
    }

    override fun start(onResult: (res: Pair<ArrayList<String>, ArrayList<String>>) -> Unit) {
        this.onResult = onResult
        this.run = true

        // register filters and receivers
        val ifScanReceiver = IntentFilter(BluetoothDevice.ACTION_FOUND)
        context.registerReceiver(scanReceiver, ifScanReceiver)
        val ifBtDeviceReceiver = IntentFilter(BluetoothDevice.ACTION_ACL_CONNECTED)
        ifBtDeviceReceiver.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED)
        context.registerReceiver(btDeviceReceiver, ifBtDeviceReceiver)

        adapter?.getProfileProxy(context, profileListener, BluetoothProfile.HEADSET)
        adapter?.getProfileProxy(context, profileListener, BluetoothProfile.HID_DEVICE)
        adapter?.getProfileProxy(context, profileListener, BluetoothProfile.HEARING_AID)
        adapter?.getProfileProxy(context, profileListener, BluetoothProfile.A2DP)

        scope.launch { loop() }
    }

    override fun stop() {
        run = false

        try {
            adapter?.cancelDiscovery()
            // unregister receivers
            context.unregisterReceiver(btDeviceReceiver)
            context.unregisterReceiver(scanReceiver)
        } catch (e: Exception) {
            Log.d("Error stopping Bluetooth Service:", e.toString())
        }
    }

    private suspend fun loop() {
        withContext(Dispatchers.IO) {
            while(run) {
                adapter?.startDiscovery()

                // get gattServer devices
                var gattServerList = manager.getConnectedDevices(BluetoothProfile.GATT_SERVER)
                gattServerList.forEach { device -> checkDevice(device, "GATT_SERVER")}

                Thread.sleep(minRefreshRate)
                if (nearDevices.isNotEmpty()) {
                    val connected = ArrayList<String>(connectedDevices.map { device -> device.name })
                    onResult(Pair(nearDevices, connected))
                    nearDevices.clear()
                }
            }
        }
    }


    // check if a device is already connected
    private fun isDeviceConnected(device: BluetoothDevice): Boolean {
        return connectedDevices.any { it.device.address == device.address }
    }

    private fun addProfile(id: String, pfl: String) {
        connectedDevices.forEach {
            if (it.id == id) it.addProfile(pfl)
        }
    }

    private fun checkDevice(device: BluetoothDevice, pfl: String){
        if (isDeviceConnected(device)) addProfile(device.address, pfl)
        else connectedDevices.add(BDevice(mutableListOf(pfl), device))
    }

    private fun removeDevice(device: BluetoothDevice){
        connectedDevices.forEach {
            if(it.device.address == device.address) connectedDevices.remove(it)
        }
    }
}