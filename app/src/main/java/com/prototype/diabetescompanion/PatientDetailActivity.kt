package com.prototype.diabetescompanion

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
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

        patientsList.add(Patient(1, "Ali Muzzafar", 39, "Male", "120 mg/dL", "1 hour ago"))
        patientsList.add(Patient(2, "Ayesha Sheikh", 47, "Female", "123 mg/dL", "12 hours ago"))
        patientsList.add(Patient(3, "Kamran Javaid", 65, "Male", "155 mg/dL", "1 day ago"))
        patientsList.add(Patient(4, "Sana Altaf", 51, "Female", "158 mg/dL", "2 days ago"))
        patientsList.add(Patient(5, "Ahmad Rehan", 66, "Male", "123 mg/dL", "3 days ago"))

        var patientsAdapter = PatientReadingsAdapter(patientsList)

        binding.patientReadingsRecyclerview.addItemDecoration(DividerItemDecoration(binding.patientReadingsRecyclerview.getContext(),
            DividerItemDecoration.VERTICAL))
        binding.patientReadingsRecyclerview.adapter = patientsAdapter
    }
}