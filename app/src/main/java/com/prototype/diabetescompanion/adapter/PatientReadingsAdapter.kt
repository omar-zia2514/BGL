package com.prototype.diabetescompanion.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.prototype.diabetescompanion.R
import com.prototype.diabetescompanion.interfaces.AdapterToActivity
import com.prototype.diabetescompanion.model.BGLReading
import java.text.SimpleDateFormat

class PatientReadingsAdapter(var ctx: Context) :
    RecyclerView.Adapter<PatientReadingsAdapter.MyViewHolder>() {
    lateinit var dataSet: List<BGLReading>

    inner class MyViewHolder(itemView: View, context: Context) : RecyclerView.ViewHolder(itemView) {
        init {
            itemView.setOnLongClickListener {
                val p = layoutPosition
                notifyLongPress(context, p)
                true // returning true instead of false, works for me
            }
        }

        var txtReadingTimestamp: TextView =
            itemView.findViewById<View>(R.id.reading_timestamp) as TextView
        var txtReadingValue: TextView =
            itemView.findViewById<View>(R.id.reading_value) as TextView

        fun notifyLongPress(context: Context, position: Int) {
            (context as AdapterToActivity).onLongPress(dataSet[position])
        }
    }


    fun setAdapterData(data: List<BGLReading>) {
        dataSet = data
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        Log.d("uiDebug", "onCreateViewHolder:")

        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.patient_reading_row, parent, false)

        //setOnClickListener here

        return MyViewHolder(view, ctx)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        Log.d("uiDebug", "onBindViewHolder: $position")

        val timestamp: TextView = holder.txtReadingTimestamp
        val value: TextView = holder.txtReadingValue

        dataSet[position].Timestamp.also {
            val timeStamp = SimpleDateFormat("EEE, MMM d, ''yy 'at' h:mm:ss a").parse(it)
            timestamp.text = SimpleDateFormat("EEE, MMM d, ''yy 'at' h:mm a").format(timeStamp)
        }

        value.text =
            dataSet[position].PrickValue.toString() + " - " + dataSet[position].SensorValue

//        dataSet[position].PrickValue.toString()
//            .also { value.text = it + " <-> " + dataSet[position].SensorValue.toString() }
    }

    override fun getItemCount(): Int {
        if (this::dataSet.isInitialized)
            return dataSet.size
        return 0
    }
}