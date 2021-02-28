package com.prototype.diabetescompanion.view

import android.content.Context
import android.content.DialogInterface.OnShowListener
import android.os.Bundle
import android.view.View
import android.widget.*
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
//        setTitle(R.string.patients_list_header)
        getSupportActionBar()?.setTitle(resources.getString(R.string.patients_list_header))
        getSupportActionBar()?.setDisplayHomeAsUpEnabled(true)
        getSupportActionBar()?.setDisplayShowHomeEnabled(true)

        context = this@PatientsListActivity

        diabetesViewModel = ViewModelProvider(this).get(DiabetesViewModel::class.java)

        layoutManager = LinearLayoutManager(this)
        binding.patientRecyclerview.layoutManager = layoutManager
        binding.patientRecyclerview.itemAnimator = DefaultItemAnimator()

        diabetesViewModel.getAllPatients(context).observe(this, Observer {
            val patientsAdapter = PatientAdapter(it, context)
            binding.patientRecyclerview.adapter = patientsAdapter
        })


        /*binding.btnSettings.setOnClickListener {
            startActivity(Intent(context,
                SettingsActivity::class.java))
        }*/
        binding.extendedFab.setOnClickListener({ initNewPatientDialog() })
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    private fun initNewPatientDialog() {
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        val v: View = layoutInflater.inflate(R.layout.new_patient_form, null)
        findViewByIdPromptDialog(v)
        builder.setView(v)

        builder.setPositiveButton("Save", null)
/*        builder.setNeutralButton("Delete", DialogInterface.OnClickListener { dialog, id ->

        })*/
        builder.setNegativeButton("Cancel", null)

        val dialog = builder.create()
        dialog.setOnShowListener(OnShowListener {
            val button: Button = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            button.setOnClickListener(View.OnClickListener {
                val selectedId: Int = radioGroup.checkedRadioButtonId
                radioButton = v.findViewById<View>(selectedId) as RadioButton

                val radioGenderValue: Int

                val etxtPatientName = v.findViewById<View>(R.id.etxt_patient_name) as EditText
                val etxtPatientAge = v.findViewById<View>(R.id.etxt_patient_age) as EditText

                if (etxtPatientName.text.toString().trim()
                        .isEmpty() || etxtPatientAge.text.toString().trim().isEmpty()
                ) {
                    Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show()
                } else {
                    diabetesViewModel.insertData(context,
                        PatientModel(etxtPatientName.text.toString(), radioButton.text.toString(),
                            etxtPatientAge.text.toString().toInt(10)))
                    //Dismiss once everything is OK.
                    dialog.dismiss()
                }
            })
        })
        dialog.show()
    }

    private fun findViewByIdPromptDialog(v: View) {
        radioGroup = v.findViewById<View>(R.id.main_radio_group) as RadioGroup
    }
}