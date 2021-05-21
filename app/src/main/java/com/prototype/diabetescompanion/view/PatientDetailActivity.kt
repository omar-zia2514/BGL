package com.prototype.diabetescompanion.view

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.content.*
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.EditText
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
import com.jjoe64.graphview.GraphView
import com.jjoe64.graphview.LegendRenderer
import com.jjoe64.graphview.series.DataPoint
import com.jjoe64.graphview.series.LineGraphSeries
import com.prototype.diabetescompanion.DataPointWithTime
import com.prototype.diabetescompanion.R
import com.prototype.diabetescompanion.Util
import com.prototype.diabetescompanion.adapter.PatientReadingsAdapter
import com.prototype.diabetescompanion.ble.*
import com.prototype.diabetescompanion.databinding.ActivityPatientDetailBinding
import com.prototype.diabetescompanion.model.BGLReading
import com.prototype.diabetescompanion.model.PatientModel
import com.prototype.diabetescompanion.viewmodel.DiabetesViewModel
import java.util.*


class PatientDetailActivity : AppCompatActivity(), OnDeviceScanListener {
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
        diabetesViewModel.getAllReadingsLiveDataWithPatientId(context, patientId).observe(this, {
            val patientReadingsAdapter = PatientReadingsAdapter(it, context)
            binding.patientReadingsRecyclerview.adapter = patientReadingsAdapter
            allReadingsList = it
        })

        layoutManager = LinearLayoutManager(this)
        binding.patientReadingsRecyclerview.layoutManager = layoutManager
        binding.patientReadingsRecyclerview.itemAnimator = DefaultItemAnimator()

        binding.patientReadingsRecyclerview.addItemDecoration(DividerItemDecoration(binding.patientReadingsRecyclerview.context,
            DividerItemDecoration.VERTICAL))
        binding.extendedFab.setOnClickListener { initEditDialog() }
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

    override fun onScanCompleted(bglDevice: BGLSensorDevice) {
        Util.makeLog("onScanCompleted(): Going to connect now ")
        //Initiate a dialog Fragment from here and ask the user to select his device
        // If the application already know the Mac address, we can simply call connect device

        mDeviceAddress = bglDevice.mDeviceAddress
        BLEConnectionManager.connect(bglDevice.mDeviceAddress)
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
            dataPointsRadius = 7.toFloat()
            color = getColor(R.color.app_green)
            title = "Prick Values"
            isDrawBackground = true
            backgroundColor = getColor(R.color.app_grey)
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
            dataPointsRadius = 7.toFloat()
            color = getColor(R.color.red)
            title = "Sensor Values"
            isDrawBackground = true
            backgroundColor = getColor(R.color.app_grey_light)
        }

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
            viewport.backgroundColor = getColor(R.color.design_default_color_secondary)
            viewport.setDrawBorder(true)
            viewport.borderColor = getColor(R.color.design_default_color_primary_variant)
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
        dialog.setOnShowListener(DialogInterface.OnShowListener {
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
                        etxtPrickVal.text.toString().trim().toInt(),
                        etxtSensorVal.text.toString().trim().toInt()))

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
        })
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
            val action = intent.action
            when {
                BLEConstants.ACTION_GATT_CONNECTED == action -> {
                    Log.i(TAG, "ACTION_GATT_CONNECTED ")
//                    BLEConnectionManager.findBLEGattService(this@PatientDetailActivity)
                }
                BLEConstants.ACTION_GATT_DISCONNECTED == action -> {
                    Log.i(TAG, "ACTION_GATT_DISCONNECTED ")
                }
                BLEConstants.ACTION_GATT_SERVICES_DISCOVERED == action -> {
                    Log.i(TAG, "ACTION_GATT_SERVICES_DISCOVERED ")
                    try {
                        Thread.sleep(500)
                    } catch (e: InterruptedException) {
                        e.printStackTrace()
                    }
                    BLEConnectionManager.findBLEGattService(this@PatientDetailActivity)
                }
                BLEConstants.ACTION_DATA_AVAILABLE == action -> {
                    val data = intent.getStringExtra(BLEConstants.EXTRA_DATA)
                    val uuId = intent.getStringExtra(BLEConstants.EXTRA_UUID)
                    Log.i(TAG, "ACTION_DATA_AVAILABLE $data")
                    etxtSensorVal.setText(data)
                }
                BLEConstants.ACTION_DATA_WRITTEN == action -> {
                    val data = intent.getStringExtra(BLEConstants.EXTRA_DATA)
                    Log.i(TAG, "ACTION_DATA_WRITTEN ")
                }
            }
        }
    }
}