package labs.next.contextmeasurement

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View

import kotlinx.android.synthetic.main.activity_main.toggleService
import kotlinx.android.synthetic.main.activity_main.serviceStatus
import labs.next.contextmeasurement.modules.ForegroundService


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        checkService()
        addButtonListener()
    }

    fun addButtonListener() {
        toggleService.setOnClickListener(View.OnClickListener {
            if (ForegroundService.isRunning) ForegroundService.stop(this)
            else ForegroundService.start(this)
            checkService()
        })
    }

    fun checkService() {
        val text: Int = if (ForegroundService.isRunning) R.string.service_running else R.string.service_stopped
        serviceStatus.text = getString(text)
    }
}
