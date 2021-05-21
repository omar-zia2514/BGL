package com.prototype.diabetescompanion

import com.prototype.diabetescompanion.model.DoctorModel
import com.prototype.diabetescompanion.model.PatientModel
import org.json.JSONArray
import org.json.JSONObject

public class JsonManager {
    companion object {
        fun getDoctorSyncJson(doctorData: DoctorModel): JSONObject {
            try {
                var patientArray = JSONArray()

                for (patient in doctorData.patients!!) {
                    val patientJson = JSONObject()
                    val readingsArray = JSONArray()
                    if (patient.OnlineId == -1) {
                        patientJson.put("PatientId", 0)
                        patientJson.put("OperationCode", 0)
                    } else {
                        patientJson.put("PatientId", patient.OnlineId)
                        patientJson.put("OperationCode", 1)
                    }
                    patientJson.put("Name", patient.Name)
                    patientJson.put("Age", patient.Age)
                    patientJson.put("Address", "")
                    patientJson.put("Gender", patient.Gender)
                    patientJson.put("ContactNo", patient.ContactNumber)
                    patientJson.put("Readings", readingsArray)

                    for (reading in patient.readings) {
                        val readingJson = JSONObject()
                        readingJson.put("ReadingdateTime",
                            reading.Timestamp)
                        readingJson.put("SensorValue", reading.SensorValue)
                        readingJson.put("PrickValue", reading.PrickValue)
                        readingsArray.put(readingJson)
                    }
                    patientArray.put(patientJson)
                }

                val doctorJson = JSONObject()
                if (doctorData.OnlineId == -1)
                    doctorJson.put("DoctorId", 0)
                else
                    doctorJson.put("DoctorId", doctorData.OnlineId)
                doctorJson.put("Name", doctorData.Name)
                doctorJson.put("Age", 0)
                doctorJson.put("Address", doctorData.Hospital)
                doctorJson.put("Specification", doctorData.Designation)
                doctorJson.put("ContactNo", doctorData.ContactNumber)
                doctorJson.put("Experience", 0)
                doctorJson.put("SyncType", 0)
                doctorJson.put("Patients", patientArray)

                Util.makeLog("Json Created: $doctorJson")
                return doctorJson
            } catch (e: Exception) {
                Util.makeLog("Exception in JSON builder: ${e.message}")
                return JSONObject()
            }
        }

        fun getPatientSyncJson(patientData: PatientModel): JSONObject {
            try {
                var patientArray = JSONArray()

                val patientJson = JSONObject()
                val readingsArray = JSONArray()
                if (patientData.OnlineId == -1) {
                    patientJson.put("PatientId", 0)
                    patientJson.put("OperationCode", 0)
                } else {
                    patientJson.put("PatientId", patientData.OnlineId)
                    patientJson.put("OperationCode", 1)
                }
                patientJson.put("Name", patientData.Name)
                patientJson.put("Age", patientData.Age)
                patientJson.put("Address", "")
                patientJson.put("Gender", patientData.Gender)
                patientJson.put("ContactNo", patientData.ContactNumber)
                patientJson.put("Readings", readingsArray)

                for (reading in patientData.readings) {
                    val readingJson = JSONObject()
                    readingJson.put("ReadingdateTime",
                        reading.Timestamp)
                    readingJson.put("SensorValue", reading.SensorValue)
                    readingJson.put("PrickValue", reading.PrickValue)
                    readingsArray.put(readingJson)
                }
                patientArray.put(patientJson)

                val doctorJson = JSONObject()
                doctorJson.put("DoctorId", 0)
                doctorJson.put("Name", "")
                doctorJson.put("Age", 0)
                doctorJson.put("Address", "")
                doctorJson.put("Specification", "")
                doctorJson.put("ContactNo", "")
                doctorJson.put("Experience", 0)
                doctorJson.put("SyncType", 1)
                doctorJson.put("Patients", patientArray)

                Util.makeLog("Json Created: $doctorJson")
                return doctorJson
            } catch (e: Exception) {
                Util.makeLog("Exception in JSON builder: ${e.message}")
                return JSONObject()
            }
        }
    }
}