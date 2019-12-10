package labs.next.argos.sensors

import android.content.Context

interface Sensor<T> {
    var context: Context
    var minRefreshRate: Long
    fun isAvailable(): Boolean
    fun start(onResult: (res: T) -> Unit)
    fun stop()
}