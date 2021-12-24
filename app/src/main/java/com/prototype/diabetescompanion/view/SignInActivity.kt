package com.prototype.diabetescompanion.view

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.prototype.diabetescompanion.R
import com.prototype.diabetescompanion.SharedPreferences
import com.prototype.diabetescompanion.databinding.ActivitySigninBinding
import com.prototype.diabetescompanion.model.DoctorModel
import com.prototype.diabetescompanion.model.PatientModel
import com.prototype.diabetescompanion.viewmodel.DiabetesViewModel
import java.util.*

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
            setDoctorSignUp()
        } else {
            setPatientSignUp()
        }
    }

    private fun setDoctorSignUp() {
        binding.groupDoctorFields.visibility = View.VISIBLE

        binding.etxtName.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                binding.btnLogin.isEnabled =
                    binding.etxtNumber.length() > 0 && (count > 0 || start > 0)
            }
        })

        binding.etxtNumber.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                binding.btnLogin.isEnabled =
                    binding.etxtName.length() > 0 && (count > 0 || start > 0)
            }
        })

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

    @SuppressLint("SetTextI18n")
    private fun setPatientSignUp() {
        binding.groupPatientFields.visibility = View.VISIBLE

        binding.etxtName.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                binding.btnLoginPatient.isEnabled =
                    binding.etxtNumber.length() > 0 && (count > 0 || start > 0)
            }
        })

        binding.etxtNumber.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                binding.btnLoginPatient.isEnabled =
                    binding.etxtName.length() > 0 && (count > 0 || start > 0)
            }
        })

        val c = Calendar.getInstance()
        val yearNow = c.get(Calendar.YEAR)
        val monthNow = c.get(Calendar.MONTH)
        val dayNow = c.get(Calendar.DAY_OF_MONTH)
        var yearOfBirth = 0
        var dob: String = ""

        binding.btnDob.setOnClickListener {
//            val dpd = DatePickerDialog(context, DatePickerDialog.OnDateSetListener(function = ))

            val dpd = DatePickerDialog(this, { view, yearSelected, monthSelected, daySelected ->
                // Display Selected date in TextView
                yearOfBirth = yearSelected
                dob = "" + daySelected + "/" + (monthSelected + 1) + "/" + yearSelected
                binding.btnDob.text = dob

            }, yearNow, monthNow, dayNow)
            dpd.show()

        }

        val languages = resources.getStringArray(R.array.Languages)
        val adapter = ArrayAdapter(this,
            android.R.layout.simple_spinner_item, languages)
        binding.spinnerGender.adapter = adapter
        var selectedGender = ""
        binding.spinnerGender.onItemSelectedListener = object :
            AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View, position: Int, id: Long,
            ) {
                selectedGender = languages[position]
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // write code to perform some action
            }
        }

        binding.btnLoginPatient.setOnClickListener {
            val name = binding.etxtName.text.toString()
            val mobileNumber = binding.etxtNumber.text.toString()
            val age = yearNow - yearOfBirth
            diabetesViewModel.insertPatient(context,
                PatientModel(name, mobileNumber, selectedGender,dob, age, DoctorId = 0))

            SharedPreferences.setPatientCreated(context, true)
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

    override fun onBackPressed() {
        super.onBackPressed()
        finishAffinity()
    }
}