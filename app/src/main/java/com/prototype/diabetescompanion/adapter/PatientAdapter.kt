package com.prototype.diabetescompanion.adapter

import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.prototype.diabetescompanion.R
import com.prototype.diabetescompanion.interfaces.AdapterToActivity
import com.prototype.diabetescompanion.model.PatientModel
import com.prototype.diabetescompanion.view.PatientDetailActivity
import java.util.*


class PatientAdapter(var ctx: Context) :
    RecyclerView.Adapter<PatientAdapter.MyViewHolder>() {
    lateinit var dataSet: List<PatientModel>

    inner class MyViewHolder(itemView: View, context: Context) : RecyclerView.ViewHolder(itemView) {
        init {
            itemView.setOnLongClickListener {
                val p = layoutPosition
                initEditDeletePatientDialog(it, context, p)
                true // returning true instead of false, works for me
            }
        }

        var cardContainer: ConstraintLayout =
            itemView.findViewById<View>(R.id.card_container_layout) as ConstraintLayout
        var txtPatientName: TextView = itemView.findViewById<View>(R.id.patient_name) as TextView
        var txtPatientGender: TextView =
            itemView.findViewById<View>(R.id.patient_gender) as TextView
        var txtPatientAge: TextView = itemView.findViewById<View>(R.id.patient_age) as TextView
        var txtPatientBglHeading: TextView =
            itemView.findViewById<View>(R.id.last_bgl_heading) as TextView
        var txtPatientBgl: TextView = itemView.findViewById<View>(R.id.last_bgl) as TextView
        var txtPatientBglTime: TextView =
            itemView.findViewById<View>(R.id.last_bgl_time) as TextView

        private fun initEditDeletePatientDialog(view: View, context: Context, position: Int) {
            val builder: AlertDialog.Builder = AlertDialog.Builder(context)
            val v: View = LayoutInflater.from(view.context).inflate(R.layout.edit_delete_form, null)
            var btnEdit = v.findViewById<View>(R.id.btn_edit) as Button
            var btnDelete = v.findViewById<View>(R.id.btn_delete) as Button

            builder.setView(v)
            val dialog = builder.create()
            dialog.show()

            /*val lp = WindowManager.LayoutParams()
            lp.copyFrom(dialog.getWindow()?.getAttributes())
            lp.width = lp.width
            lp.height = lp.height
            dialog.getWindow()?.setAttributes(lp)
*/
            btnEdit.setOnClickListener {
                initEditPatientDialog(view, context, position)
                dialog.dismiss()
            }
            btnDelete.setOnClickListener {
                initDeleteConfirmationDialog(context, position)
                dialog.dismiss()
            }
        }

        private fun initEditPatientDialog(view: View, context: Context, position: Int) {
            val builder: AlertDialog.Builder = AlertDialog.Builder(context)
            val v: View = LayoutInflater.from(view.context).inflate(R.layout.edit_form, null)
            var txtName = v.findViewById<View>(R.id.etxt_patient_name) as TextView
            var txtAge = v.findViewById<View>(R.id.etxt_patient_age) as TextView
            val radioGroup = v.findViewById<View>(R.id.main_radio_group) as RadioGroup

            txtName.text = dataSet[position].Name
            txtAge.text = dataSet[position].Age.toString()

            if (dataSet[position].Gender.equals("Male"))
                radioGroup.check(R.id.radio_male)
            else
                radioGroup.check(R.id.radio_female)

            builder.setView(v)
            builder.setPositiveButton("Update", null)

            builder.setNegativeButton("Cancel", null)
            val dialog = builder.create()

            dialog.setOnShowListener {
                val button: Button = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                button.setOnClickListener {
                    val selectedId: Int = radioGroup.checkedRadioButtonId
                    val radioButton = v.findViewById<View>(selectedId) as RadioButton

                    val etxtPatientName = v.findViewById<View>(R.id.etxt_patient_name) as EditText
                    val etxtPatientAge = v.findViewById<View>(R.id.etxt_patient_age) as EditText

                    if (etxtPatientName.text.toString().trim()
                            .isEmpty() || etxtPatientAge.text.toString().trim().isEmpty()
                    ) {
                        Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show()
                    } else {
                        val updatedPatient = PatientModel(etxtPatientName.text.toString(), "",
                            radioButton.text.toString(),
                            dataSet[position].DOB,
                            etxtPatientAge.text.toString().toInt(10),
                            dataSet[position].LastReading,
                            dataSet[position].LastReadingTimestamp)
                        updatedPatient.Id = this@PatientAdapter.dataSet[position].Id
                        (context as AdapterToActivity).onUpdate(updatedPatient)
                        dialog.dismiss()
                    }
                }
            }
            dialog.show()
        }

        private fun initDeleteConfirmationDialog(context: Context, position: Int) {
            val builder: AlertDialog.Builder = AlertDialog.Builder(context)

            builder.setMessage("Are you sure?")
            builder.setPositiveButton("Yes") { _, _ ->
                (context as AdapterToActivity).onDelete(this@PatientAdapter.dataSet[position].Id)
            }
            builder.setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
            }
            val dialog = builder.create()
            dialog.show()
        }
    }

    fun setAdapterData(data: List<PatientModel>) {
        dataSet = data
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        Log.d("uiDebug", "onCreateViewHolder:")

        val view = LayoutInflater.from(parent.context).inflate(R.layout.patient_card, parent, false)

        return MyViewHolder(view, ctx)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        Log.d("uiDebug", "onBindViewHolder: $position")

        holder.cardContainer.setOnClickListener {
            val startPatientDetailsActivityIntent: Intent = Intent(ctx,
                PatientDetailActivity::class.java)
            startPatientDetailsActivityIntent.putExtra("patientId", dataSet[position].Id)
            ctx.startActivity(startPatientDetailsActivityIntent)
        }

        val name: TextView = holder.txtPatientName
        val gender: TextView = holder.txtPatientGender
        val age: TextView = holder.txtPatientAge
        val bglHeading: TextView = holder.txtPatientBglHeading
        val bgl: TextView = holder.txtPatientBgl
        val bglTime: TextView = holder.txtPatientBglTime

        val oldColors: ColorStateList =
            holder.txtPatientName.textColors

        dataSet[position].Name.toUpperCase(Locale.ROOT).also {
            name.text = it
        }
        dataSet[position].Gender.also { gender.text = it }
        dataSet[position].Age.also { age.text = it.toString() }
        dataSet[position].LastReading.also {
            bglHeading.text = "Last Reading"
        }
        if (dataSet[position].LastReading != null && dataSet[position].LastReadingTimestamp != null) {
            dataSet[position].LastReading.also { bgl.text = it }
            dataSet[position].LastReadingTimestamp.also { bglTime.text = it }

            if (dataSet[position].LastReading!!.split(" ")[0].toFloat() >= 120 || dataSet[position].LastReading!!.split(
                    " ")[0].toFloat() <= 60
            ) {
                bglHeading.setTextColor(ctx.getColor(R.color.red))
                bgl.setTextColor(ctx.getColor(R.color.red))
                bglTime.setTextColor(ctx.getColor(R.color.red))
            } else {
                bglHeading.setTextColor(oldColors)
                bgl.setTextColor(oldColors)
                bglTime.setTextColor(oldColors)
            }
        } else {
            dataSet[position].LastReading.also { bgl.text = "No Data" }
            dataSet[position].LastReadingTimestamp.also { bglTime.text = "No Data" }
        }
    }

    override fun getItemCount(): Int {
//        Log.d("uiDebug", "PatientsList size: ${dataSet.size}")
        if (this::dataSet.isInitialized)
            return dataSet.size
        return 0
    }
}