package com.prototype.diabetescompanion.view

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.content.*
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.anychart.AnyChart
import com.anychart.chart.common.dataentry.DataEntry
import com.anychart.chart.common.dataentry.ValueDataEntry
import com.anychart.chart.common.listener.Event
import com.anychart.chart.common.listener.ListenersInterface
import com.anychart.charts.Cartesian
import com.anychart.core.cartesian.series.Line
import com.anychart.core.cartesian.series.Marker
import com.anychart.data.Mapping
import com.anychart.data.Set
import com.anychart.enums.*
import com.anychart.graphics.vector.Stroke
import com.prototype.diabetescompanion.R
import com.prototype.diabetescompanion.Util
import com.prototype.diabetescompanion.adapter.PatientReadingsAdapter
import com.prototype.diabetescompanion.ble.*
import com.prototype.diabetescompanion.databinding.ActivityPatientDetailBinding
import com.prototype.diabetescompanion.interfaces.AdapterToActivity
import com.prototype.diabetescompanion.model.BGLReading
import com.prototype.diabetescompanion.model.PatientModel
import com.prototype.diabetescompanion.viewmodel.DiabetesViewModel
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

@Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS", "DEPRECATED_IDENTITY_EQUALS")
class PatientDetailActivity : AppCompatActivity(), OnDeviceScanListener, AdapterToActivity {
    private var layoutManager: RecyclerView.LayoutManager? = null

    private lateinit var binding: ActivityPatientDetailBinding
    private lateinit var context: Context
    lateinit var diabetesViewModel: DiabetesViewModel
    private var patientId: Int = 0
    private lateinit var _patient: PatientModel
    private lateinit var allReadingsList: List<BGLReading>
    private var currentMode = 0
    private var mDeviceAddress: String = ""

    private val REQUEST_LOCATION_PERMISSION = 2018
    private val REQUEST_ENABLE_BT = 1000
    private val TAG = "diabetesDebug"
    private lateinit var etxtSensorVal: EditText

    lateinit var cartesian: Cartesian
    lateinit var seriesError: Line
    lateinit var seriesPrick: Line
    lateinit var seriesSensor: Line
    lateinit var markerPrick: Marker
    lateinit var markerSensor: Marker
    lateinit var weeklyReadings: List<BGLReading>
    lateinit var monthlyReadings: List<BGLReading>
    lateinit var yearlyReadings: List<BGLReading>
    var currentReadingTemperature: Float = 0F
    var currentReadingFingerWidth: Float = 0F
    var currentReadingVoltage: Float = 0F

    private var state: Int = 0
    private val ASent: Int = 1
    private val AReceived: Int = 2
    private val BSent: Int = 3
    private val BReceived: Int = 4
    private val CSent: Int = 5
    private val CReceived: Int = 6
    private val DSent: Int = 7
    private val DReceived: Int = 8

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

        diabetesViewModel.getPatientAndLastReading(context, patientId).observe(this, {
            binding.patientHead.patientName.text = it.name?.toUpperCase(Locale.ROOT)
            binding.patientHead.patientGender.text = it.gender
            binding.patientHead.patientAge.text = it.age?.toString()
            if (it.prickValue == null || it.sensorValue == null) {
                binding.patientHead.lastBgl.text = "No data"
                binding.patientHead.lastBglTime.text = "No data"
            } else {
                binding.patientHead.lastBgl.text =
                    it.prickValue.toString() + " - " + it.sensorValue
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

        val readingsAdapter = PatientReadingsAdapter(context)
        binding.patientReadingsRecyclerview.adapter = readingsAdapter

        diabetesViewModel.getAllReadingsLiveDataWithPatientId(context, patientId).observe(this, {
            (binding.patientReadingsRecyclerview.adapter as PatientReadingsAdapter).setAdapterData(
                it)
            allReadingsList = it
        })

        layoutManager = LinearLayoutManager(this)
        binding.patientReadingsRecyclerview.layoutManager = layoutManager
        binding.patientReadingsRecyclerview.itemAnimator = DefaultItemAnimator()

        binding.patientReadingsRecyclerview.addItemDecoration(DividerItemDecoration(binding.patientReadingsRecyclerview.context,
            DividerItemDecoration.VERTICAL))
        binding.extendedFab.setOnClickListener { initEditDialog() }

        setButtonBackgrounds(0)

        binding.btnToday.setOnClickListener {
            setButtonBackgrounds(1)
            rePlotGraph(1, Util.getCurrentTimeStamp())
        }
        binding.btnWeek.setOnClickListener {
            setButtonBackgrounds(2)
            rePlotGraph(2, Util.getCurrentTimeStamp())
        }
        binding.btnMonth.setOnClickListener {
            setButtonBackgrounds(3)
            rePlotGraph(3, Util.getCurrentTimeStamp())
        }
        binding.btnYear.setOnClickListener {
            setButtonBackgrounds(4)
            rePlotGraph(4, Util.getCurrentTimeStamp())
        }
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
                item.icon = resources.getDrawable(R.drawable.list_mode)
                item.title = "LIST"
                currentMode = 1
                switchToGraphView()
                plotGraph()
            } else {
                item.icon = resources.getDrawable(R.drawable.graph_mode)
                item.title = "GRAPH"
                currentMode = 0
                switchToListView()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onScanCompleted(bglDevice: BGLSensorDevice) {
        Util.makeLog("onScanCompleted(): Going to connect now ")
        //Initiate a dialog Fragment from here and ask the user to select his device
        // If the application already know the Mac address, we can simply call connect device

        mDeviceAddress = bglDevice.mDeviceAddress
        BLEConnectionManager.connect(bglDevice.mDeviceAddress)
    }

    private fun switchToGraphView() {
        binding.listGroup.visibility = View.GONE
        binding.graphGroup.visibility = View.VISIBLE
    }

    private fun switchToListView() {
        binding.listGroup.visibility = View.VISIBLE
        binding.graphGroup.visibility = View.GONE
    }

    private fun getXAxis(dateString: String, graphTimeline: Int): String {
        val date = SimpleDateFormat("EEE, MMM d, ''yy 'at' h:mm:ss a").parse(dateString)
        val cal = Calendar.getInstance()
        cal.time = date

        when (graphTimeline) {
            1 -> {
                val hours = if (cal[Calendar.HOUR] == 0) 12 else cal[Calendar.HOUR]
                val mins =
                    if (cal[Calendar.MINUTE] < 10) "0" + cal[Calendar.MINUTE] else cal[Calendar.MINUTE]
                val secs = cal[Calendar.SECOND]
                val ammp = cal[Calendar.AM_PM]
                val ammpString: String = if (ammp == 0)
                    "AM"
                else
                    "PM"

                return "$hours:$mins:$secs $ammpString"
            }
            2 -> {
                val day = cal[Calendar.DAY_OF_WEEK]
                Util.makeLog("Day: $day")
                return when (day) {
                    1 -> "Sunday"
                    2 -> "Monday"
                    3 -> "Tuesday"
                    4 -> "Wednesday"
                    5 -> "Thursday"
                    6 -> "Friday"
                    7 -> "Saturday"
                    else -> "Sunday"
                }
            }
            3 -> {
                val dayOfMonth = cal[Calendar.DAY_OF_MONTH]
                val monthName = when (cal[Calendar.MONTH]) {
                    0 -> "Jan"
                    1 -> "Feb"
                    2 -> "Mar"
                    3 -> "Apr"
                    4 -> "May"
                    5 -> "Jun"
                    6 -> "Jul"
                    7 -> "Aug"
                    8 -> "Sep"
                    9 -> "Oct"
                    10 -> "Nov"
                    11 -> "Dec"
                    else -> "Jan"
                }
                Util.makeLog("Day: $dayOfMonth")
                return "$dayOfMonth $monthName"
            }
            4 -> {
                val month = cal[Calendar.MONTH]
                Util.makeLog("Day: $month")
                return when (month) {
                    0 -> "January"
                    1 -> "February"
                    2 -> "March"
                    3 -> "April"
                    4 -> "May"
                    5 -> "June"
                    6 -> "July"
                    7 -> "August"
                    8 -> "September"
                    9 -> "October"
                    10 -> "November"
                    11 -> "December"
                    else -> "January"
                }
            }
            else -> {
                val hours = if (cal[Calendar.HOUR] == 0) 12 else cal[Calendar.HOUR]
                val mins =
                    if (cal[Calendar.MINUTE] < 10) "0" + cal[Calendar.MINUTE] else cal[Calendar.MINUTE]
                val secs = cal[Calendar.SECOND]
                val ammp = cal[Calendar.AM_PM]
                val ammpString: String = if (ammp == 0)
                    "AM"
                else
                    "PM"

                return "$hours:$mins:$secs $ammpString"
            }
        }
    }

    private fun getXAxisMajorLabel(dateString: String, graphTimeline: Int): String {
        val date = SimpleDateFormat("EEE, MMM d, ''yy 'at' h:mm:ss a").parse(dateString)
        val cal = Calendar.getInstance()
        cal.time = date

        when (graphTimeline) {
            1 -> {
                val dayOfMonth = cal[Calendar.DAY_OF_MONTH]
                val month: String = when (cal[Calendar.MONTH]) {
                    0 -> "January"
                    1 -> "February"
                    2 -> "March"
                    3 -> "April"
                    4 -> "May"
                    5 -> "June"
                    6 -> "July"
                    7 -> "August"
                    8 -> "September"
                    9 -> "October"
                    10 -> "November"
                    11 -> "December"
                    else -> "January"
                }
                val year = cal[Calendar.YEAR]

                return "$dayOfMonth $month, $year"
            }
            2 -> {
                return when (cal[Calendar.MONTH]) {
                    0 -> "January"
                    1 -> "February"
                    2 -> "March"
                    3 -> "April"
                    4 -> "May"
                    5 -> "June"
                    6 -> "July"
                    7 -> "August"
                    8 -> "September"
                    9 -> "October"
                    10 -> "November"
                    11 -> "December"
                    else -> "January"
                }
            }
            3 -> {
                val year = cal[Calendar.YEAR]
                return "$year"
            }
            else -> {
                val dayOfMonth = cal[Calendar.DAY_OF_MONTH]
                val month: String = when (cal[Calendar.MONTH]) {
                    0 -> "January"
                    1 -> "February"
                    2 -> "March"
                    3 -> "April"
                    4 -> "May"
                    5 -> "June"
                    6 -> "July"
                    7 -> "August"
                    8 -> "September"
                    9 -> "October"
                    10 -> "November"
                    11 -> "December"
                    else -> "January"
                }
                val year = cal[Calendar.YEAR]

                return "$dayOfMonth $month, $year"
            }
        }
    }

    @SuppressLint("SimpleDateFormat")
    fun getTodaysReadings(): List<BGLReading> {
        val todayReading = arrayListOf<BGLReading>()

        val calToday = Calendar.getInstance()
        calToday.time = Date()
        val calReading = Calendar.getInstance()

        for (entry in allReadingsList.indices.reversed()) {
            calReading.time =
                SimpleDateFormat("EEE, MMM d, ''yy 'at' h:mm:ss a").parse(allReadingsList[entry].Timestamp)
            if (calReading[Calendar.DAY_OF_YEAR] === calToday[Calendar.DAY_OF_YEAR] &&
                calReading[Calendar.YEAR] === calToday[Calendar.YEAR]
            ) {
                todayReading.add(allReadingsList[entry])
            }
        }
        Util.makeLog("Current timestamp when saving new reading: ${todayReading.size}")
        return todayReading
    }

    @SuppressLint("SimpleDateFormat")
    fun getTodaysReadings(dayTimestamp: String): List<BGLReading> {
        val todayReading = arrayListOf<BGLReading>()

        val calToday = Calendar.getInstance()
        calToday.time = SimpleDateFormat("EEE, MMM d, ''yy 'at' h:mm:ss a").parse(dayTimestamp)

        //Date()
        val calReading = Calendar.getInstance()

        for (entry in allReadingsList.indices.reversed()) {
            calReading.time =
                SimpleDateFormat("EEE, MMM d, ''yy 'at' h:mm:ss a").parse(allReadingsList[entry].Timestamp)
            if (calReading[Calendar.DAY_OF_YEAR] === calToday[Calendar.DAY_OF_YEAR] &&
                calReading[Calendar.YEAR] === calToday[Calendar.YEAR]
            ) {
                todayReading.add(allReadingsList[entry])
            }
        }
        Util.makeLog("Current timestamp when saving new reading: ${todayReading.size}")
        return todayReading
    }

    private fun getReadingsFromWeekDay(weekday: String?): List<BGLReading> {
        Util.makeLog("getReadingsFromWeekDay(): $weekday")
        val specificDayReadings = arrayListOf<BGLReading>()
        val calSpecificDay = Calendar.getInstance()
//        calSpecificDay.time = SimpleDateFormat("EEE, MMM d, ''yy 'at' h:mm:ss a").parse(weekday)

        val calReading = Calendar.getInstance()
        for (entry in weeklyReadings.indices) {
//            calReading.time =
//                SimpleDateFormat("EEE, MMM d, ''yy 'at' h:mm:ss a").parse()
            if (getXAxis(weeklyReadings[entry].Timestamp, 2) == weekday
            ) {
                specificDayReadings.add(weeklyReadings[entry])
            }
        }
        Util.makeLog("No of readings calculated from weekly to daily shift: ${specificDayReadings.size}")
        return specificDayReadings
    }

    private fun getReadingsFromMonthDay(monthday: String?): List<BGLReading> {
        Util.makeLog("getReadingsFromMonthDay(): $monthday")
        val specificDayReadings = arrayListOf<BGLReading>()
        val calSpecificDay = Calendar.getInstance()
//        calSpecificDay.time = SimpleDateFormat("EEE, MMM d, ''yy 'at' h:mm:ss a").parse(weekday)

        val calReading = Calendar.getInstance()
        for (entry in monthlyReadings.indices) {
//            calReading.time =
//                SimpleDateFormat("EEE, MMM d, ''yy 'at' h:mm:ss a").parse()
            if (getXAxis(monthlyReadings[entry].Timestamp, 3) == monthday
            ) {
                specificDayReadings.add(monthlyReadings[entry])
            }
        }
        Util.makeLog("No of readings calculated from monthly to daily shift: ${specificDayReadings.size}")
        return specificDayReadings
    }

    private fun getReadingsFromYearMonth(yearMonth: String?): List<BGLReading> {
        Util.makeLog("getReadingsFromMonthDay(): $yearMonth")
        val specificDayReadings = arrayListOf<BGLReading>()
        val calSpecificDay = Calendar.getInstance()
//        calSpecificDay.time = SimpleDateFormat("EEE, MMM d, ''yy 'at' h:mm:ss a").parse(weekday)

        val calReading = Calendar.getInstance()
        for (entry in yearlyReadings.indices) {
//            calReading.time =
//                SimpleDateFormat("EEE, MMM d, ''yy 'at' h:mm:ss a").parse()
            if (getXAxis(yearlyReadings[entry].Timestamp, 4) == yearMonth
            ) {
                specificDayReadings.add(yearlyReadings[entry])
            }
        }
        Util.makeLog("No of readings calculated from monthly to daily shift: ${specificDayReadings.size}")
        return specificDayReadings
    }


    @SuppressLint("SimpleDateFormat")
    fun getWeeksReadings(): List<BGLReading> {
        val todayReading = arrayListOf<BGLReading>()

        val calToday = Calendar.getInstance()
        calToday.time = Date()
//            SimpleDateFormat("EEE, MMM d, ''yy 'at' h:mm:ss a").parse("Sat, May 29, '21 at 2:51:11 AM")
        val calReading = Calendar.getInstance()

        for (entry in allReadingsList.indices.reversed()) {
            calReading.time =
                SimpleDateFormat("EEE, MMM d, ''yy 'at' h:mm:ss a").parse(allReadingsList[entry].Timestamp)
            if (calReading[Calendar.WEEK_OF_YEAR] === calToday[Calendar.WEEK_OF_YEAR] &&
                calReading[Calendar.YEAR] === calToday[Calendar.YEAR]
            ) {
                todayReading.add(allReadingsList[entry])
            }
        }
        Util.makeLog("Current timestamp when saving new reading: ${todayReading.size}")
        return todayReading
    }

    @SuppressLint("SimpleDateFormat")
    fun getMonthsReadings(): List<BGLReading> {
        val todayReading = arrayListOf<BGLReading>()

        val calToday = Calendar.getInstance()
        calToday.time = Date()
//            SimpleDateFormat("EEE, MMM d, ''yy 'at' h:mm:ss a").parse("Sat, May 29, '21 at 2:51:11 AM")
        val calReading = Calendar.getInstance()

        for (entry in allReadingsList.indices.reversed()) {
            calReading.time =
                SimpleDateFormat("EEE, MMM d, ''yy 'at' h:mm:ss a").parse(allReadingsList[entry].Timestamp)
            if (calReading[Calendar.MONTH] === calToday[Calendar.MONTH] &&
                calReading[Calendar.YEAR] === calToday[Calendar.YEAR]
            ) {
                todayReading.add(allReadingsList[entry])
            }
        }
        Util.makeLog("Current timestamp when saving new reading: ${todayReading.size}")
        return todayReading
    }

    @SuppressLint("SimpleDateFormat")
    fun getYearsReadings(): List<BGLReading> {
        val todayReading = arrayListOf<BGLReading>()

        val calToday = Calendar.getInstance()
        calToday.time = Date()
        val calReading = Calendar.getInstance()

        for (entry in allReadingsList.indices.reversed()) {
            calReading.time =
                SimpleDateFormat("EEE, MMM d, ''yy 'at' h:mm:ss a").parse(allReadingsList[entry].Timestamp)
            if (calReading[Calendar.YEAR] === calToday[Calendar.YEAR]) {
                todayReading.add(allReadingsList[entry])
            }
        }
        Util.makeLog("Current timestamp when saving new reading: ${todayReading.size}")
        return todayReading
    }

    private fun plotGraph() {
        cartesian = setCartesianProperties(getXAxisMajorLabel(Util.getCurrentTimeStamp(), 1), true)

        val listOfReadingsToPlot: List<BGLReading> = getTodaysReadings()

        val seriesData: ArrayList<DataEntry> = ArrayList()
        var counter = 1
        for (entry in listOfReadingsToPlot.indices) {
//            val sdf: SimpleDateFormat = SimpleDateFormat("EEE, MMM d, ''yy 'at' h:mm:ss a")
//            val date = sdf.parse(listOfReadingsToPlot[entry].Timestamp)
//            sdf.applyPattern("hh:mm:ss a MM-dd-yyyy")
//            val formatedDate = sdf.format(date)
//            Util.makeLog("Data: ${formatedDate}")
            seriesData.add(CustomDataEntry(getXAxis(listOfReadingsToPlot[entry].Timestamp,
                1),
                listOfReadingsToPlot[entry].PrickValue,
                listOfReadingsToPlot[entry].SensorValue,
                listOfReadingsToPlot[entry].PrickValue - listOfReadingsToPlot[entry].SensorValue))
            counter++
        }

        val set: Set = Set.instantiate()
        set.data(seriesData)
        val series1Mapping: Mapping =
            set.mapAs("{ x: 'x', value: 'value' }")
        val series2Mapping: Mapping =
            set.mapAs("{ x: 'x', value: 'value2' }")
        val series3Mapping: Mapping =
            set.mapAs("{ x: 'x', value: 'value3' }")

        seriesPrick = cartesian.line(series1Mapping)
        seriesPrick.apply {
            name("Prick Graph")
            color("skyblue")
            legendItem().iconType(LegendItemIconType.LINE)
            hovered().markers().enabled(true)
            hovered().markers()
                .type(MarkerType.CIRCLE)
                .size(10.0)
            tooltip()
                .position("right")
                .anchor(Anchor.LEFT_CENTER)
                .offsetX(0.0)
                .offsetY(0.0)
                .format("Prick Value: {%value}")
            setOnClickListener(object : ListenersInterface.OnClickListener(arrayOf("x",
                "value")) {
                override fun onClick(event: Event?) {
                    Util.makeLog("EventPrick: " + event?.data.toString())
                }
            })
        }

        seriesSensor = cartesian.line(series2Mapping)
        seriesSensor.apply {
            name("Sensor Graph")
            color("steelblue")
            legendItem().iconType(LegendItemIconType.LINE)
            hovered().markers().enabled(true)
            hovered().markers()
                .type(MarkerType.CIRCLE)
                .size(4.0)
            tooltip()
                .position("right")
                .anchor(Anchor.LEFT_CENTER)
                .offsetX(0.0)
                .offsetY(0.0)
                .format("Sensor Value: {%value}")
        }

        seriesError = cartesian.line(series3Mapping)
        seriesError.apply {
            name("Difference")
            legendItem().iconType(LegendItemIconType.LINE)
            hovered().markers().enabled(true)
            hovered().markers()
                .type(MarkerType.CIRCLE)
                .size(4.0)
            tooltip()
                .position("right")
                .anchor(Anchor.LEFT_CENTER)
                .offsetX(0.0)
                .offsetY(0.0)
            markers().type(MarkerType.DIAGONAL_CROSS)
        }

        markerPrick = cartesian.marker(series1Mapping)
        markerPrick.apply {
            size(4)
            color("black")
            type(MarkerType.CIRCLE)
            legendItem().iconType(LegendItemIconType.BUBBLE)
            name("Prick Points")
            fill("function() {\n" +
                    "    var threshold = this.iterator.get('value');\n" +
                    "    if (threshold > 200)\n" +
                    "      return 'crimson';\n" +
                    "    else if (threshold < 200 && threshold > 140)\n" +
                    "      return 'coral';\n" +
                    "    return 'skyblue';\n" +
                    "  }")
            tooltip().enabled(false)
        }

        markerSensor = cartesian.marker(series2Mapping)
        markerSensor.apply {
            size(4)
            color("black")
            type(MarkerType.CIRCLE)
            legendItem().iconType(LegendItemIconType.BUBBLE)
            name("Sensor Points")
            fill("function() {\n" +
                    "    var threshold = this.iterator.get('value2');\n" +
                    "    if (threshold > 200)\n" +
                    "      return 'crimson';\n" +
                    "    else if (threshold < 200 && threshold > 140)\n" +
                    "      return 'coral';\n" +
                    "    return 'steelblue';\n" +
                    "  }")
            tooltip().enabled(false)
        }
        binding.graph.setChart(cartesian)
    }

    private fun rePlotGraph(graphTimeline: Int, timestamp: String) {
        val listOfReadingsToPlot: List<BGLReading>
        when (graphTimeline) {
            1 -> {
                setCartesianProperties(getXAxisMajorLabel(timestamp, 1), false)
                listOfReadingsToPlot = getTodaysReadings(timestamp)
            }
            2 -> {
                setCartesianProperties("", false)
                weeklyReadings = getWeeksReadings()
                listOfReadingsToPlot = weeklyReadings
            }
            3 -> {
                setCartesianProperties(getXAxisMajorLabel(timestamp, 2), false)
                monthlyReadings = getMonthsReadings()
                listOfReadingsToPlot = monthlyReadings
            }
            4 -> {
                setCartesianProperties(getXAxisMajorLabel(timestamp, 3), false)
                yearlyReadings = getYearsReadings()
                listOfReadingsToPlot = yearlyReadings
            }
            else -> listOfReadingsToPlot = getTodaysReadings()
        }

        val seriesData: ArrayList<DataEntry> = ArrayList()
        var counter = 1
        for (entry in listOfReadingsToPlot.indices) {
            seriesData.add(CustomDataEntry(getXAxis(listOfReadingsToPlot[entry].Timestamp,
                graphTimeline),
                listOfReadingsToPlot[entry].PrickValue,
                listOfReadingsToPlot[entry].SensorValue,
                listOfReadingsToPlot[entry].PrickValue - listOfReadingsToPlot[entry].SensorValue))
            counter++
        }

        val set: Set = Set.instantiate()
        set.data(seriesData)
        val series1Mapping: Mapping =
            set.mapAs("{ x: 'x', value: 'value' }")
        val series2Mapping: Mapping =
            set.mapAs("{ x: 'x', value: 'value2' }")
        val series3Mapping: Mapping =
            set.mapAs("{ x: 'x', value: 'value3' }")

        seriesPrick.apply {
            data(series1Mapping)
            name("Prick Graph")
            color("skyblue")
            legendItem().iconType(LegendItemIconType.LINE)
            hovered().markers().enabled(true)
            hovered().markers()
                .type(MarkerType.CIRCLE)
                .size(10.0)
            tooltip()
                .position("right")
                .anchor(Anchor.LEFT_CENTER)
                .offsetX(0.0)
                .offsetY(0.0)
        }

        if (graphTimeline != 1) {
            seriesPrick.tooltip().format("Prick Value(Mean): {%value}")
        } else
            seriesPrick.tooltip().format("Prick Value: {%value}")

        seriesSensor.apply {
            data(series2Mapping)
            name("Sensor Graph")
            color("steelblue")
            legendItem().iconType(LegendItemIconType.LINE)
            hovered().markers().enabled(true)
            hovered().markers()
                .type(MarkerType.CIRCLE)
                .size(4.0)
            tooltip()
                .position("right")
                .anchor(Anchor.LEFT_CENTER)
                .offsetX(0.0)
                .offsetY(0.0)
        }

        seriesError.apply {
            data(series3Mapping)
            name("Difference")
            legendItem().iconType(LegendItemIconType.LINE)
            hovered().markers().enabled(true)
            hovered().markers()
                .type(MarkerType.CIRCLE)
                .size(4.0)
            tooltip()
                .position("right")
                .anchor(Anchor.LEFT_CENTER)
                .offsetX(0.0)
                .offsetY(0.0)
            markers().type(MarkerType.DIAGONAL_CROSS)
        }

        markerPrick.apply {
            data(series1Mapping)
            size(4)
            color("black")
            type(MarkerType.CIRCLE)
            legendItem().iconType(LegendItemIconType.BUBBLE)
            name("Prick Points")
            fill("function() {\n" +
                    "    var threshold = this.iterator.get('value');\n" +
                    "    if (threshold > 200)\n" +
                    "      return 'crimson';\n" +
                    "    else if (threshold < 200 && threshold > 140)\n" +
                    "      return 'coral';\n" +
                    "    return 'skyblue';\n" +
                    "  }")
        }

        markerSensor.apply {
            data(series2Mapping)
            size(4)
            color("black")
            type(MarkerType.CIRCLE)
            legendItem().iconType(LegendItemIconType.BUBBLE)
            name("Sensor Points")
            fill("function() {\n" +
                    "    var threshold = this.iterator.get('value2');\n" +
                    "    if (threshold > 200)\n" +
                    "      return 'crimson';\n" +
                    "    else if (threshold < 200 && threshold > 140)\n" +
                    "      return 'coral';\n" +
                    "    return 'steelblue';\n" +
                    "  }")
//            removeAllListeners()
            setOnClickListener(object : ListenersInterface.OnClickListener(arrayOf("x",
                "value")) {
                override fun onClick(event: Event?) {
                    Util.makeLog("Event: " + event?.data.toString())
                    if (graphTimeline == 2) {
                        setButtonBackgrounds(1)
                        rePlotGraph(1, getReadingsFromWeekDay(event?.data?.get("x")))
                    } else if (graphTimeline == 3) {
                        setButtonBackgrounds(1)
                        rePlotGraph(1, getReadingsFromMonthDay(event?.data?.get("x")))
                    } else if (graphTimeline == 4) {
                        setButtonBackgrounds(3)
                        rePlotGraph(3, getReadingsFromYearMonth(event?.data?.get("x")))
                    }
                }
            })

        }
    }

    private fun rePlotGraph(graphTimeline: Int, listOfReadingsToPlot: List<BGLReading>) {
        when (graphTimeline) {
            1 -> setCartesianProperties(getXAxisMajorLabel(listOfReadingsToPlot[0].Timestamp, 1),
                false)
            3 -> setCartesianProperties(getXAxisMajorLabel(listOfReadingsToPlot[0].Timestamp, 2),
                false)
        }

        val seriesData: ArrayList<DataEntry> = ArrayList()
        var counter = 1
        for (entry in listOfReadingsToPlot.indices) {
            seriesData.add(CustomDataEntry(getXAxis(listOfReadingsToPlot[entry].Timestamp,
                graphTimeline),
                listOfReadingsToPlot[entry].PrickValue,
                listOfReadingsToPlot[entry].SensorValue,
                listOfReadingsToPlot[entry].PrickValue - listOfReadingsToPlot[entry].SensorValue))
            counter++
        }

        val set: Set = Set.instantiate()
        set.data(seriesData)
        val series1Mapping: Mapping =
            set.mapAs("{ x: 'x', value: 'value' }")
        val series2Mapping: Mapping =
            set.mapAs("{ x: 'x', value: 'value2' }")
        val series3Mapping: Mapping =
            set.mapAs("{ x: 'x', value: 'value3' }")

        seriesPrick.apply {
            data(series1Mapping)
            name("Prick Graph")
            color("skyblue")
            legendItem().iconType(LegendItemIconType.LINE)
            hovered().markers().enabled(true)
            hovered().markers()
                .type(MarkerType.CIRCLE)
                .size(10.0)
            tooltip()
                .position("right")
                .anchor(Anchor.LEFT_CENTER)
                .offsetX(0.0)
                .offsetY(0.0)
        }

        if (graphTimeline != 1) {
            seriesPrick.tooltip().format("Prick Value(Mean): {%value}")
        } else
            seriesPrick.tooltip().format("Prick Value: {%value}")

        seriesSensor.apply {
            data(series2Mapping)
            name("Sensor Graph")
            color("steelblue")
            legendItem().iconType(LegendItemIconType.LINE)
            hovered().markers().enabled(true)
            hovered().markers()
                .type(MarkerType.CIRCLE)
                .size(4.0)
            tooltip()
                .position("right")
                .anchor(Anchor.LEFT_CENTER)
                .offsetX(0.0)
                .offsetY(0.0)
        }

        seriesError.apply {
            data(series3Mapping)
            name("Difference")
            legendItem().iconType(LegendItemIconType.LINE)
            hovered().markers().enabled(true)
            hovered().markers()
                .type(MarkerType.CIRCLE)
                .size(4.0)
            tooltip()
                .position("right")
                .anchor(Anchor.LEFT_CENTER)
                .offsetX(0.0)
                .offsetY(0.0)
            markers().type(MarkerType.DIAGONAL_CROSS)
        }

        markerPrick.apply {
            data(series1Mapping)
            size(4)
            color("black")
            type(MarkerType.CIRCLE)
            legendItem().iconType(LegendItemIconType.BUBBLE)
            name("Prick Points")
            fill("function() {\n" +
                    "    var threshold = this.iterator.get('value');\n" +
                    "    if (threshold > 200)\n" +
                    "      return 'crimson';\n" +
                    "    else if (threshold < 200 && threshold > 140)\n" +
                    "      return 'coral';\n" +
                    "    return 'skyblue';\n" +
                    "  }")
        }

        markerSensor.apply {
            data(series2Mapping)
            size(4)
            color("black")
            type(MarkerType.CIRCLE)
            legendItem().iconType(LegendItemIconType.BUBBLE)
            name("Sensor Points")
            fill("function() {\n" +
                    "    var threshold = this.iterator.get('value2');\n" +
                    "    if (threshold > 200)\n" +
                    "      return 'crimson';\n" +
                    "    else if (threshold < 200 && threshold > 140)\n" +
                    "      return 'coral';\n" +
                    "    return 'steelblue';\n" +
                    "  }")
            setOnClickListener(object : ListenersInterface.OnClickListener(arrayOf("x",
                "value")) {
                override fun onClick(event: Event?) {
                    Util.makeLog("Event: " + event?.data.toString())
                }
            })

        }
    }

    private fun setCartesianProperties(xAxisLabel: String, createNew: Boolean): Cartesian {
        if (createNew)
            cartesian = AnyChart.line()

        cartesian.animation(true)
        cartesian.padding(10.0, 10.0, 5.0, 10.0)
        cartesian.crosshair().enabled(true)
        cartesian.crosshair()
            .yLabel(true) // TODO ystroke
            .yStroke(null as Stroke?, null, null, null as String?, null as String?)
        cartesian.tooltip().positionMode(TooltipPositionMode.POINT)
        cartesian.title("Blood Glucose Data")
        cartesian.yAxis(0).title("Value(mg/dL)")

        if (xAxisLabel.isNotEmpty()) {
            Util.makeLog("isNotEmpty")
            cartesian.xAxis(0).title().enabled(true)
            cartesian.xAxis(0).title(xAxisLabel)
            cartesian.xAxis(0).title().fontSize(20)
        } else {
            Util.makeLog("isEmpty")
            cartesian.xAxis(0).title().enabled(false)
            cartesian.xAxis(0).title("")
        }
        cartesian.xAxis(0).labels().padding(5.0, 5.0, 20.0, 5.0)
        cartesian.yGrid(0).enabled(true)
//        cartesian.xGrid(0).enabled(true)

        cartesian.yGrid(0).stroke("{ dash: \"3 5\", color: 'lightgray' }")

//        cartesian.xScale(DateTime.instantiate())

//        cartesian.xAxis(0).labels().wordWrap("break-word")
//        cartesian.xAxis(0).labels().wordBreak("break-all")
//        cartesian.xAxis(0).labels().width(70)
//        cartesian.xAxis(0).labels()
//            .format("{%value}{dateTimeFormat:hh:mm:ss a MM-dd-yyyy}")


//        cartesian.xAxis(0).labels().format("function() {\n" +
//                "  var value1 = this.value;\n" +
//                "  // scale USD to EUR and rouns the result\n" +
//                "  var value2 = Date.parse(value1);\n" +
//                "  return value2;\n" +
//                "}")


/*        cartesian.xScroller(true)
        cartesian.xZoom(true)*/
        cartesian.legend().enabled(true)
        cartesian.legend().fontSize(13.0)
        cartesian.legend().padding(0.0, 0.0, 10.0, 10.0)
        cartesian.legend().align(Align.CENTER)

        return cartesian
    }

    private class CustomDataEntry(
        x: String?,
        value: Number?,
        value2: Number?,
        value3: Number?,
    ) :
        ValueDataEntry(x, value) {
        init {
            setValue("value2", value2)
            setValue("value3", value3)
        }
    }

    private fun initEditDialog() {
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        val v: View = layoutInflater.inflate(R.layout.new_reading_form, null)
        val etxtPrickVal = v.findViewById<View>(R.id.etxt_prick_value) as EditText
        etxtSensorVal = v.findViewById<View>(R.id.etxt_sensor_value) as EditText
        builder.setView(v)
        builder.setPositiveButton("Save", null)
        builder.setNeutralButton("Read BGL ", null)
        builder.setNegativeButton("Cancel", null)

        val dialog = builder.create()
        dialog.setOnShowListener {
            val buttonPositive: Button = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            val buttonNeutral: Button = dialog.getButton(AlertDialog.BUTTON_NEUTRAL)
            val buttonNegative: Button = dialog.getButton(AlertDialog.BUTTON_NEGATIVE)

            buttonPositive.setOnClickListener {
                if (etxtPrickVal.text.toString().isEmpty() || etxtSensorVal.text.toString()
                        .isEmpty()
                ) {
                    Toast.makeText(context, "Please fill the values", Toast.LENGTH_SHORT).show()
//                    BLEConnectionManager.writeCharToStartReceivingBGLValues(byteArrayOf(0x44))
                    return@setOnClickListener
                }
                val timestampString = Util.getCurrentTimeStamp()
                val valuesString =
                    etxtPrickVal.text.toString() + " - " + etxtSensorVal.text.toString()
                diabetesViewModel.insertReading(context,
                    BGLReading(patientId,
                        timestampString,
                        etxtPrickVal.text.toString().trim().toFloat(),
                        etxtSensorVal.text.toString().trim().toFloat(),
                        currentReadingTemperature,
                        currentReadingFingerWidth,
                        currentReadingVoltage,
                        0))

                diabetesViewModel.updatePatientLastReading(context,
                    patientId,
                    valuesString,
                    timestampString)
                BLEConnectionManager.writeCharToStartReceivingBGLValues(byteArrayOf(0x44))
                BLEConnectionManager.disconnect()
                dialog.dismiss()
            }
            buttonNeutral.setOnClickListener {
                checkLocationPermission()
            }
            buttonNegative.setOnClickListener {
                BLEConnectionManager.writeCharToStartReceivingBGLValues(byteArrayOf(0x44))
                BLEConnectionManager.disconnect()
                dialog.dismiss()
            }
        }
        dialog.show()
    }

    private fun checkLocationPermission() {
        when {
            isLocationPermissionEnabled() -> initBLEModule()
            ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) -> displayRationale()
            else -> requestLocationPermission()
        }
    }

    private fun isLocationPermissionEnabled(): Boolean {
        return ContextCompat.checkSelfPermission(this,
            Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestLocationPermission() {
        ActivityCompat.requestPermissions(this,
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            REQUEST_LOCATION_PERMISSION)
    }

    private fun displayRationale() {
        android.app.AlertDialog.Builder(this)
            .setMessage(getString(R.string.location_permission_disabled))
            .setPositiveButton(getString(R.string.ok)
            ) { _, _ -> requestLocationPermission() }
            .setNegativeButton(getString(R.string.cancel)
            ) { _, _ -> }
            .show()
    }

    private fun initBLEModule() {
        // BLE initialization
        if (!BLEDeviceManager.init(this)) {
            Toast.makeText(this, "BLE NOT SUPPORTED", Toast.LENGTH_SHORT).show()
            return
        }
        registerServiceReceiver()
        BLEDeviceManager.setListener(this)

        if (!BLEDeviceManager.isEnabled()) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
            Util.makeLog("starting enable ble request")
        } else {
            Util.makeLog("initING BLE service")
            BLEConnectionManager.initBLEService(this@PatientDetailActivity)
            scanDevice(false)
        }
    }

    private fun scanDevice(isContinuesScan: Boolean) = if (mDeviceAddress.isNotEmpty()) {
        connectDevice()
    } else {
        BLEDeviceManager.scanBLEDevice(isContinuesScan)
    }

    private fun connectDevice() {
        Handler().postDelayed({
            BLEConnectionManager.initBLEService(this@PatientDetailActivity)
            if (BLEConnectionManager.connect(mDeviceAddress)) {
                Toast.makeText(this@PatientDetailActivity, "DEVICE CONNECTED", Toast.LENGTH_SHORT)
                    .show()
            } else {
                Toast.makeText(this@PatientDetailActivity,
                    "DEVICE CONNECTION FAILED",
                    Toast.LENGTH_SHORT).show()
            }
        }, 100)
    }

    private fun registerServiceReceiver() {
        this.registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter())
    }

    private fun makeGattUpdateIntentFilter(): IntentFilter {
        val intentFilter = IntentFilter()
        intentFilter.addAction(BLEConstants.ACTION_GATT_CONNECTED)
        intentFilter.addAction(BLEConstants.ACTION_GATT_DISCONNECTED)
        intentFilter.addAction(BLEConstants.ACTION_GATT_SERVICES_DISCOVERED)
        intentFilter.addAction(BLEConstants.ACTION_DATA_AVAILABLE)
        intentFilter.addAction(BLEConstants.ACTION_DATA_WRITTEN)

        return intentFilter
    }

    private val mGattUpdateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                BLEConstants.ACTION_GATT_CONNECTED -> {
                    Log.i(TAG, "ACTION_GATT_CONNECTED ")
                    BLEConnectionManager.findBLEGattService(this@PatientDetailActivity)
                }
                BLEConstants.ACTION_GATT_DISCONNECTED -> {
                    Log.i(TAG, "ACTION_GATT_DISCONNECTED ")
                }
                BLEConstants.ACTION_GATT_SERVICES_DISCOVERED -> {
                    Log.i(TAG, "ACTION_GATT_SERVICES_DISCOVERED ")
                    try {
                        Thread.sleep(500)
                    } catch (e: InterruptedException) {
                        e.printStackTrace()
                    }
                    BLEConnectionManager.findBLEGattService(this@PatientDetailActivity)
                }
                BLEConstants.ACTION_DATA_AVAILABLE -> {
                    val data = intent.getByteArrayExtra(BLEConstants.EXTRA_DATA)
                    val uuId = intent.getStringExtra(BLEConstants.EXTRA_UUID)
                    try {
                        etxtSensorVal.setText(Util.hexToAscii(Util.byteArrayToHexString(data,
                            false)))
                    } catch (e: Exception) {
                        Log.e(TAG, "Exception: ${e.message}")
                        Log.e(TAG, "Exception: Val of data received: $data")
                    }
                }
                BLEConstants.ACTION_DATA_WRITTEN -> {
                    val data = intent.getStringExtra(BLEConstants.EXTRA_DATA)
                    Log.i(TAG, "ACTION_DATA_WRITTEN ")
                }
            }
        }
    }

    private fun initEditDeleteBGLReadingDialog(context: Context, reading: BGLReading) {
        val builder: AlertDialog.Builder = AlertDialog.Builder(context)
        val v: View = LayoutInflater.from(context).inflate(R.layout.edit_delete_form, null)
        var btnEdit = v.findViewById<View>(R.id.btn_edit) as Button
        var btnDelete = v.findViewById<View>(R.id.btn_delete) as Button

        builder.setView(v)
        val dialog = builder.create()
        dialog.show()

        btnEdit.setOnClickListener {
            initEditBGLReadingDialog(context, reading)
            dialog.dismiss()
        }
        btnDelete.setOnClickListener {
            initDeleteConfirmationDialog(context, reading)
            dialog.dismiss()
        }
    }

    private fun initEditBGLReadingDialog(context: Context, reading: BGLReading) {
        val builder: AlertDialog.Builder = AlertDialog.Builder(context)
        val v: View = LayoutInflater.from(context).inflate(R.layout.new_reading_form, null)
        var txtHeader = v.findViewById<View>(R.id.txt_header) as TextView
        txtHeader.text = "Edit BGL Reading"
        var txtPrick = v.findViewById<View>(R.id.etxt_prick_value) as TextView
        var txtSensor = v.findViewById<View>(R.id.etxt_sensor_value) as TextView

        txtPrick.text = reading.PrickValue.toString()
        txtSensor.text = reading.SensorValue.toString()

        builder.setView(v)
        builder.setPositiveButton("Update", null)
        builder.setNeutralButton("Read BGL ", null)
        builder.setNegativeButton("Cancel", null)
        val dialog = builder.create()

        dialog.setOnShowListener {
            val button: Button = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            val buttonNeutral: Button = dialog.getButton(AlertDialog.BUTTON_NEUTRAL)
            button.setOnClickListener {
                val etxtPrickValue = v.findViewById<View>(R.id.etxt_prick_value) as EditText
                etxtSensorVal = v.findViewById<View>(R.id.etxt_sensor_value) as EditText

                if (etxtPrickValue.text.toString().trim()
                        .isEmpty() || etxtSensorVal.text.toString().trim().isEmpty()
                ) {
                    Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show()
                } else {
                    val updatedReading = BGLReading(reading.PatientId,
                        reading.Timestamp,
                        etxtPrickValue.text.toString().trim().toFloat(),
                        etxtSensorVal.text.toString().trim().toFloat(),
                        currentReadingTemperature,
                        currentReadingFingerWidth,
                        currentReadingVoltage,
                        0)
                    updatedReading.Id = reading.Id
                    diabetesViewModel.updateReading(context, updatedReading)
                    dialog.dismiss()
                }
            }
            buttonNeutral.setOnClickListener {
                checkLocationPermission()
            }
        }
        dialog.show()
    }

    private fun initDeleteConfirmationDialog(context: Context, reading: BGLReading) {
        val builder: AlertDialog.Builder = AlertDialog.Builder(context)

        builder.setMessage("Are you sure?")
        builder.setPositiveButton("Yes") { _, _ ->
            reading.Id?.let { diabetesViewModel.deleteReadingWithId(context, it) }
        }
        builder.setNegativeButton("No") { dialog, _ ->
            dialog.dismiss()
        }
        val dialog = builder.create()
        dialog.show()
    }

    override fun onDelete(id: Int?) {
        if (id != null)
            diabetesViewModel.deletePatientWithId(context, id)
    }

    override fun onUpdate(patient: PatientModel) {
        diabetesViewModel.updatePatient(context, patient)
    }

    override fun onLongPress(reading: BGLReading) {
        initEditDeleteBGLReadingDialog(context, reading)
    }

    private fun setButtonBackgrounds(selected: Int) {
        when (selected) {
            1 -> {
                binding.btnToday.setBackgroundColor(context.resources.getColor(R.color.app_green))
                binding.btnWeek.setBackgroundColor(context.resources.getColor(R.color.app_grey_light))
                binding.btnMonth.setBackgroundColor(context.resources.getColor(R.color.app_grey_light))
                binding.btnYear.setBackgroundColor(context.resources.getColor(R.color.app_grey_light))
            }
            2 -> {
                binding.btnToday.setBackgroundColor(context.resources.getColor(R.color.app_grey_light))
                binding.btnWeek.setBackgroundColor(context.resources.getColor(R.color.app_green))
                binding.btnMonth.setBackgroundColor(context.resources.getColor(R.color.app_grey_light))
                binding.btnYear.setBackgroundColor(context.resources.getColor(R.color.app_grey_light))
            }
            3 -> {
                binding.btnToday.setBackgroundColor(context.resources.getColor(R.color.app_grey_light))
                binding.btnWeek.setBackgroundColor(context.resources.getColor(R.color.app_grey_light))
                binding.btnMonth.setBackgroundColor(context.resources.getColor(R.color.app_green))
                binding.btnYear.setBackgroundColor(context.resources.getColor(R.color.app_grey_light))
            }
            4 -> {
                binding.btnToday.setBackgroundColor(context.resources.getColor(R.color.app_grey_light))
                binding.btnWeek.setBackgroundColor(context.resources.getColor(R.color.app_grey_light))
                binding.btnMonth.setBackgroundColor(context.resources.getColor(R.color.app_grey_light))
                binding.btnYear.setBackgroundColor(context.resources.getColor(R.color.app_green))
            }
            else -> {
                binding.btnToday.setBackgroundColor(context.resources.getColor(R.color.app_green))
                binding.btnWeek.setBackgroundColor(context.resources.getColor(R.color.app_grey_light))
                binding.btnMonth.setBackgroundColor(context.resources.getColor(R.color.app_grey_light))
                binding.btnYear.setBackgroundColor(context.resources.getColor(R.color.app_grey_light))
            }
        }
    }

}