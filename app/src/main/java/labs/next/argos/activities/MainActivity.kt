package labs.next.argos.activities

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.util.Log

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import kotlinx.android.synthetic.main.toolbar.*

import labs.next.argos.R
import labs.next.argos.service.ServiceManager
import labs.next.argos.fragments.QuestionsFragment
import labs.next.argos.fragments.IncognitoModeViewFragment
import labs.next.argos.fragments.IncognitoModeBannerFragment
import labs.next.argos.fragments.RequiredPermissionsFragment

class MainActivity :
    FragmentActivity(),
    RequiredPermissionsFragment.PermissionListener,
    IncognitoModeBannerFragment.IncognitoModeListener
{
    private lateinit var serviceManager: ServiceManager
    private lateinit var requiredPermissions: Array<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setActionBar(toolbar)
        info_button.setOnClickListener {
            val intent = Intent(this, InfoActivity::class.java)
            startActivity(intent)
        }

        serviceManager = ServiceManager(this) {
            injectOnServiceFragments()
        }

        requiredPermissions = arrayOf(
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_BACKGROUND_LOCATION,
            Manifest.permission.ACTIVITY_RECOGNITION,
            "com.google.android.gms.permission.ACTIVITY_RECOGNITION",
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.ACCESS_WIFI_STATE,
            Manifest.permission.CHANGE_WIFI_STATE
        )

        injectInitialFragments()
    }

    override fun onAttachFragment(fragment: Fragment) {
        when (fragment) {
            is RequiredPermissionsFragment -> {
                fragment.setPermissionListener(this)
            }
            is IncognitoModeBannerFragment -> {
                fragment.setIncognitoModeListener(this)
                fragment.updateMode(serviceManager.incognito)
            }
        }
    }

    override fun onPermissionsGranted() {
        serviceManager.init()
    }

    override fun onIncognitoModeAttached(fragment: Fragment) {
        serviceManager.onListening { incognito ->
            (fragment as IncognitoModeBannerFragment).updateMode(incognito)
        }
    }

    override fun onIncognitoModeChanged(status: Boolean) {
        val delay = serviceManager.incognitoMode(status)

        if (serviceManager.incognito && delay != null) injectIncognitoModeFragment()
        else injectQuestionsFragment()
    }


    override fun onDestroy() {
        super.onDestroy()

        serviceManager.close()
    }

    private fun injectInitialFragments() {
        injectIncognitoModeBanner(false)
        injectRequiredPermissions()
    }

    private fun injectOnServiceFragments() {
        injectIncognitoModeBanner(true)
        if (serviceManager.incognito) injectIncognitoModeFragment()
        else injectQuestionsFragment()
    }

    private fun injectRequiredPermissions() {
        with(supportFragmentManager.beginTransaction()) {
            setCustomAnimations(R.anim.fade_in, R.anim.fade_out)
            replace(R.id.main_placeholder,
                RequiredPermissionsFragment(requiredPermissions)
            )
            commit()
        }
    }

    private fun injectIncognitoModeBanner(enabled: Boolean) {
        with(supportFragmentManager.beginTransaction()) {
            setCustomAnimations(R.anim.fade_in, R.anim.fade_out)
            replace(R.id.incognito_placeholder,
                IncognitoModeBannerFragment(serviceManager.incognito, enabled)
            )
            commit()
        }
    }

    private fun injectIncognitoModeFragment() {
        with(supportFragmentManager.beginTransaction()) {
            setCustomAnimations(R.anim.fade_in, R.anim.fade_out)
            replace(R.id.main_placeholder,
                IncognitoModeViewFragment()
            )
            commit()
        }
    }

    private fun injectQuestionsFragment() {
        with(supportFragmentManager.beginTransaction()) {
            setCustomAnimations(R.anim.fade_in, R.anim.fade_out)
            replace(R.id.main_placeholder,
                QuestionsFragment()
            )
            commit()
        }
    }
}
