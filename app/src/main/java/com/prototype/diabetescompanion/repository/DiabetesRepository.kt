package com.prototype.diabetescompanion.repository

import android.content.Context
import androidx.lifecycle.LiveData
import com.prototype.diabetescompanion.model.BGLReading
import com.prototype.diabetescompanion.model.DoctorModel
import com.prototype.diabetescompanion.model.PatientLastReadingVTable
import com.prototype.diabetescompanion.model.PatientModel
import com.prototype.diabetescompanion.room.DiabetesDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DiabetesRepository {
    companion object {
        fun insertPatient(context: Context, patient: PatientModel) {
            CoroutineScope(Dispatchers.IO).launch {
                DiabetesDatabase.getDatabase(context).diabetesDAO().insertPatient(patient)
            }
        }

        fun insertDoctor(context: Context, doctor: DoctorModel) {
            CoroutineScope(Dispatchers.IO).launch {
                DiabetesDatabase.getDatabase(context).diabetesDAO().insertDoctor(doctor)
            }
        }

        fun insertReading(context: Context, reading: BGLReading) {
            CoroutineScope(Dispatchers.IO).launch {
                DiabetesDatabase.getDatabase(context).diabetesDAO().insertReading(reading)
            }
        }

        fun getAllPatients(context: Context): LiveData<List<PatientModel>> {
            return DiabetesDatabase.getDatabase(context).diabetesDAO().getAllPatients()
        }

        fun getPatientWithId(context: Context, patientId: Int): LiveData<PatientModel> {
            return DiabetesDatabase.getDatabase(context).diabetesDAO().getPatientWithId(patientId)
        }

        fun getLastReadingOfPatient(
            context: Context,
            patientId: Int,
        ): LiveData<BGLReading> {
            return DiabetesDatabase.getDatabase(context).diabetesDAO()
                .getLastReadingOfPatient(patientId)
        }

        fun getAllReadingsWithPatientId(
            context: Context,
            patientId: Int,
        ): LiveData<List<BGLReading>> {
            return DiabetesDatabase.getDatabase(context).diabetesDAO()
                .getAllReadingsWithPatientId(patientId)
        }

        fun getPatientAndLastReading(
            context: Context,
            patientId: Int,
        ): LiveData<PatientLastReadingVTable> {
            return DiabetesDatabase.getDatabase(context).diabetesDAO()
                .getPatientAndLastReading(patientId)
        }

        fun getAllPatientsAndLastReading(
            context: Context,
        ): LiveData<List<PatientLastReadingVTable>> {
            return DiabetesDatabase.getDatabase(context).diabetesDAO()
                .getAllPatientsAndLastReading()
        }

        fun deletePatientWithId(context: Context, id: Int) {
            CoroutineScope(Dispatchers.IO).launch {
                DiabetesDatabase.getDatabase(context).diabetesDAO().deletePatientWithId(id)
            }
        }

        fun updatePatient(context: Context, patient: PatientModel) {
            CoroutineScope(Dispatchers.IO).launch {
                DiabetesDatabase.getDatabase(context).diabetesDAO().updatePatient(patient)
            }
        }

        fun updatePatientLastReading(
            context: Context,
            patientId: Int,
            values: String,
            timestamp: String,
        ) {
            CoroutineScope(Dispatchers.IO).launch {
                DiabetesDatabase.getDatabase(context).diabetesDAO()
                    .updatePatientLastReading(patientId, values, timestamp)
            }
        }
    }
}