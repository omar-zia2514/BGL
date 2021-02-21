package com.prototype.diabetescompanion

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class PatientAdapter(var dataSet: ArrayList<Patient>, var ctx: Context) :
    RecyclerView.Adapter<PatientAdapter.MyViewHolder>() {
//    private val dataSet: ArrayList<Patient>? = null

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var txtPatientName: TextView = itemView.findViewById<View>(R.id.patient_name) as TextView
        var txtPatientGender: TextView =
            itemView.findViewById<View>(R.id.patient_gender) as TextView
        var txtPatientAge: TextView = itemView.findViewById<View>(R.id.patient_age) as TextView
        var txtPatientBgl: TextView = itemView.findViewById<View>(R.id.last_bgl) as TextView
        var txtPatientBglTime: TextView =
            itemView.findViewById<View>(R.id.last_bgl_time) as TextView
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        Log.d("uiDebug", "onCreateViewHolder:")

        val view = LayoutInflater.from(parent.context).inflate(R.layout.patient_card, parent, false)

        view.setOnClickListener({
            ctx.startActivity(Intent(ctx,
                PatientDetailActivity::class.java))
        })

        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        Log.d("uiDebug", "onBindViewHolder: $position")

        val name: TextView = holder.txtPatientName
        val gender: TextView = holder.txtPatientGender
        val age: TextView = holder.txtPatientAge
        val bgl: TextView = holder.txtPatientBgl
        val bglTime: TextView = holder.txtPatientBglTime


        dataSet[position].name.also { name.text = it }
        dataSet[position].gender.also { gender.text = it }
        dataSet[position].age.also { age.text = it.toString() }
        dataSet[position].lastBGLReading.also { bgl.text = it }
        dataSet[position].lastBGLReadingTime.also { bglTime.text = it }

    }

    override fun getItemCount(): Int {
        Log.d("uiDebug", "PatientsList size: ${dataSet.size}")
        return dataSet.size
    }
}