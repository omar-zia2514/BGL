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

//        setContentView(R.layout.activity_patients_list)

        layoutManager = LinearLayoutManager(this)
        binding.patientRecyclerview.layoutManager = layoutManager
        binding.patientRecyclerview.itemAnimator = DefaultItemAnimator()

        var patientsList: ArrayList<Patient> = arrayListOf()

        patientsList.add(Patient(1, "Ali Muzzafar", 39, "Male"))
        patientsList.add(Patient(2, "Ali Muzzafar", 39, "Male"))
        patientsList.add(Patient(3, "Ali Muzzafar", 39, "Male"))
        patientsList.add(Patient(4, "Ali Muzzafar", 39, "Male"))
        patientsList.add(Patient(5, "Ali Muzzafar", 39, "Male"))

        var patientsAdapter = PatientAdapter(patientsList)

        binding.patientRecyclerview.adapter = patientsAdapter
    }
}