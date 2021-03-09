package com.prototype.diabetescompanion.interfaces

import com.prototype.diabetescompanion.model.PatientModel

interface AdapterToActivity {
    fun onDelete(id: Int?)

    fun onUpdate(patient: PatientModel)
}