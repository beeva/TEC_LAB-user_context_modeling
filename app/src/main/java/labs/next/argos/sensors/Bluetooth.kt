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
    var connectedDevices =  arrayListOf<BDevice>()

    private var run: Boolean = false
    private var scope: CoroutineScope = MainScope()
    private var results: ArrayList<String?> = ArrayList()
    private lateinit var onResult: (ArrayList<String?>) -> Unit

    private var manager: BluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    private var adapter: BluetoothAdapter? = manager.adapter


    // listen to connections and disconnections from bluetooth service
    private val profileListener = object : BluetoothProfile.ServiceListener {
        override fun onServiceDisconnected(profile: Int) {
            //connectionType = null
            //isConnected = false
            //connectedDevices.add()
            //Log.d(TAG, "onServiceDisconnected")
            // removes all elements
            connectedDevices.clear()
        }

        override fun onServiceConnected(profile: Int, proxy: BluetoothProfile?) {
            //Log.d(TAG, "onServiceConnected")
            if (profile == BluetoothProfile.A2DP){
                //Log.d(TAG, "A2DP")
                var device: BluetoothA2dp = proxy as BluetoothA2dp
                val devices_list: MutableList<BluetoothDevice> = device.connectedDevices
                checkDevices(devices_list, "A2DP")
                //devices_list.forEach { Log.d(TAG, "found: " + it.name) }

                //close proxy
                //adapter.closeProfileProxy(BluetoothProfile.A2DP, device)
            }
            if (profile == BluetoothProfile.HEADSET){
                //Log.d(TAG, "HEADSET")
                var device: BluetoothHeadset = proxy as BluetoothHeadset
                val devices_list: MutableList<BluetoothDevice> = device.connectedDevices
                checkDevices(devices_list, "HEADSET")
                //devices_list.forEach { Log.d(TAG, "found: " + it.name) }

                //close proxy
                //adapter.closeProfileProxy(BluetoothProfile.HEADSET, device)

            }
            if (profile == BluetoothProfile.HID_DEVICE){
                //Log.d(TAG, "HID_DEVICE")
                var device: BluetoothHidDevice = proxy as BluetoothHidDevice
                val devices_list: MutableList<BluetoothDevice> = device.connectedDevices
                checkDevices(devices_list, "HID_DEVICE")
                //devices_list.forEach { Log.d(TAG, "found: " + it.name) }
                //close proxy
                //adapter.closeProfileProxy(BluetoothProfile.HID_DEVICE, device)

            }
            if (profile == BluetoothProfile.HEARING_AID){
                //Log.d(TAG, "HEARING_AID")
                var device: BluetoothHearingAid = proxy as BluetoothHearingAid
                val devices_list: MutableList<BluetoothDevice> = device.connectedDevices
                checkDevices(devices_list, "HEARING_AID")
                //devices_list.forEach { Log.d(TAG, "found: " + it.name) }
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
                    //Log.d(TAG, "ONCONNECTED")
                    //Log.d(TAG, "found: " + device.name)
                    // add device
                    checkDevice(device, "NONE")
                    //connectedDevices?.forEach{Log.d(TAG, "found: " + it.name)}
                }
                BluetoothDevice.ACTION_ACL_DISCONNECTED -> {
                    val device: BluetoothDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE) as BluetoothDevice
                    //Log.d(TAG, "ONDISCONNECTED")
                    //Log.d(TAG, "found: " + device.name)
                    // remove device
                    removeDevice(device)
                    //connectedDevices?.forEach{Log.d(TAG, "found: " + it.name)}
                }

            }
        }
    }

    fun getConnDevices(): ArrayList<Map<String, String>> {
        var devices =  arrayListOf<Map<String, String>>()
        connectedDevices.forEach {
            var map = hashMapOf<String, String>("name" to it.name, "types" to it.profile.toString())
            devices.add(map)
        }
        return devices
    }

    private var scanReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent?) {

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
        return adapter?.isEnabled != null
    }

    override fun start(onResult: (res: ArrayList<String?>) -> Unit) {
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

                // get gatt_server devices
                var gattServer_list = manager.getConnectedDevices(BluetoothProfile.GATT_SERVER)
                checkDevices(gattServer_list, "GATT_SERVER")
                var gatt_list = manager.getConnectedDevices(BluetoothProfile.GATT)
                checkDevices(gatt_list, "GATT")
                Log.d(TAG, "onLoop")
                connectedDevices?.forEach { Log.d(TAG, "loop: " + it.name) }

                Thread.sleep(minRefreshRate)
                if (results.isNotEmpty()) {
                    onResult(results)
                    results.clear()
                }
            }
        }
    }


    // check if a device is already connected
    private fun isDeviceConnected(device: BluetoothDevice): Boolean {
        var isConnected: Boolean = false
        connectedDevices.forEach {
            if (it.device.address.equals(device.address)){
                isConnected = true
            }
        }
        return isConnected
    }

    private fun addProfile(id: String, pfl: String){
        connectedDevices.forEach {
            if(it.id.equals(id)){
                it.addProfile(pfl)
            }
        }
    }

    private fun checkDevices(devices_list: MutableList<BluetoothDevice>, pfl: String){
        devices_list.forEach {
            if (isDeviceConnected(it)) {
                addProfile(it.address, pfl)
            }else{
                var bdevice = BDevice(mutableListOf(pfl), it)
                connectedDevices.add(bdevice)
            }
        }
    }

    private fun checkDevice(device: BluetoothDevice, pfl: String){
        if (isDeviceConnected(device)) {
            addProfile(device.address, pfl)
        }else{
            var bdevice = BDevice(mutableListOf(pfl), device)
            connectedDevices.add(bdevice)
        }
    }

    private fun removeDevice(device: BluetoothDevice){
        connectedDevices.forEach { if(it.device.address.equals(device.address)){ connectedDevices.remove(it)} }
    }
}