package com.prototype.diabetescompanion.ble

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.content.pm.PackageManager
import android.os.Handler
import android.util.Log
import com.prototype.diabetescompanion.Util
import java.util.*

object BLEScanManager {

    private val TAG = "BLEDeviceManager"
    private var scanCallback: ScanCallback? = null
    private var mDeviceObject: BGLSensorDevice? = null
    private var mBluetoothAdapter: BluetoothAdapter? = null
    private var mHandler: Handler? = null
    private var mOnDeviceScanListener: OnDeviceScanListener? = null
    private var mLeScanCallback: BluetoothAdapter.LeScanCallback? = null
    private var mIsContinuesScan: Boolean = false
    private var isScanning = false

    init {
        mHandler = Handler()
        createScanCallBackAboveLollipop()
    }

    /**
     * ScanCallback for Lollipop and above
     * The Callback will trigger the Nearest available BLE devices
     * Search the BLE device in Range and pull the Name and Mac Address from it
     */
    private fun createScanCallBackAboveLollipop() {
        scanCallback = object : ScanCallback() {
            override fun onScanResult(callbackType: Int, result: ScanResult?) {

                Util.makeLog("ScanResult ${result?.device?.name}")
                Util.makeLog("ScanResult ${result?.device?.address}")

                if (null != mOnDeviceScanListener && result != null &&
                    result.device != null && result.device.address != null
                ) {
                    val data = BGLSensorDevice()
                    data.mDeviceName = if (result.device.name != null)
                        result.device.name else "Unknown"
                    // Some case the Device Name will return as Null from BLE
                    // because of Swathing from one device to another
                    data.mDeviceAddress = (result.device.address)
                    /**
                     * Save the Valid Device info into a list
                     * The List will display to the UI as a popup
                     * User has an option to select one BLE from the popup
                     * After selecting one BLE, the connection will establish and
                     * communication channel will create if its valid device.
                     */

                    if (data.mDeviceName.contains("BT05")) {
                        mDeviceObject = data
                        stopScan(mDeviceObject)
                    }
                }
            }
        }
    }

    /**
     * Initialize BluetoothAdapter
     * Check the device has the hardware feature BLE
     * Then enable the hardware,
     */
    fun init(context: Context): Boolean {
        val bluetoothManager =
            context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        mBluetoothAdapter = bluetoothManager.adapter

        return mBluetoothAdapter != null && context.packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)
    }

    /**
     * Check bluetooth is enabled or not.
     */
    fun isEnabled(): Boolean {

        return mBluetoothAdapter != null && mBluetoothAdapter!!.isEnabled
    }

    /**
     * setListener
     */
    fun setListener(onDeviceScanListener: OnDeviceScanListener) {
        if (mOnDeviceScanListener == null)
            mOnDeviceScanListener = onDeviceScanListener
    }

    /**
     * Scan The BLE Device
     * Check the available BLE devices in the Surrounding
     * If the device is Already scanning then stop Scanning
     * Else start Scanning and check 10 seconds
     * Send the available devices as a callback to the system
     * Finish Scanning after 10 Seconds
     */
    fun scanBLEDevice(isContinuesScan: Boolean) {
        Util.makeLog("scanBLEDevice()")
        try {
            mIsContinuesScan = isContinuesScan

            if (mBluetoothAdapter != null && mBluetoothAdapter!!.isEnabled) {
                scan()
            }
            /**
             * Stop Scanning after a Period of Time
             * Set a 10 Sec delay time and Stop Scanning
             * collect all the available devices in the 10 Second
             */
            if (!isContinuesScan) {
                mHandler?.postDelayed({
                    // Set a delay time to Scanning
                    stopScan(mDeviceObject)
                }, BLEConstants.SCAN_PERIOD) // Delay Period
            }
        } catch (e: Exception) {
            e.message?.let { Log.e(TAG, it) }
        }

    }

    private fun scan() {
        Util.makeLog("scan()")
        isScanning = true
        mBluetoothAdapter?.bluetoothLeScanner?.startScan(null,
            scanSettings(), scanCallback) // Start BLE device Scanning in a separate thread
    }

    private fun scanFilters(): List<ScanFilter> {
        val filter =
            ScanFilter.Builder().setDeviceName("MLT-BT05").build()
//                .setServiceUuid(ParcelUuid.fromString(BLEConstants.BGL_SERVICE_UUID)).build()
        val list = ArrayList<ScanFilter>(1)
        list.add(filter)
        return list
    }

    private fun scanSettings(): ScanSettings {
        return ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_BALANCED).build()
    }

    private fun stopScan(bglSensorDevice: BGLSensorDevice?) {
        try {
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            if (isScanning && mBluetoothAdapter != null && mBluetoothAdapter!!.isEnabled && scanCallback != null) {
                isScanning = false
                mBluetoothAdapter!!.bluetoothLeScanner.stopScan(scanCallback)
                if (bglSensorDevice != null) {
                    mOnDeviceScanListener?.onScanCompleted(bglSensorDevice)
                }
            }
        }
    }
}