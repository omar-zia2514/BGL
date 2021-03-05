package com.prototype.diabetescompanion.view

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.prototype.diabetescompanion.SharedPreferences
import com.prototype.diabetescompanion.databinding.ActivityModeSelectBinding

class ModeSelectActivity : AppCompatActivity() {
    private lateinit var binding: ActivityModeSelectBinding
    private lateinit var context: Context

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        binding = ActivityModeSelectBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        context = this@ModeSelectActivity

        binding.apply {
            btnDoctor.setOnClickListener {
                SharedPreferences.setSignedInProfile(context,
                    SharedPreferences.PROFILE_DOCTOR)
                startActivity(
                    Intent(
                        context,
                        PatientsListActivity::class.java
                    )
                )
            }
            btnPatient.setOnClickListener {
                SharedPreferences.setSignedInProfile(context,
                    SharedPreferences.PROFILE_PATIENT)
                startActivity(
                    Intent(
                        context,
                        PatientActivity::class.java
                    )
                )
            }
        }
    }
}