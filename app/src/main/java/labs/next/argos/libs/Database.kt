package labs.next.argos.libs

import android.util.Log

import com.google.firebase.database.ServerValue
import com.google.firebase.database.FirebaseDatabase

class Database (
    var user: String,
    var online: Boolean = true
) {
    private var instance: FirebaseDatabase = FirebaseDatabase.getInstance("https://next-lab-usercontext-poc-es-4ba7d.firebaseio.com/")

    private fun saveMetric(
        source: ContextSource,
        sensor: ContextSensor?,
        metric: String,
        value: Any
    ) {
        val model = hashMapOf(
            "source" to source.name,
            "sensor" to sensor?.name,
            "metric" to metric,
            "value" to value,
            "timestamp" to ServerValue.TIMESTAMP
        )

        if (online) {
            val entry = instance.getReference("data/$user").push()
            entry.setValue(model)
        } else Log.d("Database offline insert", "$sensor ($source) -> $metric: $value")
    }

    fun saveUserAnswer(metric: String, value: Any) {
        saveMetric(
            ContextSource.USER,
            ContextSensor.ACTIVITY, metric, value)
    }

    fun saveBattery(metric: String, value: Any) {
        saveMetric(
            ContextSource.SENSOR,
            ContextSensor.BATTERY, metric, value)
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

    fun saveWifi(metric: String, value: Any) {
        saveMetric(
            ContextSource.SENSOR,
            ContextSensor.WIFI, metric, value)
    }

    fun saveMovement(metric: String, value: Any) {
        saveMetric(
            ContextSource.SENSOR,
            ContextSensor.MOVEMENT, metric, value)
    }

    fun saveIncognito(value: Boolean) {
        saveMetric(
            ContextSource.USER,
            ContextSensor.INCOGNITO, "toggle_to", value)
    }

    fun saveDeviceModel(model: String) {
        if (online) instance.getReference("info/$user/model").setValue(model)
    }

    fun saveUserToken(token: String) {
        if (online) instance.getReference("info/$user/token").setValue(token)
    }

    fun saveAuthID(authID: String) {
        if (online) instance.getReference("info/$user/authID").setValue(authID)
    }
}