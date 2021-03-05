package com.prototype.diabetescompanion.view

import android.content.Context
import android.content.DialogInterface
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.jjoe64.graphview.GraphView
import com.jjoe64.graphview.series.DataPoint
import com.jjoe64.graphview.series.LineGraphSeries
import com.prototype.diabetescompanion.R
import com.prototype.diabetescompanion.Util
import com.prototype.diabetescompanion.adapter.PatientReadingsAdapter
import com.prototype.diabetescompanion.databinding.ActivityPatientDetailBinding
import com.prototype.diabetescompanion.model.BGLReading
import com.prototype.diabetescompanion.model.PatientModel
import com.prototype.diabetescompanion.viewmodel.DiabetesViewModel
import java.util.*


class PatientDetailActivity : AppCompatActivity() {
    private var layoutManager: RecyclerView.LayoutManager? = null

    private lateinit var binding: ActivityPatientDetailBinding
    private lateinit var context: Context
    lateinit var diabetesViewModel: DiabetesViewModel
    private var patientId: Int = 0
    private lateinit var _patient: PatientModel
    private lateinit var allReadingsList: List<BGLReading>
    private var currentMode = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPatientDetailBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        supportActionBar?.title = resources.getString(R.string.patients_detail_header)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        val oldColors: ColorStateList =
            binding.patientHead.lastBglHeading.textColors
        context = this@PatientDetailActivity
        patientId = intent.getIntExtra("patientId", -1)
        Util.makeLog("patientId received in details activity: $patientId")

        diabetesViewModel = ViewModelProvider(this).get(DiabetesViewModel::class.java)

        diabetesViewModel.getPatientAndLastReading(context, patientId).observe(this, Observer {
            binding.patientHead.patientName.text = it.name?.toUpperCase(Locale.ROOT)
            binding.patientHead.patientGender.text = it.gender
            binding.patientHead.patientAge.text = it.age?.toString()
            if (it.prickValue == null || it.sensorValue == null) {
                binding.patientHead.lastBgl.text = "No data"
                binding.patientHead.lastBglTime.text = "No data"
            } else {
                binding.patientHead.lastBgl.text =
                    it.prickValue.toString() + " mg/dL <---> " + it.sensorValue + " mg/dL"
                binding.patientHead.lastBglTime.text = it.timestamp
                if (it.prickValue!! >= 120 || it.prickValue!! <= 60) {
                    binding.patientHead.lastBglHeading.setTextColor(getColor(R.color.red))
                    binding.patientHead.lastBgl.setTextColor(getColor(R.color.red))
                    binding.patientHead.lastBglTime.setTextColor(getColor(R.color.red))
                } else {
                    binding.patientHead.lastBglHeading.setTextColor(oldColors)
                    binding.patientHead.lastBgl.setTextColor(oldColors)
                    binding.patientHead.lastBglTime.setTextColor(oldColors)
                }
            }
        })
        diabetesViewModel.getAllReadingsWithPatientId(context, patientId).observe(this, {
            val patientReadingsAdapter = PatientReadingsAdapter(it, context)
            binding.patientReadingsRecyclerview.adapter = patientReadingsAdapter
            allReadingsList = it
        })

        layoutManager = LinearLayoutManager(this)
        binding.patientReadingsRecyclerview.layoutManager = layoutManager
        binding.patientReadingsRecyclerview.itemAnimator = DefaultItemAnimator()

        binding.patientReadingsRecyclerview.addItemDecoration(DividerItemDecoration(binding.patientReadingsRecyclerview.context,
            DividerItemDecoration.VERTICAL))
        binding.extendedFab.setOnClickListener { initEditDialog().show() }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.mode_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == R.id.mode_switch) {
            if (currentMode == 0) {
                /*val drawable = ContextCompat.getDrawable(applicationContext, R.drawable.change_pass)
                supportActionBar.setOverflowIcon(drawable)*/
                item.icon = getResources().getDrawable(R.drawable.list_mode)
                item.title = "LIST"
                currentMode = 1
                switchToGraphView()
                initGraph()
            } else {
                item.icon = getResources().getDrawable(R.drawable.graph_mode)
                item.title = "GRAPH"
                currentMode = 0
                switchToListView()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun switchToGraphView() {
        binding.listGroup.visibility = View.GONE
        binding.graph.visibility = View.VISIBLE
    }

    private fun switchToListView() {
        binding.listGroup.visibility = View.VISIBLE
        binding.graph.visibility = View.GONE
    }

    private fun initGraph() {
        val graph = findViewById<View>(R.id.graph) as GraphView
        graph.removeAllSeries()
        graph.title = "BGL Readings"
        var labelRenderer = graph.gridLabelRenderer
//        labelRenderer.horizontalAxisTitle = "No of readings"
//        labelRenderer.verticalAxisTitle = "Values"
        labelRenderer.numVerticalLabels = 20
        labelRenderer.numHorizontalLabels = allReadingsList.size

        graph.viewport.setMinX(1.toDouble())
        graph.viewport.setMaxX((allReadingsList.size).toDouble())
        graph.viewport.setMinY(0.0)
        graph.viewport.setMaxY(400.0)

        graph.viewport.isYAxisBoundsManual = true
        graph.viewport.isXAxisBoundsManual = true

        val series: LineGraphSeries<DataPoint> = LineGraphSeries()
        series.thickness = 8
        series.isDrawDataPoints = true
        series.dataPointsRadius = 15.toFloat()
//        series.isDrawBackground = true
        series.setColor(getColor(R.color.app_green))
        var counter = 1
        for (entry in allReadingsList) {
            series.appendData(DataPoint(counter.toDouble(), entry.PrickValue.toDouble()),
                true, allReadingsList.size)
            counter++
        }
        graph.addSeries(series)
    }

    private fun initEditDialog(): AlertDialog {
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        val v: View = layoutInflater.inflate(R.layout.new_reading_form, null)
        val etxtPrickVal = v.findViewById<View>(R.id.etxt_prick_value) as EditText
        val etxtSensorVal = v.findViewById<View>(R.id.etxt_sensor_value) as EditText
        builder.setView(v)
        builder.setPositiveButton("Save") { dialog, id ->
            diabetesViewModel.insertReading(context,
                BGLReading(patientId,
                    Util.getCurrentTimeStamp(),
                    etxtPrickVal.text.toString().toInt(),
                    etxtSensorVal.text.toString().toInt()))
        }
        builder.setNeutralButton("Read BGL ", DialogInterface.OnClickListener { dialog, id ->
            Toast.makeText(context, "Not implemented yet", Toast.LENGTH_SHORT).show()
        })
        builder.setNegativeButton("Cancel"
        ) { dialog, id -> }
        return builder.create()
    }
}