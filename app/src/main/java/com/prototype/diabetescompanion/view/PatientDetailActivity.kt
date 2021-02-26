package com.prototype.diabetescompanion.view

import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.prototype.diabetescompanion.Patient
import com.prototype.diabetescompanion.adapter.PatientReadingsAdapter
import com.prototype.diabetescompanion.R
import com.prototype.diabetescompanion.databinding.ActivityPatientDetailBinding


class PatientDetailActivity : AppCompatActivity() {
    private var layoutManager: RecyclerView.LayoutManager? = null

    private lateinit var binding: ActivityPatientDetailBinding
    private lateinit var ctx: Context

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityPatientDetailBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        layoutManager = LinearLayoutManager(this)
        binding.patientReadingsRecyclerview.layoutManager = layoutManager
        binding.patientReadingsRecyclerview.itemAnimator = DefaultItemAnimator()

        ctx = this

        var patientsList: ArrayList<Patient> = arrayListOf()

        patientsList.add(Patient(1, "Ali Muzzafar", 39, "Male", "143 mg/dL <-> 140 mg/dL", "1 hour ago"))
        patientsList.add(Patient(2, "Ayesha Sheikh", 47, "Female", "123 mg/dL <-> 129 mg/dL", "12 hours ago"))
        patientsList.add(Patient(3, "Kamran Javaid", 65, "Male", "155 mg/dL <-> 151 mg/dL", "1 day ago"))
        patientsList.add(Patient(4, "Sana Altaf", 51, "Female", "158 mg/dL <-> 155 mg/dL", "2 days ago"))
        patientsList.add(Patient(5, "Ahmad Rehan", 66, "Male", "123 mg/dL <-> 127 mg/dL", "3 days ago"))
        patientsList.add(Patient(5, "Ahmad Rehan", 66, "Male", "127 mg/dL <-> 125 mg/dL", "4 days ago"))
        patientsList.add(Patient(5, "Ahmad Rehan", 66, "Male", "126 mg/dL <-> 130 mg/dL", "4 days ago"))
        patientsList.add(Patient(5, "Ahmad Rehan", 66, "Male", "126 mg/dL <-> 126 mg/dL", "5 days ago"))
        patientsList.add(Patient(5, "Ahmad Rehan", 66, "Male", "124 mg/dL <-> 130 mg/dL", "5 days ago"))

        var patientsAdapter = PatientReadingsAdapter(patientsList)

        binding.patientReadingsRecyclerview.addItemDecoration(DividerItemDecoration(binding.patientReadingsRecyclerview.context,
            DividerItemDecoration.VERTICAL))
        binding.btnBack.setOnClickListener { onBackPressed() }
        binding.patientReadingsRecyclerview.adapter = patientsAdapter

        binding.extendedFab.setOnClickListener { initEditDialog().show() }
    }

    private fun initEditDialog(): AlertDialog {
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        val v: View = layoutInflater.inflate(R.layout.new_reading_form, null)
        builder.setView(v)
        builder.setPositiveButton("Save") { dialog, id ->

        }
        builder.setNeutralButton("Read BGL ", DialogInterface.OnClickListener { dialog, id ->

        })
        builder.setNegativeButton("Cancel"
        ) { dialog, id -> }
        return builder.create()
    }
}