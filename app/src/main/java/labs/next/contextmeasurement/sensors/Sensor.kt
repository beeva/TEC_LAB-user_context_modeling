package labs.next.contextmeasurement.modules.sensors

import android.content.Context

interface Sensor<T> {
    var context: Context
    var minRefreshRate: Long
    var permissions: Array<String>
    fun isAvailable(): Boolean
    fun start(onResult: (res: T) -> Unit)
    fun stop()
}