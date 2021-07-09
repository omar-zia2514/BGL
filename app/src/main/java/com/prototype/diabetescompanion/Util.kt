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
    }
}