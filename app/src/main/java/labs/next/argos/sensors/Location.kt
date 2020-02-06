package labs.next.argos.sensors

import android.util.Log
import android.content.Context
import android.location.LocationManager

import com.google.android.gms.location.*

import kotlinx.coroutines.*

class Location(
    override var context: Context,
    override var minRefreshRate: Long = 5000
) : Sensor<Map<String, Double>?> {
    private var run: Boolean = false
    private var scope: CoroutineScope = MainScope()
    private var results: HashMap<String, Double>? = null
    private var manager: LocationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    private var fusedClient: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)
    private lateinit var callback: (HashMap<String, Double>?) -> Unit

    private var onChange = object : LocationCallback() {
        override fun onLocationResult(locations: LocationResult) {
            val location = locations.lastLocation
            results = hashMapOf(
                "lat" to location.latitude,
                "long" to location.longitude
            )
        }
    }

    override fun isAvailable(): Boolean {
        try {
            val gps = manager.isProviderEnabled(LocationManager.GPS_PROVIDER)
            val net = manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

            return gps or net
        } catch (e: Exception) {
            Log.d("Error with Location Service", e.toString())
        }

        return false
    }

    override fun start(onResult: (res: Map<String, Double>?) -> Unit) {
        run = true
        callback = onResult


        fusedClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                results = hashMapOf(
                    "lat" to location.latitude,
                    "long" to location.longitude
                )
            }
        }

        val request = createRequest()
        fusedClient.requestLocationUpdates(request, onChange, null)

        scope.launch { loop() }
    }

    override fun stop() {
        run = false

        try {
            fusedClient.removeLocationUpdates(onChange)
        } catch (e: Exception) {
            Log.d("Error stopping Location Service", e.toString())
        }
    }

    private suspend fun loop() {
        withContext(Dispatchers.IO) {
            while (run) {
                if (!isAvailable()){
                    results = hashMapOf(
                        "lat" to 0.0,
                        "long" to 0.0
                    )
                }
                callback(results)
                Thread.sleep(minRefreshRate)
            }
        }
    }

    private fun createRequest(): LocationRequest {
        val locationRequest = LocationRequest.create()
        locationRequest.interval = this.minRefreshRate * 2
        locationRequest.fastestInterval = this.minRefreshRate
        locationRequest.priority = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY

        return locationRequest
    }
}