package labs.next.argos.sensors

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import kotlinx.coroutines.*

class Bluetooth (
    override var context: Context,
    override var minRefreshRate: Long = 5000
) : Sensor<ArrayList<String?>> {
    var MEDIA: String = "media"
    var OTHER: String = "other"
    var connectionType: String? = null
    var isConnected: Boolean = false

    private var run: Boolean = false
    private var scope: CoroutineScope = MainScope()
    private var results: ArrayList<String?> = ArrayList()
    private lateinit var onResult: (ArrayList<String?>) -> Unit

    private var manager: BluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    private var adapter: BluetoothAdapter = manager.adapter
    private val profileListener = object : BluetoothProfile.ServiceListener {
        override fun onServiceDisconnected(profile: Int) {
            connectionType = null
            isConnected = false
        }

        override fun onServiceConnected(profile: Int, proxy: BluetoothProfile?) {
            isConnected = true
            connectionType = when(profile) {
                BluetoothProfile.HEADSET -> MEDIA
                BluetoothProfile.A2DP -> MEDIA
                else -> OTHER
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

        val intent = IntentFilter(BluetoothDevice.ACTION_FOUND)
        context.registerReceiver(scanReceiver, intent)

        /*
        adapter.getProfileProxy(context, profileListener, BluetoothProfile.HEADSET)
        adapter.getProfileProxy(context, profileListener, BluetoothProfile.A2DP)
        adapter.getProfileProxy(context, profileListener, BluetoothProfile.GATT)
        */

        scope.launch { loop() }
    }

    override fun stop() {
        run = false

        try {
            adapter.cancelDiscovery()
            context.unregisterReceiver(scanReceiver)
        } catch (e: Exception) {
            Log.d("Error stopping Bluetooth Service:", e.toString())
        }
    }

    private suspend fun loop() {
        withContext(Dispatchers.IO) {
            while(run) {
                adapter.startDiscovery()
                Thread.sleep(minRefreshRate)
                if (results.isNotEmpty()) {
                    onResult(results)
                    results.clear()
                }
            }
        }
    }
}