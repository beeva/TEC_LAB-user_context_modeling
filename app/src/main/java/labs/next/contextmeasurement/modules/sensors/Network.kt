package labs.next.contextmeasurement.modules.sensors

import android.util.Log
import android.content.Context
import android.net.Network
import android.net.ConnectivityManager
import kotlinx.coroutines.*


class Network(
    override var context: Context,
    override var minRefreshRate: Long = 5000
) : Sensor<String> {
    var CELL: String = "cellular"
    var WIFI: String = "wifi"

    private var run: Boolean = false
    private var scope: CoroutineScope = MainScope()
    private lateinit var callback: (String) -> Unit

    private val connectivityManager: ConnectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    private var networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            run = true
            scope.launch { loop() }
        }
    }

    override fun isAvailable(): Boolean {
        return true
    }

    override fun start(onResult: (String) -> Unit) {
        callback = onResult
        connectivityManager.registerDefaultNetworkCallback(networkCallback)
    }

    override fun stop() {
        run = false

        try {
            scope.cancel()
            connectivityManager.unregisterNetworkCallback(networkCallback)
        } catch (e: Exception) {
            Log.d("Wifi", e.toString())
        }
    }

    private suspend fun loop() {
        withContext(Dispatchers.IO) {
            while(run) {
                callback(if (connectivityManager.isActiveNetworkMetered) CELL else WIFI)
                Thread.sleep(minRefreshRate)
            }
        }
    }
}