package labs.next.contextmeasurement.modules

import com.google.firebase.database.FirebaseDatabase

enum class DatabaseSource { USER, SENSOR }

enum class DatabaseSensor { BLUETOOTH, LOCATION, NETWORK, USAGE, ACTIVITY, WIFI }

class Database (var user: String) {
    private var instance: FirebaseDatabase = FirebaseDatabase.getInstance()

    private fun saveMetric(
        source: DatabaseSource,
        sensor: DatabaseSensor,
        metric: String,
        value: Any
    ) {
        var model = object {
            var source = source.name
            var sensor = sensor.name
            var metric = metric
            var value = value
        }

        var entry = instance.getReference("$user/metrics").push()
        entry.setValue(model)
    }

    fun saveBluetooth(metric: String, value: Any) {
        saveMetric(DatabaseSource.SENSOR, DatabaseSensor.BLUETOOTH, metric, value)
    }

    fun saveLocation(metric: String, value: Any) {
        saveMetric(DatabaseSource.SENSOR, DatabaseSensor.LOCATION, metric, value)
    }

    fun saveNetwork(metric: String, value: Any) {
        saveMetric(DatabaseSource.SENSOR, DatabaseSensor.NETWORK, metric, value)
    }

    fun saveUsage(metric: String, value: Any) {
        saveMetric(DatabaseSource.SENSOR, DatabaseSensor.USAGE, metric, value)
    }

    fun saveActivity(metric: String, value: Any) {
        saveMetric(DatabaseSource.SENSOR, DatabaseSensor.ACTIVITY, metric, value)
    }

    fun saveWifi(metric: String, value: Any) {
        saveMetric(DatabaseSource.SENSOR, DatabaseSensor.WIFI, metric, value)
    }

    fun setValue(ref: String, value: Any) {
        instance.getReference("$user/$ref").setValue(value)
    }
}