package com.prototype.diabetescompanion

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class PatientAdapter(val dataSet: ArrayList<Patient>) :
    RecyclerView.Adapter<PatientAdapter.MyViewHolder>() {
//    private val dataSet: ArrayList<Patient>? = null

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var txtPatientName: TextView = itemView.findViewById<View>(R.id.patient_name) as TextView
        var txtPatientGender: TextView =
            itemView.findViewById<View>(R.id.patient_gender) as TextView
        var txtPatientAge: TextView = itemView.findViewById<View>(R.id.patient_age) as TextView

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.patient_card, parent, false)

        //setOnClickListener here

        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val name: TextView = holder.txtPatientName
        val gender: TextView = holder.txtPatientGender
        val age: TextView = holder.txtPatientAge

        dataSet[position].name.also { name.text = it }
        dataSet[position].gender.also { gender.text = it }
        dataSet[position].age.also { age.text = it.toString() }
    }

    override fun getItemCount(): Int {
        return dataSet.size
    }
}