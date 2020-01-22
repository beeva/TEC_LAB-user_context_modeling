package labs.next.argos.sensors

import android.util.Log
import android.content.Intent
import android.content.Context
import android.content.IntentFilter
import android.content.BroadcastReceiver
import android.net.wifi.WifiManager
import android.net.wifi.SupplicantState

import kotlinx.coroutines.*

class Wifi (
    override var context: Context,
    var location: Location,
    override var minRefreshRate: Long = 5000
) : Sensor<Pair<ArrayList<String>, String>> {
    private val connectedNetwork: String
        get (){
            val wifiInfo = wifiManager.connectionInfo;
            if (wifiInfo.supplicantState == SupplicantState.COMPLETED)
                return wifiInfo.ssid
            return "null"
        }

    private var run: Boolean = false
    private var scope: CoroutineScope = MainScope()
    private lateinit var callback: (Pair<ArrayList<String>, String>) -> Unit

    private var wifiManager: WifiManager = context.getSystemService(Context.WIFI_SERVICE) as WifiManager
    private var wifiScanReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val success = intent.getBooleanExtra(WifiManager.EXTRA_RESULTS_UPDATED, false)
            if (success) {
                var results: ArrayList<String> = ArrayList()
                wifiManager.scanResults.forEach{ network ->
                    if (
                        network.SSID != null &&
                        network.SSID != "" &&
                        !results.contains(network.SSID)
                    ) {
                        results.add(network.SSID)
                    }
                }

                callback(Pair(results, connectedNetwork))
            }
        }
    }

    override fun isAvailable(): Boolean {
        return wifiManager.isWifiEnabled
    }

    override fun start(onResult: (res: Pair<ArrayList<String>, String>) -> Unit) {
        callback = onResult
        run = true

        var intent = IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)
        context.registerReceiver(wifiScanReceiver, intent)

        scope.launch { loop() }
    }

    override fun stop() {
        run = false

        try {
            context.unregisterReceiver(wifiScanReceiver)
        } catch (e: Exception) {
            Log.d("Error stopping Wifi Service:", e.toString())
        }
    }

    private suspend fun loop() {
        withContext(Dispatchers.IO) {
            while(run) {
                while (isAvailable() && location.isAvailable() && !wifiManager.startScan()) {
                    Log.d("Wifi", "Retrying...")
                    Thread.sleep(5000)
                }

                Thread.sleep(minRefreshRate)
            }
        }
    }
}

