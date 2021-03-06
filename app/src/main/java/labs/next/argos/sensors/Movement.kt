package labs.next.argos.sensors

import android.util.Log
import android.content.Context
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager

import kotlinx.coroutines.*

class Movement (
    override var context: Context,
    override var minRefreshRate: Long = 5000
) : Sensor<Boolean> {
    private var run: Boolean = false
    private var scope: CoroutineScope = MainScope()
    private var counter: Int = 0
    private var sensorManager: SensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager

    private lateinit var sensor: android.hardware.Sensor
    private lateinit var callback: (Boolean) -> Unit

    private var listener = object : SensorEventListener {
        override fun onAccuracyChanged(sensor: android.hardware.Sensor?, accuracy: Int) {}

        override fun onSensorChanged(event: SensorEvent) { counter++ }
    }

    override fun isAvailable(): Boolean {
        return sensorManager.getDefaultSensor(android.hardware.Sensor.TYPE_STEP_COUNTER) != null
    }

    override fun start(onResult: (res: Boolean) -> Unit) {
        counter = 0
        run = true
        callback = onResult

        sensor = sensorManager.getDefaultSensor(android.hardware.Sensor.TYPE_STEP_COUNTER)
        sensorManager.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_NORMAL)

        scope.launch { loop() }
    }

    override fun stop() {
        run = false

        try {
            sensorManager.unregisterListener(listener)
        } catch (e: Exception) {
            Log.d("Error stopping Bluetooth Service:", e.toString())
        }
    }

    private suspend fun loop() {
        withContext(Dispatchers.IO) {
            while (run) {
                callback(counter >= 10)
                counter = 0
                Thread.sleep(minRefreshRate)
            }
        }
    }
}