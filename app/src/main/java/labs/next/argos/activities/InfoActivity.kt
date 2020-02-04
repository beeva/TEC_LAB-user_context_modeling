package labs.next.argos.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_info.*
import kotlinx.android.synthetic.main.secondary_toolbar.*
import labs.next.argos.R
import labs.next.argos.libs.Utils

class InfoActivity : AppCompatActivity() {
    private lateinit var utils: Utils
    private val prefix: String = "mailto:"
    private val email: String = "labs.next@bbva.com"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_info)
        setActionBar(secondary_toolbar)

        utils = Utils(this)
        inflateDataList()
        inflateDeviceID()
        setListeners()
    }

    private fun inflateDataList() {
        val entities = resources.getStringArray(R.array.info_data_entities)

        var listText = ""
        entities.forEach { listText += " - $it\n" }
        data_list_container.text = listText
    }

    private fun inflateDeviceID() {
        device_id_container.text = utils.deviceID
    }

    private fun setListeners() {
        go_back_button.setOnClickListener { goBack() }
        contact_button.setOnClickListener { mailTo() }
    }

    private fun goBack() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }

    private fun mailTo() {
        val intent = Intent(Intent.ACTION_SEND)
        intent.data = Uri.parse(prefix + email)
        intent.type = "text/plain"
        intent.putExtra(Intent.EXTRA_EMAIL, arrayOf(email))

        val subject = "ArgosApp - User question (ID: $utils.deviceID)"
        intent.putExtra(Intent.EXTRA_SUBJECT, subject)

        val title = resources.getString(R.string.info_contact_dialog)
        val chooser = Intent.createChooser(intent, title)
        if (intent.resolveActivity(packageManager) != null) startActivity(chooser)
    }
}