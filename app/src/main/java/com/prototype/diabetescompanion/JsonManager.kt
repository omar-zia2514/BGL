package com.prototype.diabetescompanion

import org.json.JSONArray
import org.json.JSONObject

public class JsonManager {
    companion object {
        fun getJson(): JSONObject {
            try {
                var readingsArray = JSONArray()

                var reading1 = JSONObject()
                reading1.put("ReadingdateTime", "2018-04-19 15:27:16.050")
                reading1.put("SensorValue", 125)
                reading1.put("PrickValue", 124)
                reading1.put("ReadingdateTime", "2018-04-19 15:27:16.050")

                var reading2 = JSONObject()
                reading1.put("ReadingdateTime", "2018-04-19 15:27:16.050")
                reading1.put("SensorValue", 130)
                reading1.put("PrickValue", 129)
                reading1.put("ReadingdateTime", "2018-04-19 15:27:16.050")

                readingsArray.put(reading1)
                readingsArray.put(reading2)

                var patient1 = JSONObject()
                patient1.put("Id", 1)
                patient1.put("PatientId", 7)
                patient1.put("Name", "Ben")
                patient1.put("Age", 36)
                patient1.put("Address", "Wisconsin")
                patient1.put("Gender", "Male")
                patient1.put("Address", "Wisconsin")
                patient1.put("ContactNo", "03331234567")
                patient1.put("OperationCode", 1)
                patient1.put("Readings", readingsArray)

                var patientArray = JSONArray()
                patientArray.put(patient1)

                var doctor = JSONObject()
                doctor.put("DoctorId", 11)
                doctor.put("Name", "Dr Paul")
                doctor.put("Age", 49)
                doctor.put("Address", "Miami")
                doctor.put("Specification", "RM")
                doctor.put("Experience", 13)
                doctor.put("SyncType", 0)
                doctor.put("Patients", patientArray)

                Util.makeLog("Json Created: $doctor")
                return doctor
            } catch (e: Exception) {
                Util.makeLog("Exception in JSON builder: ${e.message}")
                return JSONObject()
            }
        }
    }
}