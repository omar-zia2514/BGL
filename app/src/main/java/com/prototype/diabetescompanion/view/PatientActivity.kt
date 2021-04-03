package com.prototype.diabetescompanion.view

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.prototype.diabetescompanion.R
import com.prototype.diabetescompanion.Util
import com.prototype.diabetescompanion.databinding.ActivityPatientBinding
import com.prototype.diabetescompanion.model.BGLReading
import com.prototype.diabetescompanion.viewmodel.DiabetesViewModel

class PatientActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPatientBinding
    lateinit var context: Context
    lateinit var diabetesViewModel: DiabetesViewModel


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPatientBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        supportActionBar?.title = resources.getString(R.string.patients_screen_title)

        context = this@PatientActivity

        binding.extendedFab.setOnClickListener { initEditDialog().show() }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finishAffinity()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.settings_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == R.id.settings) {
            startActivity(Intent(context, SettingsActivity::class.java))
        }
        return super.onOptionsItemSelected(item)
    }

    private fun initEditDialog(): AlertDialog {
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        val v: View = layoutInflater.inflate(R.layout.new_reading_form, null)
        val etxtPrickVal = v.findViewById<View>(R.id.etxt_prick_value) as EditText
        val etxtSensorVal = v.findViewById<View>(R.id.etxt_sensor_value) as EditText
        builder.setView(v)
        /*builder.setPositiveButton("Save") { dialog, id ->
            val timestampString = Util.getCurrentTimeStamp()
            val valuesString = etxtPrickVal.text.toString() + " - " + etxtSensorVal.text.toString()
            diabetesViewModel.insertReading(context,
                BGLReading(patientId,
                    timestampString,
                    etxtPrickVal.text.toString().toInt(),
                    etxtSensorVal.text.toString().toInt()))

            diabetesViewModel.updatePatientLastReading(context,
                patientId,
                valuesString,
                timestampString)
        }*/
        builder.setNeutralButton("Read BGL ", DialogInterface.OnClickListener { dialog, id ->
            Toast.makeText(context, "Not implemented yet", Toast.LENGTH_SHORT).show()
        })
        builder.setNegativeButton("Cancel"
        ) { dialog, id -> }
        return builder.create()
    }
}