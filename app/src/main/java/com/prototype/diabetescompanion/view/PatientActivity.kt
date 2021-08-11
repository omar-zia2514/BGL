package com.prototype.diabetescompanion.view

import android.Manifest
import android.app.ProgressDialog
import android.bluetooth.BluetoothAdapter
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
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
import com.android.volley.DefaultRetryPolicy
import com.android.volley.toolbox.JsonObjectRequest
import com.prototype.diabetescompanion.*
import com.prototype.diabetescompanion.adapter.PatientReadingsAdapter
import com.prototype.diabetescompanion.ble.*
import com.prototype.diabetescompanion.databinding.ActivityPatientBinding
import com.prototype.diabetescompanion.interfaces.AdapterToActivity
import com.prototype.diabetescompanion.model.BGLReading
import com.prototype.diabetescompanion.model.PatientModel
import com.prototype.diabetescompanion.viewmodel.DiabetesViewModel

class PatientActivity : AppCompatActivity(), OnDeviceScanListener, AdapterToActivity {

    private lateinit var binding: ActivityPatientBinding

    private lateinit var context: Context
    lateinit var diabetesViewModel: DiabetesViewModel
    private var patientId: Int = 0
    private lateinit var _patient: PatientModel
    private lateinit var allReadingsList: List<BGLReading>
    private var currentMode = 0
    private var mDeviceAddress: String = ""
    private var cardDownloadProgress: ProgressDialog? = null

    private var layoutManager: RecyclerView.LayoutManager? = null
    lateinit var pDialog: ProgressDialog
    private lateinit var etxtSensorVal: EditText

    private val REQUEST_LOCATION_PERMISSION = 2018
    private val REQUEST_ENABLE_BT = 1000
    private val TAG = "diabetesDebug"

    var currentReadingTemperature: Float = 0F
    var currentReadingFingerWidth: Float = 0F
    var currentReadingVoltage: Float = 0F
    var currentReadingDeviceId: Float = 0F

    private var state: Int = 0
    private val ASent: Int = 1
    private val AReceived: Int = 2
    private val BSent: Int = 3
    private val BReceived: Int = 4
    private val CSent: Int = 5
    private val CReceived: Int = 6
    private val DSent: Int = 7
    private val DReceived: Int = 8
    private val ESent: Int = 9
    private val EReceived: Int = 10

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPatientBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        supportActionBar?.title = resources.getString(R.string.patients_screen_title)

        context = this@PatientActivity

        diabetesViewModel = ViewModelProvider(this).get(DiabetesViewModel::class.java)

        val readingsAdapter = PatientReadingsAdapter(context)
        binding.patientReadingsRecyclerview.adapter = readingsAdapter

        diabetesViewModel.getOwnerPatientIdLiveData(context).observe(this, {
            patientId = it
            diabetesViewModel.getAllReadingsLiveDataWithPatientId(context, patientId)
                .observe(this, {
                    (binding.patientReadingsRecyclerview.adapter as PatientReadingsAdapter).setAdapterData(
                        it)
                    allReadingsList = it
                })
        })

        layoutManager = LinearLayoutManager(this)
        binding.patientReadingsRecyclerview.layoutManager = layoutManager
        binding.patientReadingsRecyclerview.itemAnimator = DefaultItemAnimator()

        binding.patientReadingsRecyclerview.addItemDecoration(DividerItemDecoration(binding.patientReadingsRecyclerview.context,
            DividerItemDecoration.VERTICAL))

        binding.extendedFab.setOnClickListener { initNewBGLReadingDialog()}
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
            R.id.sync -> {
                pDialog = ProgressDialog(context)
                pDialog.isIndeterminate = true
                pDialog.setMessage("Sync in progress...")
                pDialog.show()
                syncPatientData()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    fun syncPatientData() {
        val url = "http://diabetescompanions.uk/api/SyncRecord"

        var patientData = diabetesViewModel.getPatientData(context, patientId)
        if (patientData.readings.size < 1) {
            Toast.makeText(applicationContext, "Already synced", Toast.LENGTH_LONG)
                .show()
            pDialog.dismiss()
            return
        }

        val request = object :
            JsonObjectRequest(Method.POST, url, JsonManager.getPatientSyncJson(patientData),
                { response ->
                    // Process the json
                    try {
                        Util.makeLog("Response: $response")
                        diabetesViewModel.updateSyncStatusPatient(context, patientId)
                        diabetesViewModel.updateOnlineIdsPatientSync(context, patientData, response)
                        pDialog.dismiss()
                    } catch (e: Exception) {
                        Util.makeLog("Exception: $e")
                        pDialog.dismiss()
                    }

                }, {
                    // Error in request
                    Util.makeLog("Volley error1: $it")

                    Toast.makeText(applicationContext,
                        "error,$it",
                        Toast.LENGTH_LONG)
                        .show()
                    pDialog.dismiss()
                    // Toast.makeText(applicationContext, obj.getString("message"), Toast.LENGTH_LONG).show()
                }) {
            override fun getHeaders(): MutableMap<String, String> {
                val params: MutableMap<String, String> = java.util.HashMap()
//                params["Content-Type"] = "application/x-www-form-urlencoded"
                params["Content-Type"] = "application/json"
                return params
            }
        }

        // Volley request policy, only one time request to avoid duplicate transaction
        request.retryPolicy = DefaultRetryPolicy(
            DefaultRetryPolicy.DEFAULT_TIMEOUT_MS,
            // 0 means no retry
            DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        )

        // Add the volley post request to the request queue
        VolleySingleton.getInstance(this).addToRequestQueue(request)
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
        /*val series: LineGraphSeries<DataPoint> = LineGraphSeries()
        series.apply {
            thickness = 4
            isDrawDataPoints = true
            dataPointsRadius = 6.toFloat()
            color = getColor(R.color.app_green)
            title = "Prick Values"
        }

        var counter = 1
        var maxY = 0f
        var minY = 400f

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
            dataPointsRadius = 6.toFloat()
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
        graph.addSeries(seriesSensor)*/
    }

    private fun initNewBGLReadingDialog() {
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
                        currentReadingDeviceId,
                        0))

                diabetesViewModel.updatePatientLastReading(context,
                    patientId,
                    valuesString,
                    timestampString)
                BLEConnectionManager.disconnect()
                dialog.dismiss()
            }
            buttonNeutral.setOnClickListener {
                checkLocationPermission()
            }
            buttonNegative.setOnClickListener {
                BLEConnectionManager.disconnect()
                dialog.dismiss()
            }
        }
        dialog.show()
    }


    private fun initEditDeletePatientDialog(context: Context, reading: BGLReading) {
        val builder: AlertDialog.Builder = AlertDialog.Builder(context)
        val v: View = LayoutInflater.from(context).inflate(R.layout.edit_delete_form, null)
        var btnEdit = v.findViewById<View>(R.id.btn_edit) as Button
        var btnDelete = v.findViewById<View>(R.id.btn_delete) as Button

        builder.setView(v)
        val dialog = builder.create()
        dialog.show()

        btnEdit.setOnClickListener {
            initEditPatientDialog(context, reading)
            dialog.dismiss()
        }
        btnDelete.setOnClickListener {
            initDeleteConfirmationDialog(context, reading)
            dialog.dismiss()
        }
    }

    private fun initEditPatientDialog(context: Context, reading: BGLReading) {
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
                val etxtSensorValue = v.findViewById<View>(R.id.etxt_sensor_value) as EditText

                if (etxtPrickValue.text.toString().trim()
                        .isEmpty() || etxtSensorValue.text.toString().trim().isEmpty()
                ) {
                    Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show()
                } else {
                    val updatedReading = BGLReading(reading.PatientId,
                        reading.Timestamp,
                        etxtPrickValue.text.toString().trim().toFloat(),
                        etxtSensorValue.text.toString().trim().toFloat(),
                        0F, 0F, 0F, 0f, 0)
                    updatedReading.Id = reading.Id
                    diabetesViewModel.updateReading(context, updatedReading)
                    dialog.dismiss()
                }
            }
            buttonNeutral.setOnClickListener {
//                checkLocationPermission()
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
        initEditDeletePatientDialog(context, reading)
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
        if (!BLEScanManager.init(this)) {
            Toast.makeText(this, "BLE NOT SUPPORTED", Toast.LENGTH_SHORT).show()
            return
        }
        registerServiceReceiver()
        BLEScanManager.setListener(this)

        if (!BLEScanManager.isEnabled()) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
            Util.makeLog("Starting enable ble request")
        } else {
            Util.makeLog("initING BLE service")
            BLEConnectionManager.initBLEService(this@PatientActivity)
            scanDevice(false)
        }
    }

    private fun scanDevice(isContinuesScan: Boolean) = if (mDeviceAddress.isNotEmpty()) {
        connectDevice()
    } else {
        BLEScanManager.scanBLEDevice(isContinuesScan)
    }

    private fun connectDevice() {
        Handler().postDelayed({
//            BLEConnectionManager.initBLEService(this@PatientDetailActivity) //No need to call here as already called in initBLEModule
            if (BLEConnectionManager.connect(mDeviceAddress)) {
                Util.makeLog("DEVICE CONNECTED")
            } else {
                Util.makeLog("DEVICE COULD NOT CONNECT")
                Toast.makeText(this@PatientActivity,
                    "Device could not connect. Please retry.",
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
//                    BLEConnectionManager.findBLEGattService(this@PatientDetailActivity)
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
                    showProgressLoader()
                    state = ESent
                    BLEConnectionManager.findBLEGattService(this@PatientActivity)
//                    BLEConnectionManager.writeCharToStartReceivingBGLValues(byteArrayOf(0x4E))
                }
                BLEConstants.ACTION_DATA_AVAILABLE -> {
                    val data = intent.getByteArrayExtra(BLEConstants.EXTRA_DATA)
                    when (state) {
                        ESent -> {
                            state = EReceived
                            currentReadingDeviceId =
                                Util.hexToAscii(Util.byteArrayToHexString(data,
                                    false)).toFloat()
                            state = BSent
                            BLEConnectionManager.writeCharToStartReceivingBGLValues(byteArrayOf(0x41)) //body temp
                        }
                        BSent -> {
                            state = BReceived
                            currentReadingTemperature =
                                Util.hexToAscii(Util.byteArrayToHexString(data,
                                    false)).toFloat()
                            state = CSent
                            BLEConnectionManager.writeCharToStartReceivingBGLValues(byteArrayOf(0x43)) //finger width
                        }
                        CSent -> {
                            state = CReceived
                            currentReadingFingerWidth =
                                Util.hexToAscii(Util.byteArrayToHexString(data,
                                    false)).toFloat()
                            state = DSent
                            BLEConnectionManager.writeCharToStartReceivingBGLValues(byteArrayOf(0x44)) //voltage
                        }
                        DSent -> {
                            state = DReceived
                            currentReadingVoltage =
                                Util.hexToAscii(Util.byteArrayToHexString(data,
                                    false)).toFloat()
                            state = ASent
                            BLEConnectionManager.writeCharToStartReceivingBGLValues(byteArrayOf(0x4E)) //BGL
                        }
                        ASent -> {
                            state = AReceived
                            etxtSensorVal.setText(Util.hexToAscii(Util.byteArrayToHexString(data,
                                false)))
                            Util.makeLog("Temp: $currentReadingTemperature")
                            Util.makeLog("Finger: $currentReadingFingerWidth")
                            Util.makeLog("Voltage: $currentReadingVoltage")
                            Util.makeLog("DeviceId: $currentReadingDeviceId")
                            Util.makeLog("BGL: ${etxtSensorVal.text.toString()}")

                            BLEConnectionManager.disconnect()
                            dismissProgressLoader()
                        }
                    }
                }
                BLEConstants.ACTION_DATA_WRITTEN -> {
                    val data = intent.getByteArrayExtra(BLEConstants.EXTRA_DATA)
                    Log.i(TAG, "ACTION_DATA_WRITTEN ")
                }
            }
        }
    }

    private fun showProgressLoader() {
        if (!isFinishing) {
            cardDownloadProgress = ProgressDialog(this@PatientActivity)
            cardDownloadProgress!!.setMessage("Getting BGL data...")
            cardDownloadProgress!!.setIndeterminate(true)
            cardDownloadProgress!!.setProgressStyle(ProgressDialog.STYLE_SPINNER)
            cardDownloadProgress!!.setCancelable(false)
            cardDownloadProgress!!.setCanceledOnTouchOutside(false);
            cardDownloadProgress!!.show()
        }
    }

    private fun dismissProgressLoader() {
        if (cardDownloadProgress != null) cardDownloadProgress!!.dismiss()
    }
}