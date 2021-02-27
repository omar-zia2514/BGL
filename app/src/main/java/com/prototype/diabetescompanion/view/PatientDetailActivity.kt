package com.prototype.diabetescompanion.view

import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.prototype.diabetescompanion.R
import com.prototype.diabetescompanion.adapter.PatientReadingsAdapter
import com.prototype.diabetescompanion.databinding.ActivityPatientDetailBinding
import com.prototype.diabetescompanion.viewmodel.DiabetesViewModel


class PatientDetailActivity : AppCompatActivity() {
    private var layoutManager: RecyclerView.LayoutManager? = null

    private lateinit var binding: ActivityPatientDetailBinding
    private lateinit var context: Context
    lateinit var diabetesViewModel: DiabetesViewModel
    private var patientId: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPatientDetailBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        context = this@PatientDetailActivity

        diabetesViewModel = ViewModelProvider(this).get(DiabetesViewModel::class.java)

        layoutManager = LinearLayoutManager(this)
        binding.patientReadingsRecyclerview.layoutManager = layoutManager
        binding.patientReadingsRecyclerview.itemAnimator = DefaultItemAnimator()

        diabetesViewModel.getAllReadingsWithPatientId(context, patientId).observe(this, Observer {
            val patientsAdapter = PatientReadingsAdapter(it, context)
            binding.patientReadingsRecyclerview.adapter = patientsAdapter
        })

        binding.patientReadingsRecyclerview.addItemDecoration(DividerItemDecoration(binding.patientReadingsRecyclerview.context,
            DividerItemDecoration.VERTICAL))
        binding.btnBack.setOnClickListener { onBackPressed() }
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