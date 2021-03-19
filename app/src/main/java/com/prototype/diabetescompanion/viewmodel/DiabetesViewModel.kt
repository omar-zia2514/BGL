package com.prototype.diabetescompanion.viewmodel

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.prototype.diabetescompanion.model.BGLReading
import com.prototype.diabetescompanion.model.PatientLastReadingVTable
import com.prototype.diabetescompanion.model.PatientModel
import com.prototype.diabetescompanion.repository.DiabetesRepository

class DiabetesViewModel : ViewModel() {

    fun insertPatient(context: Context, patient: PatientModel) {
        DiabetesRepository.insertPatient(context, patient)
    }

    fun insertReading(context: Context, reading: BGLReading) {
        DiabetesRepository.insertReading(context, reading)
    }

    fun getAllPatients(context: Context): LiveData<List<PatientModel>> {
        return DiabetesRepository.getAllPatients(context)
    }

    fun getPatientWithId(context: Context, patientId: Int): LiveData<PatientModel> {
        return DiabetesRepository.getPatientWithId(context, patientId)
    }

    fun getLastReadingOfPatient(context: Context, patientId: Int): LiveData<BGLReading> {
        return DiabetesRepository.getLastReadingOfPatient(context, patientId)
    }

    fun getAllReadingsWithPatientId(context: Context, patientId: Int): LiveData<List<BGLReading>> {
        return DiabetesRepository.getAllReadingsWithPatientId(context, patientId)
    }

    fun getPatientAndLastReading(
        context: Context,
        patientId: Int,
    ): LiveData<PatientLastReadingVTable> {
        return DiabetesRepository.getPatientAndLastReading(context, patientId)
    }

    fun getAllPatientsAndLastReading(
        context: Context,
    ): LiveData<List<PatientLastReadingVTable>> {
        return DiabetesRepository.getAllPatientsAndLastReading(context)
    }

    fun deletePatientWithId(context: Context, id: Int) {
        DiabetesRepository.deletePatientWithId(context, id)
    }

    fun updatePatient(context: Context, patient: PatientModel) {
        DiabetesRepository.updatePatient(context, patient)
    }

    fun updatePatientLastReading(
        context: Context,
        patientId: Int,
        values: String,
        timestamp: String,
    ) {
        DiabetesRepository.updatePatientLastReading(context, patientId, values, timestamp)
    }
}