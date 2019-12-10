package labs.next.argos.libs

import android.util.Log
import com.google.firebase.database.FirebaseDatabase

class Database (var user: String, var online: Boolean = true) {
    private var instance: FirebaseDatabase = FirebaseDatabase.getInstance()

    private fun saveMetric(
        source: ContextSource,
        sensor: ContextSensor?,
        metric: String,
        value: Any
    ) {
        val model = object {
            val source = source.name
            val sensor = sensor?.name
            val metric = metric
            val value = value
        }

        if (online) {
            val entry = instance.getReference("$user/metrics").push()
            entry.setValue(model)
        } else Log.d("Database offline insert", "$sensor ($source) -> $metric: $value")
    }

    fun saveUserAnswer(metric: String, value: Any) {
        saveMetric(
            ContextSource.USER,
            null, metric, value)
    }

    fun saveBluetooth(metric: String, value: Any) {
        saveMetric(
            ContextSource.SENSOR,
            ContextSensor.BLUETOOTH, metric, value)
    }

    fun saveLocation(metric: String, value: Any) {
        saveMetric(
            ContextSource.SENSOR,
            ContextSensor.LOCATION, metric, value)
    }

    fun saveNetwork(metric: String, value: Any) {
        saveMetric(
            ContextSource.SENSOR,
            ContextSensor.NETWORK, metric, value)
    }

    fun saveUsage(metric: String, value: Any) {
        saveMetric(
            ContextSource.SENSOR,
            ContextSensor.USAGE, metric, value)
    }

    fun saveActivity(metric: String, value: Any) {
        saveMetric(
            ContextSource.SENSOR,
            ContextSensor.ACTIVITY, metric, value)
    }

    fun saveWifi(metric: String, value: Any) {
        saveMetric(
            ContextSource.SENSOR,
            ContextSensor.WIFI, metric, value)
    }

    fun setValue(ref: String, value: Any) {
        if (online) instance.getReference("$user/$ref").setValue(value)
    }
}