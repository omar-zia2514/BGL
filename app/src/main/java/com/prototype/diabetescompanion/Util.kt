package com.prototype.diabetescompanion

import android.util.Log
import java.text.SimpleDateFormat
import java.util.*

class Util {
    companion object {
        fun makeLog(logMessage: String) {
            Log.d("diabetesDebug", logMessage)
        }

        /*    public String getCurrentLocalDateTimeStamp() {
        return LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS"));
    }*/
        fun getCurrentTimeStamp(): String {
            val tmp = SimpleDateFormat("EEE, MMM d, ''yy 'at' h:mm:ss a").format(Date())
            makeLog("Current timestamp when saving new wreading")
            return tmp
        }

        fun byteArrayToHexString(data: ByteArray?, addSpace: Boolean): String? {
            var str = ""
            if (data != null) {
                for (aData in data) {
                    str += if (addSpace) byteToHex(aData) + " " else byteToHex(
                        aData)
                }
            }
            return str
        }

        private fun byteToHex(i: Byte): String? {
            val sb = StringBuilder()
            sb.append(Integer.toHexString(i.toInt()))
            if (sb.length < 2) {
                sb.insert(0, '0') // pad with leading zero if needed
            }
            return if (i < 0) sb.toString().substring(6, 8)
                .toUpperCase(Locale.getDefault()) else sb.toString().toUpperCase(
                Locale.getDefault())
        }
    }
}