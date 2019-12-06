package labs.next.contextmeasurement

import android.content.ComponentName
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import labs.next.contextmeasurement.modules.services.ForegroundService

import kotlinx.android.synthetic.main.activity_main.toggleService
import kotlinx.android.synthetic.main.activity_main.serviceStatus


class MainActivity : AppCompatActivity() {
    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as ForegroundService.ServiceBinder
            var current: ForegroundService = binder.getService()

            checkPermissions(current.permissions)
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            Log.d("disconnected", "Bye")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        checkService()
        addButtonListener()
    }

    override fun onDestroy() {
        super.onDestroy()

        unbindService(serviceConnection)
    }

    private fun checkPermissions(permissions: Array<String>) {
        permissions.forEach { permission ->
            Log.d("Permission needed", permission)
            if (ContextCompat.checkSelfPermission(this, permission)
                != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(permission), 1)
            } else Log.d("Permission granted", permission)
        }
    }

    private fun addButtonListener() {
        toggleService.setOnClickListener {
            if (ForegroundService.isRunning) ForegroundService.stop(this, serviceConnection)
            else ForegroundService.start(this, serviceConnection)
            checkService()
        }
    }

    private fun checkService() {
        val text: Int = if (ForegroundService.isRunning) R.string.service_running else R.string.service_stopped
        serviceStatus.text = getString(text)
    }
}
