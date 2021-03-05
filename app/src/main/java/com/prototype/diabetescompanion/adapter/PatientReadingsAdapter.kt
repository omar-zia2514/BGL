package com.prototype.diabetescompanion.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.prototype.diabetescompanion.R
import com.prototype.diabetescompanion.model.BGLReading

class PatientReadingsAdapter(var dataSet: List<BGLReading>, var ctx: Context) :
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

        dataSet[position].Timestamp.also { timestamp.text = it }

        value.text =
            dataSet[position].PrickValue.toString() + " mg/dL <---> " + dataSet[position].SensorValue + " mg/dL"

//        dataSet[position].PrickValue.toString()
//            .also { value.text = it + " <-> " + dataSet[position].SensorValue.toString() }
    }

    override fun getItemCount(): Int {
        Log.d("uiDebug", "PatientsList size: ${dataSet.size}")
        return dataSet.size
    }
}