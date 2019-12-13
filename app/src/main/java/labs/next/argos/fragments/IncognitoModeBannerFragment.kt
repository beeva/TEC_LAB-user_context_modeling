package labs.next.argos.fragments

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.LayoutInflater

import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_incognito_mode_banner.*

import labs.next.argos.R

class IncognitoModeBannerFragment(
    private var initialValue: Boolean,
    private val enabled: Boolean = true
) : Fragment() {
    private lateinit var callback: IncognitoModeListener

    interface IncognitoModeListener {
        fun onIncognitoModeChanged(enabled: Boolean)
        fun onIncognitoModeAttached(fragment: Fragment)
    }

    fun setIncognitoModeListener(callback: IncognitoModeListener) {
        this.callback = callback
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_incognito_mode_banner, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        updateMode(initialValue)
        syncTextToMode(initialValue)
        incognito_mode_banner_switch.isClickable = enabled
        incognito_mode_banner_switch.setOnCheckedChangeListener { _, enabled ->
            callback.onIncognitoModeChanged(enabled)
        }

        callback.onIncognitoModeAttached(this)
    }

    fun updateMode(enabled: Boolean) {
        this.initialValue = enabled
        if (incognito_mode_banner_switch != null) incognito_mode_banner_switch.isChecked = enabled
        syncTextToMode(enabled)

    }

    private fun syncTextToMode(enabled: Boolean) {
        if (incognito_mode_banner_text != null) {
            incognito_mode_banner_text.text = getString(
                if (enabled) R.string.incognito_mode_banner_text_enable
                else R.string.incognito_mode_banner_text_disable
            )
        }
    }
}