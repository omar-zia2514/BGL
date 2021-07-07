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

        fun getAllPatientsLiveData(context: Context): LiveData<List<PatientModel>> {
            return DiabetesDatabase.getDatabase(context).diabetesDAO().getAllPatientsLiveData()
        }

        fun getAllPatients(context: Context): List<PatientModel> {
            return DiabetesDatabase.getDatabase(context).diabetesDAO().getAllPatients()
        }

        fun getPatienLiveDatatWithId(context: Context, patientId: Int): LiveData<PatientModel> {
            return DiabetesDatabase.getDatabase(context).diabetesDAO()
                .getPatientLiveDataWithId(patientId)
        }

        fun getPatientWithId(context: Context, patientId: Int): PatientModel {
            return DiabetesDatabase.getDatabase(context).diabetesDAO()
                .getPatientWithId(patientId)
        }

        fun getLastReadingOfPatient(
            context: Context,
            patientId: Int,
        ): LiveData<BGLReading> {
            return DiabetesDatabase.getDatabase(context).diabetesDAO()
                .getLastReadingOfPatient(patientId)
        }

        fun getAllReadingsLiveDataWithPatientId(
            context: Context,
            patientId: Int,
        ): LiveData<List<BGLReading>> {
            return DiabetesDatabase.getDatabase(context).diabetesDAO()
                .getAllReadingsLiveDataWithPatientId(patientId)
        }

        fun getAllReadingsWithPatientId(
            context: Context,
            patientId: Int,
        ): List<BGLReading> {
            return DiabetesDatabase.getDatabase(context).diabetesDAO()
                .getAllReadingsWithPatientId(patientId)
        }

        fun getAllUnSyncedReadingsWithPatientId(
            context: Context,
            patientId: Int?,
        ): List<BGLReading> {
            return DiabetesDatabase.getDatabase(context).diabetesDAO()
                .getAllUnSyncedReadingsWithPatientId(patientId)
        }

        fun getDoctorData(
            context: Context,
        ): List<DoctorModel> {
            return DiabetesDatabase.getDatabase(context).diabetesDAO()
                .getDoctorData()
        }

        fun getOwnerPatientIdLiveData(
            context: Context,
        ): LiveData<Int> {
            return DiabetesDatabase.getDatabase(context).diabetesDAO()
                .getOwnerPatientIdLiveData()
        }

        fun getOwnerPatientId(
            context: Context,
        ): Int {
            return DiabetesDatabase.getDatabase(context).diabetesDAO()
                .getOwnerPatientId()
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

        fun deleteReadingWithId(context: Context, id: Int) {
            CoroutineScope(Dispatchers.IO).launch {
                DiabetesDatabase.getDatabase(context).diabetesDAO().deleteReadingWithId(id)
            }
        }

        fun updatePatient(context: Context, reading: BGLReading) {
            CoroutineScope(Dispatchers.IO).launch {
                DiabetesDatabase.getDatabase(context).diabetesDAO().updateReading(reading)
            }
        }

        fun updateSyncStatusDoctor(context: Context, patientId: Int) {
            CoroutineScope(Dispatchers.IO).launch {
                DiabetesDatabase.getDatabase(context).diabetesDAO()
                    .updateSyncStatusDoctor(patientId)
            }
        }

        fun updateSyncStatusAll(context: Context) {
            CoroutineScope(Dispatchers.IO).launch {
                DiabetesDatabase.getDatabase(context).diabetesDAO().updateSyncStatusAll()
            }
        }

        fun updateSyncStatusPatient(context: Context, patientId: Int) {
            CoroutineScope(Dispatchers.IO).launch {
                DiabetesDatabase.getDatabase(context).diabetesDAO()
                    .updateSyncStatusPatient(patientId)
            }
        }

        fun updateDoctorOnlineId(context: Context, id: Int) {
            CoroutineScope(Dispatchers.IO).launch {
                DiabetesDatabase.getDatabase(context).diabetesDAO().updateDoctorOnlineId(id)
            }
        }

        fun updatePatientOnlineId(context: Context, id: Int, onlineId: Int) {
            CoroutineScope(Dispatchers.IO).launch {
                DiabetesDatabase.getDatabase(context).diabetesDAO()
                    .updatePatientOnlineId(id, onlineId)
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