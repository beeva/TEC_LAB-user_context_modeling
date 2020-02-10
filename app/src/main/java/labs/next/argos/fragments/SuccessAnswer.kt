package labs.next.argos.fragments

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.LayoutInflater

import androidx.fragment.app.Fragment

import kotlinx.android.synthetic.main.fragment_success_answer.*

import labs.next.argos.R
import java.util.*
import kotlin.concurrent.timerTask

class SuccessAnswer : Fragment() {
    private lateinit var callback: BackButtonListener

    interface BackButtonListener {
        fun onBackButtonListener()
    }

    fun setBackButtonListener(callback: BackButtonListener) {
        this.callback = callback
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_success_answer, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        back_to_questions.setOnClickListener { callback.onBackButtonListener() }
    }

    override fun onPause() {
        super.onPause()
        callback.onBackButtonListener()
    }
}