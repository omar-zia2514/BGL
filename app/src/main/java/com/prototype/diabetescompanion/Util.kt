package com.prototype.diabetescompanion

import android.util.Log

class Util {
    companion object {
        fun makeLog(logMessage: String) {
            Log.d("diabetesDebug", logMessage)
        }
    }
}