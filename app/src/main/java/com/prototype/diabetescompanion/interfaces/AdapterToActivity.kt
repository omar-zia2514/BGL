package com.prototype.diabetescompanion.interfaces

import com.prototype.diabetescompanion.model.BGLReading
import com.prototype.diabetescompanion.model.PatientModel

interface AdapterToActivity {
    fun onDelete(id: Int?)

    fun onUpdate(patient: PatientModel)

    fun onLongPress(reading: BGLReading)

}