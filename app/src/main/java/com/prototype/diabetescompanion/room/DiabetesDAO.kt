package com.prototype.diabetescompanion.room

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.prototype.diabetescompanion.model.BGLReading
import com.prototype.diabetescompanion.model.PatientModel

@Dao
interface DiabetesDAO {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertPatient(patient: PatientModel)

    @Query("SELECT * FROM PatientTable")
    fun getAllPatients(): LiveData<List<PatientModel>>

    @Query("SELECT * FROM BGLReadingTable WHERE PatientId = :patientId")
    fun getAllReadingsWithPatientId(patientId: Int): LiveData<List<BGLReading>>
}