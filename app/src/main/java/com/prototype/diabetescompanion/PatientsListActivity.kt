package com.prototype.diabetescompanion

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.prototype.diabetescompanion.databinding.ActivityPatientsListBinding

class PatientsListActivity : AppCompatActivity() {
    lateinit var patientAdapter: PatientAdapter
    private var layoutManager: RecyclerView.LayoutManager? = null
    private val recyclerView: RecyclerView? = null
    private val data: ArrayList<Patient>? = null

    private lateinit var binding: ActivityPatientsListBinding
    private lateinit var ctx: Context


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityPatientsListBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        ctx = this
//        setContentView(R.layout.activity_patients_list)

        layoutManager = LinearLayoutManager(this)
        binding.patientRecyclerview.layoutManager = layoutManager
        binding.patientRecyclerview.itemAnimator = DefaultItemAnimator()

        var patientsList: ArrayList<Patient> = arrayListOf()

        patientsList.add(Patient(1, "Ali Muzzafar", 39, "Male", "120 mg/dL", "1 day ago"))
        patientsList.add(Patient(2, "Ayesha Sheikh", 47, "Female", "123 mg/dL", "8 hours ago"))
        patientsList.add(Patient(3, "Kamran Javaid", 65, "Male", "155 mg/dL", "16 hours ago"))
        patientsList.add(Patient(4, "Sana Altaf", 51, "Female", "158 mg/dL", "2 hours ago"))
        patientsList.add(Patient(5, "Ahmad Rehan", 66, "Male", "123 mg/dL", "2 days ago"))
        patientsList.add(Patient(6, "Asif Hussain", 68, "Male", "123 mg/dL", "2 days ago"))
        patientsList.add(Patient(7, "Nauman Ali", 61, "Male", "123 mg/dL", "2 days ago"))


        var patientsAdapter = PatientAdapter(patientsList, ctx)

        binding.patientRecyclerview.adapter = patientsAdapter
        binding.btnSettings.setOnClickListener({ onBackPressed() })
    }
}