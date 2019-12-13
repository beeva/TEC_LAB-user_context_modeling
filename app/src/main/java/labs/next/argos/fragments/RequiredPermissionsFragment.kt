package labs.next.argos.fragments

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.LayoutInflater
import android.content.pm.PackageManager

import androidx.fragment.app.Fragment

import kotlinx.android.synthetic.main.fragment_required_permissions.*

import labs.next.argos.R
import labs.next.argos.libs.PermissionChecker

class RequiredPermissionsFragment(
    var permissions: HashMap<String, Int>,
    var requestCode: Int = 14
) : Fragment() {
    private lateinit var toRequest: Array<String>
    private lateinit var callback: PermissionListener

    interface PermissionListener {
        fun onPermissionsGranted()
    }

    fun setPermissionListener(callback: PermissionListener) {
        this.callback = callback
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_required_permissions, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setCheckButtonListener()

        val (allGranted, unauthorized) = PermissionChecker.check(this.context!!, permissions)
        toRequest = unauthorized
        if (allGranted) callback.onPermissionsGranted()
    }

    override fun onRequestPermissionsResult(code: Int, permissions: Array<out String>, results: IntArray) {
        if (requestCode == code) {
            if (
                results.isNotEmpty() &&
                toRequest.size == results.filter { it == PackageManager.PERMISSION_GRANTED }.size
            ) callback.onPermissionsGranted()
        }
    }

    private fun setCheckButtonListener() {
        check_permissions_again.setOnClickListener {
            request()
        }
    }

    private fun request() {
        val (allGranted, unauthorized) = PermissionChecker.check(this.context!!, permissions)
        toRequest = unauthorized

        if (!allGranted) requestPermissions(toRequest, requestCode)
        else callback.onPermissionsGranted()
    }
}