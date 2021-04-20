package com.prototype.diabetescompanion.view

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.jjoe64.graphview.GraphView
import com.jjoe64.graphview.LegendRenderer
import com.jjoe64.graphview.series.DataPoint
import com.jjoe64.graphview.series.LineGraphSeries
import com.prototype.diabetescompanion.DataPointWithTime
import com.prototype.diabetescompanion.R
import com.prototype.diabetescompanion.Util
import com.prototype.diabetescompanion.adapter.PatientReadingsAdapter
import com.prototype.diabetescompanion.databinding.ActivityPatientBinding
import com.prototype.diabetescompanion.model.BGLReading
import com.prototype.diabetescompanion.viewmodel.DiabetesViewModel

class PatientActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPatientBinding
    lateinit var context: Context
    lateinit var diabetesViewModel: DiabetesViewModel
    private var patientId = 0
    private var layoutManager: RecyclerView.LayoutManager? = null
    private lateinit var allReadingsList: List<BGLReading>
    private var currentMode = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPatientBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        supportActionBar?.title = resources.getString(R.string.patients_screen_title)

        context = this@PatientActivity

        diabetesViewModel = ViewModelProvider(this).get(DiabetesViewModel::class.java)

        diabetesViewModel.getOwnerPatientId(context).observe(this, {
            patientId = it
            diabetesViewModel.getAllReadingsWithPatientId(context, patientId).observe(this, {
                val patientReadingsAdapter = PatientReadingsAdapter(it, context)
                binding.patientReadingsRecyclerview.adapter = patientReadingsAdapter
                allReadingsList = it
            })
        })

        layoutManager = LinearLayoutManager(this)
        binding.patientReadingsRecyclerview.layoutManager = layoutManager
        binding.patientReadingsRecyclerview.itemAnimator = DefaultItemAnimator()

        binding.patientReadingsRecyclerview.addItemDecoration(DividerItemDecoration(binding.patientReadingsRecyclerview.context,
            DividerItemDecoration.VERTICAL))

        binding.extendedFab.setOnClickListener { initEditDialog().show() }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finishAffinity()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.sync, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {
            R.id.settings -> startActivity(Intent(context, SettingsActivity::class.java))
            R.id.mode_switch -> {
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
        val series: LineGraphSeries<DataPoint> = LineGraphSeries()
        series.apply {
            thickness = 4
            isDrawDataPoints = true
            dataPointsRadius = 10.toFloat()
            color = getColor(R.color.app_green)
            title = "Prick Values"
        }

        var counter = 1
        var maxY = 0
        var minY = 400

        var listOfPrickPoints = mutableListOf<DataPointWithTime>()

        for (entry in allReadingsList.indices.reversed()) {
            if (allReadingsList[entry].PrickValue > maxY)
                maxY = allReadingsList[entry].PrickValue
            if (allReadingsList[entry].PrickValue < minY)
                minY = allReadingsList[entry].PrickValue
            listOfPrickPoints.add(DataPointWithTime(counter.toDouble(),
                allReadingsList[entry].PrickValue.toDouble()))
            series.appendData(DataPoint(counter.toDouble(),
                allReadingsList[entry].PrickValue.toDouble()),
                true, allReadingsList.size)
            counter++
        }

        val seriesSensor: LineGraphSeries<DataPoint> = LineGraphSeries()
        seriesSensor.apply {
            thickness = 4
            isDrawDataPoints = true
            dataPointsRadius = 10.toFloat()
            color = getColor(R.color.red)
            title = "Sensor Values"
        }
//        series.isDrawBackground = true

        var counterSensor = 1
        for (entry in allReadingsList.indices.reversed()) {
            if (allReadingsList[entry].SensorValue > maxY)
                maxY = allReadingsList[entry].SensorValue
            if (allReadingsList[entry].SensorValue < minY)
                minY = allReadingsList[entry].SensorValue
            seriesSensor.appendData(DataPoint(counterSensor.toDouble(),
                allReadingsList[entry].SensorValue.toDouble()),
                true, allReadingsList.size)
            counterSensor++
        }

        series.setOnDataPointTapListener { series1, dataPoint ->
            var pointsCounter = 1
            for (entry in allReadingsList.indices.reversed()) {
                var point = DataPointWithTime(dataPoint.x, dataPoint.y)
                var originalPoint = DataPointWithTime(pointsCounter.toDouble(),
                    allReadingsList[entry].PrickValue.toDouble())
                if (point == originalPoint)
                    Toast.makeText(context,
                        "${allReadingsList[entry].Timestamp} \nValue: ${dataPoint.y}",
                        Toast.LENGTH_LONG).show()
                pointsCounter++
            }
        }

        seriesSensor.setOnDataPointTapListener { series1, dataPoint ->
            var sensorPointsCounter = 1
            for (entry in allReadingsList.indices.reversed()) {
                var point = DataPointWithTime(dataPoint.x, dataPoint.y)
                var originalPoint = DataPointWithTime(sensorPointsCounter.toDouble(),
                    allReadingsList[entry].SensorValue.toDouble())
                if (point == originalPoint)
                    Toast.makeText(context,
                        "${allReadingsList[entry].Timestamp} \nValue: ${dataPoint.y}",
                        Toast.LENGTH_LONG).show()
                sensorPointsCounter++
            }
        }

        val graph = findViewById<View>(R.id.graph) as GraphView
        graph.apply {
            removeAllSeries()
            title = "BGL Readings"
            legendRenderer.isVisible = true
            legendRenderer.align = LegendRenderer.LegendAlign.TOP
            viewport.setMinX(1.toDouble())
            viewport.setMaxX((allReadingsList.size).toDouble())
            viewport.setMinY((minY - 20).toDouble())
            viewport.setMaxY((maxY + 20).toDouble())
            viewport.isYAxisBoundsManual = true
            viewport.isXAxisBoundsManual = true
            viewport.isScrollable = true
            viewport.setScrollableY(true)
            viewport.isScalable = true
            viewport.setScalableY(true)
        }

        val labelRenderer = graph.gridLabelRenderer
        labelRenderer.horizontalAxisTitle = "No of readings"
        labelRenderer.verticalAxisTitle = "BGL Values"
        labelRenderer.numVerticalLabels = 20
        labelRenderer.numHorizontalLabels = allReadingsList.size
        labelRenderer.padding = 50
//        labelRenderer.setHorizontalLabelsAngle(90)

        graph.addSeries(series)
        graph.addSeries(seriesSensor)
    }

    private fun initEditDialog(): AlertDialog {
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        val v: View = layoutInflater.inflate(R.layout.new_reading_form, null)
        val etxtPrickVal = v.findViewById<View>(R.id.etxt_prick_value) as EditText
        val etxtSensorVal = v.findViewById<View>(R.id.etxt_sensor_value) as EditText
        builder.setView(v)
        builder.setPositiveButton("Save") { dialog, id ->
            val timestampString = Util.getCurrentTimeStamp()
            val valuesString = etxtPrickVal.text.toString() + " - " + etxtSensorVal.text.toString()
            diabetesViewModel.insertReading(context,
                BGLReading(patientId,
                    timestampString,
                    etxtPrickVal.text.toString().toInt(),
                    etxtSensorVal.text.toString().toInt()))

            diabetesViewModel.updatePatientLastReading(context,
                patientId,
                valuesString,
                timestampString)
        }
        builder.setNeutralButton("Read BGL ") { dialog, id ->
            Toast.makeText(context, "Not implemented yet", Toast.LENGTH_SHORT).show()
        }
        builder.setNegativeButton("Cancel"
        ) { dialog, id -> }
        return builder.create()
    }
}