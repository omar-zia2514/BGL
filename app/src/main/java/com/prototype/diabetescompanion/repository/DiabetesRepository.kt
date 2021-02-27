package com.prototype.diabetescompanion.repository

import android.content.Context
import androidx.lifecycle.LiveData
import com.prototype.diabetescompanion.model.BGLReading
import com.prototype.diabetescompanion.model.PatientModel
import com.prototype.diabetescompanion.room.DiabetesDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DiabetesRepository {
    companion object {
        fun insertData(context: Context, patient: PatientModel) {
            CoroutineScope(Dispatchers.IO).launch {
                DiabetesDatabase.getDatabase(context).diabetesDAO().insertPatient(patient)
            }
        }

        fun getAllPatients(context: Context): LiveData<List<PatientModel>> {
            return DiabetesDatabase.getDatabase(context).diabetesDAO().getAllPatients()
        }

        fun getAllReadingsWithPatientId(context: Context, patientId: Int): LiveData<List<BGLReading>> {
            return DiabetesDatabase.getDatabase(context).diabetesDAO().getAllReadingsWithPatientId(patientId)
        }
    }
}