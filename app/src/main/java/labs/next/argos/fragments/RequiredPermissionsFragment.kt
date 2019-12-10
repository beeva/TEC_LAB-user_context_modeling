package labs.next.argos.fragments

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import androidx.fragment.app.Fragment
import androidx.core.content.ContextCompat

import kotlinx.android.synthetic.main.fragment_required_permissions.*

import labs.next.argos.R

class RequiredPermissionsFragment(
    var permissions: Array<String>,
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

        val granted = check()
        if (granted) callback.onPermissionsGranted()
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

    private fun check() : Boolean {
        toRequest = permissions.filter { permission ->
            ContextCompat.checkSelfPermission(context as Context, permission) != PackageManager.PERMISSION_GRANTED
        }.toTypedArray()

        return toRequest.isEmpty()
    }

    private fun request() {
        if (!check()) requestPermissions(toRequest, requestCode)
        else callback.onPermissionsGranted()
    }
}