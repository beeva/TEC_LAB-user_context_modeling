package labs.next.contextmeasurement.modules.sensors

import android.Manifest
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.android.gms.location.*
import kotlinx.coroutines.*

class UserActivity(
    override var context: Context,
    override var minRefreshRate: Long = 5000,
    override var permissions: Array<String> = arrayOf(
        Manifest.permission.ACTIVITY_RECOGNITION,
        "com.google.android.gms.permission.ACTIVITY_RECOGNITION"
    )
) : Sensor<String?> {
    var VEHICLE = "vehicle"
    var WALKING = "walking"
    var STILL = "still"

    private var run: Boolean = false
    private var scope: CoroutineScope = MainScope()
    private var current: String? = null
    private lateinit var callback: (String?) -> Unit
    private lateinit var intent: PendingIntent

    private var recognitionClient: ActivityRecognitionClient = ActivityRecognitionClient(context)
    private var transitionReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (ActivityRecognitionResult.hasResult(intent)) {
                val result = ActivityRecognitionResult.extractResult(intent)
                result.probableActivities.forEach { activity ->
                    current = when(activity.type) {
                        DetectedActivity.IN_VEHICLE,
                        DetectedActivity.ON_BICYCLE -> VEHICLE
                        DetectedActivity.ON_FOOT -> WALKING
                        DetectedActivity.STILL -> STILL
                        else -> null
                    }
                }
            }
        }
    }

    override fun isAvailable(): Boolean { return true }

    override fun start(onResult: (res: String?) -> Unit) {
        run = true
        callback = onResult

        intent = createIntent()
        val request = createRequest()
        recognitionClient.requestActivityTransitionUpdates(request, intent)
            .addOnFailureListener { e ->
                Log.d("Error stopping UserActivity Service...", e.toString())
            }

        scope.launch { loop() }
    }

    override fun stop() {
        run = false
        scope.cancel()

        recognitionClient.removeActivityTransitionUpdates(intent)
            .addOnFailureListener { e ->
                Log.d("Error stopping UserActivity Service...", e.toString())
            }
    }

    private fun createIntent() : PendingIntent {
        val intent = Intent(context, transitionReceiver::class.java)
        return PendingIntent.getBroadcast(context, 0, intent, 0)
    }

    private fun createRequest() : ActivityTransitionRequest {
        val types = listOf(
            DetectedActivity.IN_VEHICLE,
            DetectedActivity.ON_FOOT,
            DetectedActivity.ON_BICYCLE,
            DetectedActivity.STILL
        )

        val transitions = mutableListOf<ActivityTransition>()
        types.forEach { activity ->
            transitions.add(
                ActivityTransition.Builder()
                    .setActivityType(activity)
                    .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_ENTER)
                    .build()
            )
        }

        return ActivityTransitionRequest(transitions)
    }

    private suspend fun loop() {
        withContext(Dispatchers.IO) {
            while (run) {
                callback(current)
                Thread.sleep(minRefreshRate)
            }
        }
    }
}