package com.prototype.diabetescompanion.view

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.prototype.diabetescompanion.R
import com.prototype.diabetescompanion.SharedPreferences
import com.prototype.diabetescompanion.databinding.ActivitySigninBinding
import com.prototype.diabetescompanion.model.DoctorModel
import com.prototype.diabetescompanion.viewmodel.DiabetesViewModel

class SignInActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySigninBinding
    private lateinit var context: Context
    lateinit var diabetesViewModel: DiabetesViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySigninBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        supportActionBar?.title = resources.getString(R.string.title_sigin_activity)

        context = this@SignInActivity
        diabetesViewModel = ViewModelProvider(this).get(DiabetesViewModel::class.java)

        var signInType = intent.getIntExtra("signInType", SharedPreferences.PROFILE_DOCTOR)

        if (signInType == SharedPreferences.PROFILE_DOCTOR) {
            binding.groupDoctorFields.visibility = View.VISIBLE
            setDoctorSignUp()
        } else {
            binding.groupPatientFields.visibility = View.VISIBLE
        }

        binding.btnLogin.isEnabled = true

        binding.btnLogin.setOnClickListener {
            val name = binding.etxtName.text.toString()
            val mobileNumber = binding.etxtNumber.text.toString()
            val designation = binding.etxtDesignation.text.toString()
            val hospital = binding.etxtHospital.text.toString()

            diabetesViewModel.insertDoctor(context,
                DoctorModel(name, mobileNumber, designation, hospital))

            SharedPreferences.setDoctorCreated(context, true)
            SharedPreferences.setSignedInProfile(context,
                SharedPreferences.PROFILE_DOCTOR)
            startActivity(
                Intent(
                    context,
                    PatientsListActivity::class.java
                )
            )
        }
    }

    private fun setDoctorSignUp(){
        binding.etxtName.addTextChangedListener()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finishAffinity()
    }
}