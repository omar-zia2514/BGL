package com.prototype.diabetescompanion.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.prototype.diabetescompanion.Patient
import com.prototype.diabetescompanion.R

class PatientReadingsAdapter(var dataSet: ArrayList<Patient>) :
    RecyclerView.Adapter<PatientReadingsAdapter.MyViewHolder>() {

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var txtReadingTimestamp: TextView =
            itemView.findViewById<View>(R.id.reading_timestamp) as TextView
        var txtReadingValue: TextView =
            itemView.findViewById<View>(R.id.reading_value) as TextView
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        Log.d("uiDebug", "onCreateViewHolder:")

        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.patient_reading_row, parent, false)

        //setOnClickListener here

        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        Log.d("uiDebug", "onBindViewHolder: $position")

        val timestamp: TextView = holder.txtReadingTimestamp
        val value: TextView = holder.txtReadingValue

        dataSet[position].lastBGLReadingTime.also { timestamp.text = it }
        dataSet[position].lastBGLReading.also { value.text = it }
    }

    override fun getItemCount(): Int {
        Log.d("uiDebug", "PatientsList size: ${dataSet.size}")
        return dataSet.size
    }
}