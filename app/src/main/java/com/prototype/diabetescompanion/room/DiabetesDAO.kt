package com.prototype.diabetescompanion.room

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.prototype.diabetescompanion.model.BGLReading
import com.prototype.diabetescompanion.model.PatientLastReadingVTable
import com.prototype.diabetescompanion.model.PatientModel

@Dao
interface DiabetesDAO {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertPatient(patient: PatientModel)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertReading(reading: BGLReading)

    @Query("SELECT * FROM PatientTable")
    fun getAllPatients(): LiveData<List<PatientModel>>

    @Query("SELECT * FROM PatientTable WHERE Id = :patientId")
    fun getPatientWithId(patientId: Int): LiveData<PatientModel>

    //        @Query("SELECT * FROM BGLReadingTable WHERE id=(SELECT max(id) FROM BGLReadingTable)")
    @Query("SELECT * FROM BGLReadingTable WHERE patientId = :patientId ORDER BY id LIMIT 1")
    fun getLastReadingOfPatient(patientId: Int): LiveData<BGLReading>

    @Query("SELECT * FROM BGLReadingTable WHERE PatientId = :patientId ORDER BY id DESC")
    fun getAllReadingsWithPatientId(patientId: Int): LiveData<List<BGLReading>>

    @Query("SELECT PatientTable.name, PatientTable.gender, PatientTable.age, BGLReadingTable.sensorValue, BGLReadingTable.prickValue, BGLReadingTable.timestamp FROM PatientTable LEFT JOIN BGLReadingTable ON PatientTable.id = BGLReadingTable.patientId WHERE PatientTable.id = :patientId ORDER BY BGLReadingTable.id DESC")
    fun getPatientAndLastReading(patientId: Int): LiveData<PatientLastReadingVTable>

    @Query("SELECT PatientTable.id, PatientTable.name, PatientTable.gender, PatientTable.age, BGLReadingTable.sensorValue, BGLReadingTable.prickValue, BGLReadingTable.timestamp FROM PatientTable JOIN BGLReadingTable ON PatientTable.id = BGLReadingTable.patientId")
    fun getAllPatientsAndLastReading(): LiveData<List<PatientLastReadingVTable>>

    /*SELECT PatientTable.id, PatientTable.name, PatientTable.gender, PatientTable.age, BGLReadingTable.sensorValue,
    BGLReadingTable.prickValue, BGLReadingTable.timestamp FROM PatientTable LEFT OUTER JOIN BGLReadingTable ON
    PatientTable.id = BGLReadingTable.patientId ORDER BY BGLReadingTable.id DESC LIMIT 1*/
}