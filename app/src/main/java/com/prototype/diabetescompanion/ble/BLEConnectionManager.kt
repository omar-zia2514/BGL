package com.prototype.diabetescompanion.ble

import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattCharacteristic.*
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Handler
import android.os.IBinder
import android.util.Log
import com.prototype.diabetescompanion.Util

object BLEConnectionManager {

    private val TAG = "BLEConnectionManager"
    private var mBLEService: BLEService? = null
    private var isBind = false
    private var charBGL: BluetoothGattCharacteristic? = null
    private var mHandler: Handler? = null

    private val mServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(componentName: ComponentName, service: IBinder) {
            Util.makeLog("onServiceConnected()")
            mBLEService = (service as BLEService.LocalBinder).getService()

            if (!mBLEService?.initialize()!!) {
                Log.e(TAG, "Unable to initialize")
            }
        }

        override fun onServiceDisconnected(componentName: ComponentName) {
            mBLEService = null
        }
    }

    /**
     * Initialize Bluetooth service.
     */
    fun initBLEService(context: Context) {
        try {
            if (mBLEService == null) {
                val gattServiceIntent = Intent(context, BLEService::class.java)

                isBind = context.bindService(gattServiceIntent, mServiceConnection,
                    Context.BIND_AUTO_CREATE)
            }

        } catch (e: Exception) {
            e.message?.let { Log.e(TAG, it) }
        }

    }

    /**
     * Unbind BLE Service
     */
    fun unBindBLEService(context: Context) {

        if (isBind) {
            context.unbindService(mServiceConnection)
        }

        mBLEService = null
    }

    /**
     * Connect to a BLE Device
     */
    fun connect(deviceAddress: String): Boolean {
        var result = false
        Util.makeLog("Going to connect to: $deviceAddress")
        if (mBLEService != null) {
            Util.makeLog("mBLEService is not null")
            result = mBLEService!!.connect(deviceAddress)
        }
        Util.makeLog("Result of connect command: $result")
        return result
    }

    /**
     * Disconnect
     */
    fun disconnect() {
        if (null != mBLEService) {
            mBLEService!!.disconnect()
//            mBLEService = null
        }

    }

    fun writeCharToStartReceivingBGLValues(value: ByteArray) {
        Util.makeLog("writeCharToStartReceivingBGLValues()")
        if (charBGL != null) {
            charBGL!!.value = value
            writeBLECharacteristic(charBGL)
        }
    }

    fun writeBatteryLevel(batteryLevel: String) {
        if (batteryLevel != null) {
//            writeBLECharacteristic(mDataMDLPForMissedConnection);
        }
    }

    fun writeMissedConnection(value: String) {
        var gattCharacteristic = BluetoothGattCharacteristic(java.util.UUID.fromString(value),
            PROPERTY_WRITE,
            PERMISSION_WRITE)
        if (gattCharacteristic != null) {
            gattCharacteristic.setValue(value)
            writeBLECharacteristic(gattCharacteristic)
        }
    }

    fun writeCharToStartReceivingBGLValues(value: String) {
        var mDataMDLPForEmergency = BluetoothGattCharacteristic(java.util.UUID.fromString(value),
            PROPERTY_READ,
            PERMISSION_READ)
        mDataMDLPForEmergency.setValue(value)
        writeBLECharacteristic(mDataMDLPForEmergency)
    }

    /**
     * Write BLE Characteristic.
     */
    private fun writeBLECharacteristic(characteristic: BluetoothGattCharacteristic?) {
        Util.makeLog("writeBLECharacteristic()")
        if (null != characteristic) {
            if (mBLEService != null) {
                mBLEService?.writeCharacteristic(characteristic)
            }
        }
    }

    fun readCharBGLValue(UUID: String) {
        Util.makeLog("readCharBGLValue()")
        val gattCharacteristic = BluetoothGattCharacteristic(java.util.UUID.fromString(UUID),
            PROPERTY_READ,
            PERMISSION_READ)
        readBLECharacteristic(charBGL)
    }

    fun readBatteryLevel(UUID: String) {
        var gattCharacteristic = BluetoothGattCharacteristic(java.util.UUID.fromString(UUID),
            PROPERTY_READ,
            PERMISSION_READ)
        if (gattCharacteristic != null) {
            readBLECharacteristic(gattCharacteristic)
        }
    }

    fun readEmergencyGatt(UUID: String) {
        var gattCharacteristic = BluetoothGattCharacteristic(java.util.UUID.fromString(UUID),
            PROPERTY_READ,
            PERMISSION_READ)
        if (gattCharacteristic != null) {
            readBLECharacteristic(gattCharacteristic)
        }
    }

    /**
     * Read MLDP Characteristic.
     */
    private fun readBLECharacteristic(characteristic: BluetoothGattCharacteristic?) {
        if (null != characteristic) {
            if (mBLEService != null) {
                mBLEService?.readCharacteristic(characteristic)
            }
        }
    }


    /**
     * findBLEGattService
     */
    fun findBLEGattService(mContext: Context) {

        if (mBLEService == null) {
            return
        }

        if (mBLEService!!.getSupportedGattServices() == null) {
            return
        }

        var uuid: String
        charBGL = null
        val serviceList = mBLEService!!.getSupportedGattServices()

        if (serviceList != null) {
            for (gattService in serviceList) {
                Util.makeLog("gatt service: ${gattService.uuid.toString()}")
                if (gattService.uuid.toString()
                        .equals(BLEConstants.BGL_SERVICE_UUID,
                            ignoreCase = true)
                ) {
                    Util.makeLog("BGL gatt service found!!!")
                    val gattCharacteristics = gattService.characteristics
                    for (gattCharacteristic in gattCharacteristics) {
                        Util.makeLog("gatt characteristic: ${gattCharacteristic.uuid?.toString()}")
                        Util.makeLog("gatt characteristic properties: ${gattCharacteristic.properties}")

                        uuid =
                            if (gattCharacteristic.uuid != null) gattCharacteristic.uuid.toString() else ""

                        if (uuid.equals(BLEConstants.BGL_CHAR_UUID,
                                ignoreCase = true)
                        ) {
                            var newChar = gattCharacteristic
//                            newChar = setProperties(newChar)
                            charBGL = newChar
                            writeCharToStartReceivingBGLValues(byteArrayOf(0x4E))
                            mBLEService?.setCharacteristicNotification(gattCharacteristic, true)
                            mHandler = Handler()
                            mHandler?.postDelayed(
                                readRun, 2000) // Delay Period
                        }
                    }
                }
            }
        }
    }

    private var readRun = Runnable {
        readCharBGLValue(BLEConstants.BGL_CHAR_UUID)
//        mHandler?.postDelayed(readRun, 2000)
    }

    private fun setProperties(gattCharacteristic: BluetoothGattCharacteristic):
            BluetoothGattCharacteristic {
        val characteristicProperties = gattCharacteristic.properties

        if (characteristicProperties and PROPERTY_NOTIFY > 0) {
            mBLEService?.setCharacteristicNotification(gattCharacteristic, true)
        }

//        if (characteristicProperties and PROPERTY_INDICATE > 0) {
//            mBLEService?.setCharacteristicIndication(gattCharacteristic, true)
//        }

/*        if (characteristicProperties and PROPERTY_WRITE > 0) {
            Util.makeLog("Setting write type")
            gattCharacteristic.writeType = WRITE_TYPE_NO_RESPONSE
        }*/

        /*if (characteristicProperties and PROPERTY_WRITE_NO_RESPONSE > 0) {
            gattCharacteristic.writeType = WRITE_TYPE_NO_RESPONSE
        }
        if (characteristicProperties and PROPERTY_READ > 0) {
            gattCharacteristic.writeType = PROPERTY_READ
        }*/
        return gattCharacteristic
    }

}