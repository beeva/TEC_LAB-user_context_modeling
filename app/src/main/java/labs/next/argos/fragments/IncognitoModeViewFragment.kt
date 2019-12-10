package labs.next.argos.fragments

import java.util.*
import java.text.SimpleDateFormat

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.LayoutInflater

import androidx.fragment.app.Fragment

import kotlinx.android.synthetic.main.fragment_incognito_mode_view.*

import labs.next.argos.R
import labs.next.argos.libs.Utils

class IncognitoModeViewFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_incognito_mode_view, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val template = getString(R.string.incognito_mode_title)
        val delay = Utils(view.context).incognitoDelay
        val temp = SimpleDateFormat("HH:mm").format(Date(delay))

        incognito_mode_title.text = "$template $temp"
    }
}