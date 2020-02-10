package labs.next.argos.sensors

import android.util.Log
import android.net.Network
import android.content.Context
import android.net.ConnectivityManager

import kotlinx.coroutines.*

const val CELL: String = "cellular"
const val WIFI: String = "wifi"

class Network(
    override var context: Context,
    override var minRefreshRate: Long = 5000
) : Sensor<String> {
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
            connectivityManager.unregisterNetworkCallback(networkCallback)
        } catch (e: Exception) {
            Log.d("Error stopping Network Service:", e.toString())
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