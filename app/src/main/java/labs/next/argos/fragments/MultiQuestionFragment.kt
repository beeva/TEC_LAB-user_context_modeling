package labs.next.argos.fragments

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.LayoutInflater
import android.widget.Button

import androidx.fragment.app.Fragment

import kotlinx.android.synthetic.main.fragment_multi_questions.*

import labs.next.argos.R
import labs.next.argos.libs.ContextState

class MultiQuestionFragment : Fragment() {
    private lateinit var stateButtons: HashMap<ContextState, Button>
    private lateinit var callback: MultiQuestionListener

    interface MultiQuestionListener {
        fun onMultiQuestionAnswered(answer: ContextState)
    }

    fun setMultiQuestionListener(callback: MultiQuestionListener) {
        this.callback = callback
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_multi_questions, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        stateButtons = hashMapOf(
            ContextState.WORK to set_status_work,
            ContextState.STILL to set_status_still,
            ContextState.TRANSIT to set_status_transit,
            ContextState.PERSONAL to set_status_personal
        )

        setStateButtonsListeners()
    }

    private fun setStateButtonsListeners() {
        stateButtons.forEach { (state, button) ->
            button.setOnClickListener {
                callback.onMultiQuestionAnswered(state)
            }
        }
    }
}