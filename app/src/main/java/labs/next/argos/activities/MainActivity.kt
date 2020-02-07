package labs.next.argos.activities

import android.Manifest
import android.os.Bundle
import android.content.Intent

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import kotlinx.android.synthetic.main.toolbar.*

import labs.next.argos.R
import labs.next.argos.service.ServiceManager
import labs.next.argos.fragments.QuestionsFragment
import labs.next.argos.fragments.IncognitoModeViewFragment
import labs.next.argos.fragments.IncognitoModeBannerFragment
import labs.next.argos.fragments.RequiredPermissionsFragment
import labs.next.argos.libs.Auth
import labs.next.argos.libs.PermissionChecker
import labs.next.argos.libs.Utils

class MainActivity :
    FragmentActivity(),
    RequiredPermissionsFragment.PermissionListener,
    IncognitoModeBannerFragment.IncognitoModeListener
{
    private lateinit var serviceManager: ServiceManager
    private lateinit var requiredPermissions: HashMap<String, Int>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setActionBar(toolbar)
        info_button.setOnClickListener {
            val intent = Intent(this, InfoActivity::class.java)
            startActivity(intent)
        }

        Utils.initChannels(this)
        serviceManager = ServiceManager(this) {
            injectUI()
        }

        requiredPermissions = hashMapOf(
            Manifest.permission.BLUETOOTH to 1,
            Manifest.permission.BLUETOOTH_ADMIN to 1,
            Manifest.permission.ACCESS_FINE_LOCATION to 1,
            Manifest.permission.ACCESS_BACKGROUND_LOCATION to 29,
            Manifest.permission.ACTIVITY_RECOGNITION to 29,
            "com.google.android.gms.permission.ACTIVITY_RECOGNITION" to 29,
            Manifest.permission.ACCESS_NETWORK_STATE to 1,
            Manifest.permission.ACCESS_WIFI_STATE to 1,
            Manifest.permission.CHANGE_WIFI_STATE to 1
        )

        initialChecks()
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

    private fun initialChecks() {
        val (allGranted, _) = PermissionChecker.check(this, requiredPermissions)

        if (!allGranted) injectRequiredPermissions()
        else serviceManager.init()
    }

    private fun injectUI() {
        Auth.signIn(Utils(this).deviceID)

        injectIncognitoModeBanner(serviceManager.incognito)
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
            replace(
                R.id.main_placeholder,
                QuestionsFragment()
            )
            commit()
        }
    }
}
