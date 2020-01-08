package labs.next.argos.service

import java.util.concurrent.TimeUnit

import android.os.IBinder
import android.content.Context
import android.content.ComponentName
import android.content.ServiceConnection

import labs.next.argos.libs.Utils

class ServiceManager {
    var incognito: Boolean = true
    private var utils: Utils
    private var context: Context

    private var incognitoModeDelay = TimeUnit.HOURS.toMillis(8)
    private var initCallback: ((Boolean) -> Unit)? = null
    private var listenCallback : ((Boolean) -> Unit)? = null
    private lateinit var foregroundService: ForegroundService

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as ForegroundService.ServiceBinder
            foregroundService = binder.getService()
            serviceStarted()
        }

        override fun onServiceDisconnected(name: ComponentName?) {}
    }

    constructor(ctx : Context, onInit: ((Boolean) -> Unit)? = null) {
        context = ctx
        utils = Utils(context)
        initCallback = onInit
    }

    private fun serviceStarted() {
        foregroundService.onListenChange{ listening ->
            incognito = !listening
            if (listenCallback != null) listenCallback?.invoke(incognito)
        }

        incognito = !foregroundService.isListening
        if (utils.firstBoot) foregroundService.startListening()
        initCallback?.let { it(incognito) }
    }

    fun init() {
        if (!ForegroundService.isAlive) ForegroundService.init(context, serviceConnection, false)
        else ForegroundService.connect(context, serviceConnection)
    }

    fun close() {
        ForegroundService.disconnect(context, serviceConnection)
    }

    fun onListening(callback : (Boolean) -> Unit) {
        this.listenCallback = callback
    }

    fun incognitoMode(enabled: Boolean) : Long? {
        return if (enabled) {
            utils.incognitoDelay = foregroundService.stopListening(incognitoModeDelay)
            utils.incognitoDelay
        } else {
            foregroundService.startListening()
            null
        }
    }
}