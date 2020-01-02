package labs.next.argos.sensors

import android.util.Log
import android.content.Intent
import android.content.Context
import android.content.IntentFilter
import android.content.BroadcastReceiver
import android.os.BatteryManager
import kotlinx.coroutines.*

class Battery(
    override var context: Context,
    override var minRefreshRate: Long
) : Sensor<Pair<Boolean, Float>> {
    private var run: Boolean = false
    private var isCharging: Boolean = false
    private var currentLevel: Float = 0F
    private var scope: CoroutineScope = MainScope()
    private lateinit var callback: (Pair<Boolean, Float>) -> Unit

    private var batteryReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1).toFloat()
            val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1).toFloat()
            currentLevel = level / scale * 100F

            val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
            isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING
        }
    }

    override fun isAvailable(): Boolean { return true }

    override fun start(onResult: (res: Pair<Boolean, Float>) -> Unit) {
        run = true
        callback = onResult

        val intent = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        context.registerReceiver(batteryReceiver, intent)

        scope.launch { loop() }
    }

    override fun stop() {
        run = false

        try {
            context.unregisterReceiver(batteryReceiver)
        } catch (e: Exception) {
            Log.d("Error stopping Battery Service:", e.toString())
        }
    }

    private suspend fun loop() {
        withContext(Dispatchers.IO) {
            while (run) {
                callback(Pair(isCharging, currentLevel))
                Thread.sleep(minRefreshRate)
            }
        }
    }
}