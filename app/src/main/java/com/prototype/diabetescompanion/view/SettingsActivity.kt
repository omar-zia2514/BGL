package com.prototype.diabetescompanion.view

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.prototype.diabetescompanion.R
import com.prototype.diabetescompanion.SharedPreferences
import com.prototype.diabetescompanion.databinding.ActivitySettingsBinding


class SettingsActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySettingsBinding
    private lateinit var context: Context

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        supportActionBar?.title = resources.getString(R.string.title_settings_activity)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        context = this@SettingsActivity

        setProfileValues()
        setRadioListeners()
    }

    override fun onSupportNavigateUp(): Boolean {
        if (SharedPreferences.getSignedInProfile(context) == SharedPreferences.PROFILE_DOCTOR)
            startActivity(Intent(context, PatientsListActivity::class.java))
        else
            startActivity(Intent(context, PatientActivity::class.java))
        finish()
        return true
    }

    override fun onBackPressed() {
        onSupportNavigateUp()
    }

    private fun setProfileValues() {
        if (SharedPreferences.getSignedInProfile(context) == SharedPreferences.PROFILE_DOCTOR)
            binding.radioDoctor.isChecked = true
        else
            binding.radioPatient.isChecked = true

    }

    private fun setRadioListeners() {
        binding.radioDoctor.setOnClickListener {
            if (SharedPreferences.getSignedInProfile(context) == SharedPreferences.PROFILE_PATIENT) {
                binding.radioDoctor.isChecked = true
                binding.radioPatient.isChecked = false
                if (SharedPreferences.getDoctorCreated(context)) {
                    SharedPreferences.setSignedInProfile(context, SharedPreferences.PROFILE_DOCTOR)
                    Toast.makeText(context, "You have switched to Doctor's profile",
                        Toast.LENGTH_SHORT)
                        .show()
                    startActivity(Intent(context, PatientsListActivity::class.java))
                    finish()
                } else {
                    val intent = Intent(context, SignInActivity::class.java)
                    intent.putExtra("signInType", SharedPreferences.PROFILE_DOCTOR)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                }
            }
        }
        binding.radioPatient.setOnClickListener {
            if (SharedPreferences.getSignedInProfile(context) == SharedPreferences.PROFILE_DOCTOR) {
                binding.radioDoctor.isChecked = false
                binding.radioPatient.isChecked = true
                if (SharedPreferences.getPatientCreated(context)) {
                    SharedPreferences.setSignedInProfile(context, SharedPreferences.PROFILE_PATIENT)
                    Toast.makeText(context,
                        "You have switched to Patient's profile",
                        Toast.LENGTH_SHORT)
                        .show()
                    startActivity(Intent(context, PatientActivity::class.java))
                    finish()

                } else {
                    val intent = Intent(context, SignInActivity::class.java)
                    intent.putExtra("signInType", SharedPreferences.PROFILE_PATIENT)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                    intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
                    startActivity(intent)
                }
            }
        }
    }
}