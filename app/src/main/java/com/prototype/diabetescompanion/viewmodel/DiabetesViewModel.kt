package com.prototype.diabetescompanion.viewmodel

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.prototype.diabetescompanion.Util
import com.prototype.diabetescompanion.model.BGLReading
import com.prototype.diabetescompanion.model.DoctorModel
import com.prototype.diabetescompanion.model.PatientLastReadingVTable
import com.prototype.diabetescompanion.model.PatientModel
import com.prototype.diabetescompanion.repository.DiabetesRepository
import org.json.JSONObject

class DiabetesViewModel : ViewModel() {

    fun insertPatient(context: Context, patient: PatientModel) {
        DiabetesRepository.insertPatient(context, patient)
    }

    fun insertDoctor(context: Context, doctor: DoctorModel) {
        DiabetesRepository.insertDoctor(context, doctor)
    }

    fun insertReading(context: Context, reading: BGLReading) {
        DiabetesRepository.insertReading(context, reading)
    }

    fun getAllPatients(context: Context): LiveData<List<PatientModel>> {
        return DiabetesRepository.getAllPatientsLiveData(context)
    }

    fun getPatientWithId(context: Context, patientId: Int): LiveData<PatientModel> {
        return DiabetesRepository.getPatienLiveDatatWithId(context, patientId)
    }

    fun getLastReadingOfPatient(context: Context, patientId: Int): LiveData<BGLReading> {
        return DiabetesRepository.getLastReadingOfPatient(context, patientId)
    }

    fun getAllReadingsLiveDataWithPatientId(
        context: Context,
        patientId: Int,
    ): LiveData<List<BGLReading>> {
        return DiabetesRepository.getAllReadingsLiveDataWithPatientId(context, patientId)
    }

    fun getPatientData(context: Context, patientId: Int): PatientModel {
        val patientData: PatientModel = DiabetesRepository.getPatientWithId(context, patientId)
        patientData.readings = DiabetesRepository.getAllReadingsWithPatientId(context,
            patientId) as ArrayList<BGLReading>
        return patientData
    }

    fun getDoctorData(context: Context): DoctorModel {
        val doctor = DiabetesRepository.getDoctorData(context)[0]
        val patients = DiabetesRepository.getAllPatients(context)
        doctor.patients = ArrayList()
        for (patient in patients) {
            patient.readings = DiabetesRepository.getAllUnSyncedReadingsWithPatientId(context,
                patient.Id) as ArrayList<BGLReading>
            if (patient.readings.size > 0)
                doctor.patients?.add(patient)
        }
        return doctor
    }

    fun getOwnerPatientIdLiveData(context: Context): LiveData<Int> {
        return DiabetesRepository.getOwnerPatientIdLiveData(context)
    }

    fun getOwnerPatientId(context: Context): Int {
        return DiabetesRepository.getOwnerPatientId(context)
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

    fun deleteReadingWithId(context: Context, id: Int) {
        DiabetesRepository.deleteReadingWithId(context, id)
    }

    fun updateReading(context: Context, reading: BGLReading) {
        DiabetesRepository.updatePatient(context, reading)
    }

    fun updateSyncStatusDoctor(context: Context, patientId: Int) {
        Util.makeLog("updateSyncStatusDoctor")
        if (patientId == -1)
            DiabetesRepository.updateSyncStatusAll(context)
        else
            DiabetesRepository.updateSyncStatusDoctor(context, patientId)
    }

    fun updateSyncStatusPatient(context: Context, patientId: Int) {
        DiabetesRepository.updateSyncStatusPatient(context, patientId)
    }

    fun updateOnlineIdsDoctorSync(
        context: Context,
        doctorData: DoctorModel,
        onlineIds: JSONObject,
    ) {
        if (doctorData.OnlineId == -1)
            DiabetesRepository.updateDoctorOnlineId(context, onlineIds.getInt("DoctorID"))

        var count = 0
        for (patient in doctorData.patients!!) {
            if (patient.OnlineId == -1) {
                val patientJsonArray = onlineIds.getJSONArray("Patients")
                DiabetesRepository.updatePatientOnlineId(context,
                    patient.Id!!,
                    (patientJsonArray.get(count) as JSONObject).get("PatientId") as Int)
                count++
            }
        }
    }

    fun updateOnlineIdsPatientSync(
        context: Context,
        patientData: PatientModel,
        onlineIds: JSONObject,
    ) {
        if (patientData.OnlineId == -1) {
            val patientJsonArray = onlineIds.getJSONArray("Patients")
            DiabetesRepository.updatePatientOnlineId(context,
                patientData.Id!!,
                (patientJsonArray.get(0) as JSONObject).get("PatientId") as Int)
        }
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