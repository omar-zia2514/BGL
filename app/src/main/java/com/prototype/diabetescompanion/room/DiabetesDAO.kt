package com.prototype.diabetescompanion.room

import androidx.lifecycle.LiveData
import androidx.room.*
import com.prototype.diabetescompanion.model.BGLReading
import com.prototype.diabetescompanion.model.DoctorModel
import com.prototype.diabetescompanion.model.PatientLastReadingVTable
import com.prototype.diabetescompanion.model.PatientModel

@Dao
interface DiabetesDAO {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertPatient(patient: PatientModel)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertDoctor(doctor: DoctorModel)

    @Delete
    suspend fun deletePatient(patient: PatientModel)

    @Update
    suspend fun updatePatient(patient: PatientModel)

    @Update
    suspend fun updateReading(reading: BGLReading)

    @Query("UPDATE BGLReadingTable set syncStatus = 1 WHERE syncStatus = 0 AND patientId != :patientId")
    suspend fun updateSyncStatusDoctor(patientId: Int)

    @Query("UPDATE BGLReadingTable set syncStatus = 1 WHERE syncStatus = 0")
    suspend fun updateSyncStatusAll()

    @Query("UPDATE BGLReadingTable set syncStatus = 1 WHERE syncStatus = 0 AND patientId = :patientId")
    suspend fun updateSyncStatusPatient(patientId: Int)

    @Query("UPDATE DoctorTable set onlineId = :id")
    suspend fun updateDoctorOnlineId(id: Int)

    @Query("UPDATE PatientTable set onlineId = :onlineId WHERE id = :id")
    suspend fun updatePatientOnlineId(id: Int, onlineId: Int)

    @Query("UPDATE PatientTable set lastReading = :values, lastReadingTimestamp = :timestamp WHERE id = :patientId")
    suspend fun updatePatientLastReading(patientId: Int, values: String, timestamp: String)

    @Query("DELETE FROM PatientTable WHERE id = :id")
    suspend fun deletePatientWithId(id: Int)

    @Query("DELETE FROM BGLReadingTable WHERE id = :id")
    suspend fun deleteReadingWithId(id: Int)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertReading(reading: BGLReading)

    @Query("SELECT * FROM PatientTable WHERE doctorId = 1")
    fun getAllPatientsLiveData(): LiveData<List<PatientModel>>

    @Query("SELECT * FROM PatientTable WHERE doctorId = 1")
    fun getAllPatients(): List<PatientModel>

    @Query("SELECT * FROM DoctorTable")
    fun getDoctorData(): List<DoctorModel>

    @Query("SELECT * FROM PatientTable WHERE id = :patientId")
    fun getPatientLiveDataWithId(patientId: Int): LiveData<PatientModel>

    @Query("SELECT * FROM PatientTable WHERE id = :patientId")
    fun getPatientWithId(patientId: Int): PatientModel

    //        @Query("SELECT * FROM BGLReadingTable WHERE id=(SELECT max(id) FROM BGLReadingTable)")
    @Query("SELECT * FROM BGLReadingTable WHERE patientId = :patientId ORDER BY id LIMIT 1")
    fun getLastReadingOfPatient(patientId: Int): LiveData<BGLReading>

    @Query("SELECT * FROM BGLReadingTable WHERE patientId = :patientId ORDER BY id DESC")
    fun getAllReadingsLiveDataWithPatientId(patientId: Int): LiveData<List<BGLReading>>

    @Query("SELECT * FROM BGLReadingTable WHERE patientId = :patientId AND syncStatus = 0 ORDER BY id DESC")
    fun getAllReadingsWithPatientId(patientId: Int): List<BGLReading>

    @Query("SELECT * FROM BGLReadingTable WHERE patientId = :patientId AND syncStatus = 0 ORDER BY id DESC")
    fun getAllUnSyncedReadingsWithPatientId(patientId: Int?): List<BGLReading>

    @Query("SELECT PatientTable.id FROM PatientTable WHERE PatientTable.doctorId = 0")
    fun getOwnerPatientIdLiveData(): LiveData<Int>

    @Query("SELECT PatientTable.id FROM PatientTable WHERE PatientTable.doctorId = 0")
    fun getOwnerPatientId(): Int

    @Query("SELECT PatientTable.name, PatientTable.gender, PatientTable.age, BGLReadingTable.sensorValue, BGLReadingTable.prickValue, BGLReadingTable.timestamp FROM PatientTable LEFT JOIN BGLReadingTable ON PatientTable.id = BGLReadingTable.patientId WHERE PatientTable.id = :patientId ORDER BY BGLReadingTable.id DESC")
    fun getPatientAndLastReading(patientId: Int): LiveData<PatientLastReadingVTable>

    @Query("SELECT PatientTable.id, PatientTable.name, PatientTable.gender, PatientTable.age, BGLReadingTable.sensorValue, BGLReadingTable.prickValue, BGLReadingTable.timestamp FROM PatientTable JOIN BGLReadingTable ON PatientTable.id = BGLReadingTable.patientId")
    fun getAllPatientsAndLastReading(): LiveData<List<PatientLastReadingVTable>>

    /*SELECT PatientTable.id, PatientTable.name, PatientTable.gender, PatientTable.age, BGLReadingTable.sensorValue,
    BGLReadingTable.prickValue, BGLReadingTable.timestamp FROM PatientTable LEFT OUTER JOIN BGLReadingTable ON
    PatientTable.id = BGLReadingTable.patientId ORDER BY BGLReadingTable.id DESC LIMIT 1*/
}