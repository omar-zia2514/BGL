package com.prototype.diabetescompanion.adapter

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.prototype.diabetescompanion.R
import com.prototype.diabetescompanion.model.PatientModel
import com.prototype.diabetescompanion.view.PatientDetailActivity

class PatientAdapter(var dataSet: List<PatientModel>, var ctx: Context) :
    RecyclerView.Adapter<PatientAdapter.MyViewHolder>() {
//    private val dataSet: ArrayList<Patient>? = null

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var txtPatientName: TextView = itemView.findViewById<View>(R.id.patient_name) as TextView
        var txtPatientGender: TextView =
            itemView.findViewById<View>(R.id.patient_gender) as TextView
        var txtPatientAge: TextView = itemView.findViewById<View>(R.id.patient_age) as TextView
        var txtPatientBglHeading: TextView =
            itemView.findViewById<View>(R.id.last_bgl_heading) as TextView
        var txtPatientBgl: TextView = itemView.findViewById<View>(R.id.last_bgl) as TextView
        var txtPatientBglTime: TextView =
            itemView.findViewById<View>(R.id.last_bgl_time) as TextView
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        Log.d("uiDebug", "onCreateViewHolder:")

        val view = LayoutInflater.from(parent.context).inflate(R.layout.patient_card, parent, false)

        view.setOnClickListener {
            ctx.startActivity(Intent(ctx,
                PatientDetailActivity::class.java))
        }

        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        Log.d("uiDebug", "onBindViewHolder: $position")

        val name: TextView = holder.txtPatientName
        val gender: TextView = holder.txtPatientGender
        val age: TextView = holder.txtPatientAge
        val bglHeading: TextView = holder.txtPatientBglHeading
        val bgl: TextView = holder.txtPatientBgl
        val bglTime: TextView = holder.txtPatientBglTime


        dataSet[position].Name.toUpperCase().also {
            name.text = it
        }

        dataSet[position].Gender.also { gender.text = it }
        dataSet[position].Age.also { age.text = it.toString() }
        if (position == 0 || position == 1 || position == 4) {
//            bglHeading.setTextColor(ctx.getColor(R.color.parrot_green_light))
//            bgl.setTextColor(ctx.getColor(R.color.parrot_green_light))
//            bglTime.setTextColor(ctx.getColor(R.color.parrot_green_light))
        } else {
            bglHeading.setTextColor(ctx.getColor(R.color.red))
            bgl.setTextColor(ctx.getColor(R.color.red))
            bglTime.setTextColor(ctx.getColor(R.color.red))
        }

        dataSet[position].LastReading.also {
            bglHeading.text = "Last Reading"
        }
        dataSet[position].LastReading.also { bgl.text = it.toString() }
        dataSet[position].LastReading.also { bglTime.text = it.toString() }
    }

    override fun getItemCount(): Int {
        Log.d("uiDebug", "PatientsList size: ${dataSet.size}")
        return dataSet.size
    }
}