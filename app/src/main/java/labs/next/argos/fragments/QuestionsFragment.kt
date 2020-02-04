package labs.next.argos.fragments

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.LayoutInflater

import androidx.fragment.app.Fragment

import labs.next.argos.R
import labs.next.argos.libs.Utils
import labs.next.argos.libs.Database
import labs.next.argos.libs.ContextState

class QuestionsFragment :
    Fragment(),
    MultiQuestionFragment.MultiQuestionListener,
    SuccessAnswer.BackButtonListener
{
    private var currentState : ContextState? = null
    private lateinit var database: Database

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_questions, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        database = Database(Utils(view.context).deviceID)
        injectMultiQuestion()
    }

    override fun onAttachFragment(childFragment: Fragment) {
        when(childFragment) {
            is MultiQuestionFragment -> childFragment.setMultiQuestionListener(this)
            is SuccessAnswer -> childFragment.setBackButtonListener(this)
        }
    }

    override fun onMultiQuestionAnswered(answer: ContextState) {
        database.saveUserAnswer("state", answer.name)
        injectSuccessAnswer()
    }

    override fun onBackButtonListener() {
        injectMultiQuestion()
    }

    private fun injectMultiQuestion() {
        with(childFragmentManager.beginTransaction()) {
            replace(
                R.id.form_placeholder,
                MultiQuestionFragment()
            )
            commit()
        }
    }

    private fun injectSuccessAnswer() {
        with(childFragmentManager.beginTransaction()) {
            replace(
                R.id.form_placeholder,
                SuccessAnswer()
            )
            commit()
        }
    }
}