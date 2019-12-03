package labs.next.contextmeasurement.modules.sensors

import android.app.usage.UsageStatsManager
import android.content.Context
import kotlinx.coroutines.*

class UsageStats (
    override var context: Context,
    override var minRefreshRate: Long = 5000,
    var statsInterval: Long = 60000
) : Sensor<HashMap<String, Long>> {
    private var run: Boolean = false
    private var scope: CoroutineScope = MainScope()
    private lateinit var callback: (HashMap<String, Long>) -> Unit

    private var manager: UsageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager

    override fun isAvailable(): Boolean { return true }

    override fun start(onResult: (res: HashMap<String, Long>) -> Unit) {
        run = true
        callback = onResult

        scope.launch { loop() }
    }

    override fun stop() {
        run = false
        scope.cancel()
    }

    private suspend fun loop() {
        withContext(Dispatchers.IO) {
            while (run) {
                val latest = currentStats()
                callback(latest)
                Thread.sleep(minRefreshRate)
            }
        }
    }

    private fun currentStats(): HashMap<String, Long> {
        val end = System.currentTimeMillis()
        val start = end - statsInterval

        var stats: HashMap<String, Long> = HashMap()
        manager.queryUsageStats(UsageStatsManager.INTERVAL_BEST, start, end)
            .forEach { stat ->
                stats[stat.packageName] = stat.lastTimeStamp
            }

        return stats
    }
}