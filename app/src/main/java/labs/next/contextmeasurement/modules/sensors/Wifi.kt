package labs.next.contextmeasurement.modules.sensors

import android.util.Log
import android.content.Intent
import android.content.Context
import android.content.IntentFilter
import android.content.BroadcastReceiver
import android.net.wifi.ScanResult
import android.net.wifi.WifiManager
import android.net.wifi.SupplicantState
import kotlinx.coroutines.*

class Wifi (
    override var context: Context,
    override var minRefreshRate: Long = 5000
) : Sensor<ArrayList<String>> {
    private var run: Boolean = false
    private var scope: CoroutineScope = MainScope()

    val connectedNetwork: String
        get() {
            val wifiInfo = wifiManager.getConnectionInfo();
            if (wifiInfo.getSupplicantState() == SupplicantState.COMPLETED) {
                return wifiInfo.getSSID()
            }

            return ""
        }

    private var wifiManager: WifiManager = context.getSystemService(Context.WIFI_SERVICE) as WifiManager
    private var wifiScanReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val success = intent.getBooleanExtra(WifiManager.EXTRA_RESULTS_UPDATED, false)
            if (success) {
                val results: ArrayList<String> = ArrayList()
                val networks = wifiManager.scanResults as ArrayList<ScanResult>
                for (network in networks) {
                    results.add(network.SSID)
                }

                onResult(results)
            }
        }
    }

    lateinit var onResult: (ArrayList<String>) -> Unit

    override fun isAvailable(): Boolean {
        return wifiManager.isWifiEnabled
    }

    override fun start(onResult: (ArrayList<String>) -> Unit) {
        this.onResult = onResult
        this.run = true

        var intent = IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)
        context.registerReceiver(wifiScanReceiver, intent)

        scope.launch { loop() }
    }

    override fun stop() {
        run = false

        try {
            scope.cancel()
            context.unregisterReceiver(wifiScanReceiver)
        } catch (e: Exception) {
            Log.d("Wifi", e.toString())
        }
    }

    private suspend fun loop() {
        withContext(Dispatchers.IO) {
            while(run) {
                val success = wifiManager.startScan()
                if (!success) onResult(ArrayList())

                Thread.sleep(minRefreshRate)
            }
        }
    }
}

