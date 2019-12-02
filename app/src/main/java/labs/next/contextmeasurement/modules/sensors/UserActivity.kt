package labs.next.contextmeasurement.modules.sensors

import android.app.IntentService
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.android.gms.location.*
import com.google.android.gms.location.ActivityRecognitionResult
import kotlinx.coroutines.*


class UserActivity(
    override var context: Context,
    override var minRefreshRate: Long = 5000
) : Sensor<String>, IntentService("UserActivity") {
    private var run: Boolean = false
    private var scope: CoroutineScope = MainScope()
    private lateinit var callback: (String) -> Unit

    private var recognitionClient: ActivityRecognitionClient = ActivityRecognitionClient(context)
    private var transitionReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {

        }
    }

    override fun isAvailable(): Boolean { return true }

    override fun start(onResult: (res: String) -> Unit) {
        callback = onResult
        run = true

        scope.launch { loop() }
    }

    override fun stop() {
        run = false

        try {
            scope.cancel()
        } catch (e: Exception) {
            Log.d("Error stopping UserActivity Service:", e.toString())
        }
    }

    private suspend fun loop() {
        withContext(Dispatchers.IO) {
            while (run) {
                var intent = createIntent()
                var request = createRequest()
                var task = ActivityRecognition.getClient(context)
                    .requestActivityTransitionUpdates(request, intent)

                Thread.sleep(minRefreshRate)
            }
        }
    }

    private fun createIntent() : PendingIntent {
        val intent = Intent(context, UserActivity::class.java)
        return PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
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

    override fun onHandleIntent(intent: Intent?) {
        if (ActivityRecognitionResult.hasResult(intent)) {
            val result = ActivityRecognitionResult.extractResult(intent)
            Log.d("Activity", result.probableActivities.toString())
        }
    }
}