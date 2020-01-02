package labs.next.argos.sensors

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import com.google.android.gms.location.*
import kotlinx.coroutines.*

class UserActivity(
    override var context: Context,
    override var minRefreshRate: Long = 5000
) : Sensor<String?> {
    var VEHICLE = "vehicle"
    var WALKING = "walking"
    var STILL = "still"
    var UNKNOWN = "unknown"

    private var run: Boolean = false
    private var scope: CoroutineScope = MainScope()
    private var current: String? = null
    private lateinit var callback: (String?) -> Unit
    private lateinit var intent: PendingIntent
    private lateinit var recognitionClient: ActivityRecognitionClient

    private var recognitionHandler = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (ActivityRecognitionResult.hasResult(intent)) {
                val result = ActivityRecognitionResult.extractResult(intent)

                val activity = result.mostProbableActivity
                current = when(activity.type) {
                    DetectedActivity.IN_VEHICLE,
                    DetectedActivity.ON_BICYCLE -> VEHICLE
                    DetectedActivity.WALKING,
                    DetectedActivity.RUNNING -> WALKING
                    DetectedActivity.STILL -> STILL
                    else -> UNKNOWN
                }
            }
        }
    }

    override fun isAvailable(): Boolean { return true }

    override fun start(onResult: (res: String?) -> Unit) {
        callback = onResult

        intent = createIntent()
        val request = createRequest()
        recognitionClient = ActivityRecognition.getClient(context)

        recognitionClient.requestActivityTransitionUpdates(request, intent)
            .addOnSuccessListener {
                run = true
                Log.d("UserActivity Service info", "listening...")
            }
            .addOnFailureListener { e ->
                run = false
                Log.d("UserActivity Service error", e.toString())
            }

        scope.launch { loop() }
    }

    override fun stop() {
        recognitionClient.removeActivityTransitionUpdates(intent)
            .addOnSuccessListener {
                run = false
            }
            .addOnFailureListener { e ->
                Log.d("UserActivity Service error", e.toString())
            }
    }

    private fun createIntent() : PendingIntent {
        val intent = Intent(context, recognitionHandler::class.java)

        context.registerReceiver(recognitionHandler, IntentFilter())
        return PendingIntent.getBroadcast(context, 1, intent, 0)
    }

    private fun createRequest() : ActivityTransitionRequest {
        val types = listOf(
            DetectedActivity.IN_VEHICLE,
            DetectedActivity.WALKING,
            DetectedActivity.RUNNING,
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