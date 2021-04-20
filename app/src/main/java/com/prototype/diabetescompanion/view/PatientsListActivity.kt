package com.prototype.diabetescompanion.view

import android.content.Context
import android.content.DialogInterface.OnShowListener
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.*
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.prototype.diabetescompanion.*
import com.prototype.diabetescompanion.R
import com.prototype.diabetescompanion.adapter.PatientAdapter
import com.prototype.diabetescompanion.databinding.ActivityPatientsListBinding
import com.prototype.diabetescompanion.interfaces.AdapterToActivity
import com.prototype.diabetescompanion.model.PatientModel
import com.prototype.diabetescompanion.viewmodel.DiabetesViewModel
import org.json.JSONException

class PatientsListActivity : AppCompatActivity(), AdapterToActivity {
    lateinit var patientAdapter: PatientAdapter
    private var layoutManager: RecyclerView.LayoutManager? = null
    private val recyclerView: RecyclerView? = null
    private val data: ArrayList<Patient>? = null

    private lateinit var binding: ActivityPatientsListBinding

    private lateinit var radioGroup: RadioGroup
    private lateinit var radioButton: RadioButton

    lateinit var diabetesViewModel: DiabetesViewModel
    lateinit var context: Context

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPatientsListBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        supportActionBar?.title = resources.getString(R.string.patients_list_header)

        context = this@PatientsListActivity

        diabetesViewModel = ViewModelProvider(this).get(DiabetesViewModel::class.java)

        layoutManager = LinearLayoutManager(this)
        binding.patientRecyclerview.layoutManager = layoutManager
        binding.patientRecyclerview.itemAnimator = DefaultItemAnimator()

        val patientsAdapter = PatientAdapter(context)
        binding.patientRecyclerview.adapter = patientsAdapter

        diabetesViewModel.getAllPatients(context).observe(this, {

            (binding.patientRecyclerview.adapter as PatientAdapter).setAdapterData(it)

        })

        binding.extendedFab.setOnClickListener { initNewPatientDialog() }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finishAffinity()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.settings_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.sync -> {
                syncDoctorData()
            }

            R.id.settings -> startActivity(Intent(context, SettingsActivity::class.java))
        }
        return super.onOptionsItemSelected(item)
    }

    fun syncDoctorData() {
        val url = "http://diabetescompanions.uk/api/SyncRecord"

        // Post parameters
        // Form fields and values

        val stringRequestData =
            "\"{\"DoctorId\\\":126,\\\"Name\\\":\\\"Dr Peter\\\",\\\"Age\\\":25,\\\"Address\\\":\\\"134 Upper Mall\\\",\\\"Specification\\\":\\\"HOD\\\",\\\"Experience\\\":15,\\\"SyncType\\\":0,\\\"Patients\\\":[{\\\"Id\\\":0,\\\"PatientId\\\":12,\\\"Name\\\":\\\"Usman\\\",\\\"Age\\\":25,\\\"Address\\\":\\\"4J Gulberg\\\",\\\"Gender\\\":\\\"HOD\\\",\\\"ContactNo\\\":\\\"03311224545\\\",\\\"OperationCode\\\":1,\\\"Readings\\\":[{\\\"ReadingdateTime\\\":\\\"2018-04-19 15:27:16.050\\\",\\\"SensorValue\\\":135,\\\"PrickValue\\\":130},{\\\"ReadingdateTime\\\":\\\"2018-04-19 15:27:16.050\\\",\\\"SensorValue\\\":132,\\\"PrickValue\\\":131}]},{\\\"Id\\\":1,\\\"PatientId\\\":12,\\\"Name\\\":\\\"Usman\\\",\\\"Age\\\":25,\\\"Address\\\":\\\"4J Gulberg\\\",\\\"Gender\\\":\\\"HOD\\\",\\\"ContactNo\\\":\\\"03311224545\\\",\\\"Readings\\\":[{\\\"ReadingdateTime\\\":\\\"2018-04-19 15:27:16.050\\\",\\\"SensorValue\\\":135,\\\"PrickValue\\\":130},{\\\"ReadingdateTime\\\":\\\"2018-04-19 15:27:16.050\\\",\\\"SensorValue\\\":132,\\\"PrickValue\\\":131}]}]}\""


        // Volley post request with parameters
        val request = object : JsonObjectRequest(Method.POST, url, JsonManager.getJson(),
            { response ->
                // Process the json
                try {
                    Util.makeLog("Response: $response")
                    Toast.makeText(applicationContext,
                        "Response," + response.toString() + "",
                        Toast.LENGTH_LONG).show()
                } catch (e: Exception) {
                    Util.makeLog("Exception: $e")
                }

            }, {
                // Error in request
                Util.makeLog("Volley error1: $it")




                Util.makeLog("Volley error2: ${it.toString()}")
                Util.makeLog("Volley error3: ${it.message}")
                Util.makeLog("Volley error4: ${it.localizedMessage}")

                Toast.makeText(applicationContext, "error," + it.toString() + "", Toast.LENGTH_LONG)
                    .show()
                // Toast.makeText(applicationContext, obj.getString("message"), Toast.LENGTH_LONG).show()
            }) {
            @Throws(AuthFailureError::class)
            override fun getParams(): Map<String, String> {
                val params = HashMap<String, String>()
                params["Data"] = JsonManager.getJson().toString()
                return params
            }

            override fun getHeaders(): MutableMap<String, String> {
                val params: MutableMap<String, String> = java.util.HashMap()
                params["Content-Type"] = "application/x-www-form-urlencoded"
//                params["Content-Type"] = "application/json"
                return params
            }
        }

        val stringRequest = object : StringRequest(Method.POST, url,
            Response.Listener { response ->
                try {

                    val obj = (response)
                    Toast.makeText(applicationContext, "test " + obj + "", Toast.LENGTH_SHORT)
                        .show()

                    // Toast.makeText(applicationContext, obj.getString("message"), Toast.LENGTH_LONG).show()
                } catch (e: JSONException) {
                    Toast.makeText(applicationContext,
                        "response," + response.toString() + "",
                        Toast.LENGTH_LONG).show()
                    e.printStackTrace()
                }
            },

            object : Response.ErrorListener {
                override fun onErrorResponse(volleyError: VolleyError) {
                    Toast.makeText(applicationContext,
                        "error," + volleyError.toString() + "",
                        Toast.LENGTH_LONG).show()

                }
            }) {
            @Throws(AuthFailureError::class)
            override fun getParams(): Map<String, String> {
                val params = HashMap<String, String>()
                params["Data"] = JsonManager.getJson().toString()
                return params
            }

            override fun getHeaders(): MutableMap<String, String> {
                val params: MutableMap<String, String> = java.util.HashMap()
                params["Content-Type"] = "application/x-www-form-urlencoded"
                // params.put("Content-Type", "application/json");

//                params["Content-Type"] = "application/json"
                return params
            }
        }
        // Volley request policy, only one time request to avoid duplicate transaction
        request.retryPolicy = DefaultRetryPolicy(
            DefaultRetryPolicy.DEFAULT_TIMEOUT_MS,
            // 0 means no retry
            DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        )

        // Add the volley post request to the request queue
        VolleySingleton.getInstance(this).addToRequestQueue(request)
    }

    private fun initNewPatientDialog() {
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        val v: View = layoutInflater.inflate(R.layout.new_patient_form, null)
        findViewByIdPromptDialog(v)
        builder.setView(v)

        builder.setPositiveButton("Save", null)
/*        builder.setNeutralButton("Delete", DialogInterface.OnClickListener { dialog, id ->

        })*/
        builder.setNegativeButton("Cancel", null)

        val dialog = builder.create()
        dialog.setOnShowListener(OnShowListener {
            val button: Button = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            button.setOnClickListener(View.OnClickListener {
                val selectedId: Int = radioGroup.checkedRadioButtonId
                radioButton = v.findViewById<View>(selectedId) as RadioButton

                val radioGenderValue: Int

                val etxtPatientName = v.findViewById<View>(R.id.etxt_patient_name) as EditText
                val etxtPatientAge = v.findViewById<View>(R.id.etxt_patient_age) as EditText

                if (etxtPatientName.text.toString().trim()
                        .isEmpty() || etxtPatientAge.text.toString().trim().isEmpty()
                ) {
                    Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show()
                } else {
                    diabetesViewModel.insertPatient(context,
                        PatientModel(etxtPatientName.text.toString(),
                            "",
                            radioButton.text.toString(),
                            etxtPatientAge.text.toString().toInt(10)))
                    dialog.dismiss()
                }
            })
        })
        dialog.show()
    }

    private fun findViewByIdPromptDialog(v: View) {
        radioGroup = v.findViewById<View>(R.id.main_radio_group) as RadioGroup
    }

    override fun onDelete(id: Int?) {
        if (id != null)
            diabetesViewModel.deletePatientWithId(context, id)
    }

    override fun onUpdate(patient: PatientModel) {
        diabetesViewModel.updatePatient(context, patient)
    }
}