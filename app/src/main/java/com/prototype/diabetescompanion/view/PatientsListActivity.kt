package com.prototype.diabetescompanion.view

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.prototype.diabetescompanion.Patient
import com.prototype.diabetescompanion.R
import com.prototype.diabetescompanion.adapter.PatientAdapter
import com.prototype.diabetescompanion.databinding.ActivityPatientsListBinding
import com.prototype.diabetescompanion.model.PatientModel
import com.prototype.diabetescompanion.viewmodel.DiabetesViewModel

class PatientsListActivity : AppCompatActivity() {
    lateinit var patientAdapter: PatientAdapter
    private var layoutManager: RecyclerView.LayoutManager? = null
    private val recyclerView: RecyclerView? = null
    private val data: ArrayList<Patient>? = null

    private lateinit var binding: ActivityPatientsListBinding

    private lateinit var radioGroup: RadioGroup
    private lateinit var radioButton: RadioButton

    lateinit var diabetesViewModel: DiabetesViewModel
    lateinit var context: Context

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPatientsListBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        context = this@PatientsListActivity

        diabetesViewModel = ViewModelProvider(this).get(DiabetesViewModel::class.java)

        layoutManager = LinearLayoutManager(this)
        binding.patientRecyclerview.layoutManager = layoutManager
        binding.patientRecyclerview.itemAnimator = DefaultItemAnimator()

        diabetesViewModel.getAllPatients(context).observe(this, Observer {
            var patientsAdapter = PatientAdapter(it, context)
            binding.patientRecyclerview.adapter = patientsAdapter
        })


        binding.btnSettings.setOnClickListener {
            startActivity(Intent(context,
                SettingsActivity::class.java))
        }
        binding.extendedFab.setOnClickListener({ initNewPatientDialog().show() })
    }

    private fun initNewPatientDialog(): AlertDialog {
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        val v: View = layoutInflater.inflate(R.layout.new_patient_form, null)
        findViewByIdPromptDialog(v)
        builder.setView(v)
        builder.setPositiveButton("Save") { dialog, id ->
            val selectedId: Int = radioGroup.checkedRadioButtonId
            radioButton = v.findViewById<View>(selectedId) as RadioButton

            val radioGenderValue: Int

            val etxtPatientName = v.findViewById<View>(R.id.etxt_patient_name) as EditText
            val etxtPatientAge = v.findViewById<View>(R.id.etxt_patient_age) as EditText

            diabetesViewModel.insertData(context,
                PatientModel(etxtPatientName.text.toString(), radioButton.text.toString(),
                    etxtPatientAge.text.toString().toInt(10)))
        }
/*        builder.setNeutralButton("Delete", DialogInterface.OnClickListener { dialog, id ->

        })*/
        builder.setNegativeButton("Cancel",
            DialogInterface.OnClickListener { dialog, id -> })
        return builder.create()
    }

    private fun findViewByIdPromptDialog(v: View) {
        radioGroup = v.findViewById<View>(R.id.main_radio_group) as RadioGroup
    }
}