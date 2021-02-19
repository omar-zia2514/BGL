package com.prototype.diabetescompanion

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.prototype.diabetescompanion.databinding.ActivityPatientsListBinding
import java.util.*

class PatientsListActivity : AppCompatActivity() {
    lateinit var patientAdapter: PatientAdapter
    private val layoutManager: RecyclerView.LayoutManager? = null
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

//        layoutManager = LinearLayoutManager(this)
//        binding.patientRecyclerview.
    }
}