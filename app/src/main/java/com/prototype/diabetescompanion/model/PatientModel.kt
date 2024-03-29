package com.prototype.diabetescompanion.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey

@Entity(tableName = "PatientTable")
class PatientModel(
    @ColumnInfo(name = "name")
    var Name: String,

    @ColumnInfo(name = "number")
    var ContactNumber: String = "",

    @ColumnInfo(name = "gender")
    var Gender: String,

    @ColumnInfo(name = "dob")
    var DOB: String,

    @ColumnInfo(name = "age")
    var Age: Int,

    @ColumnInfo(name = "lastReading")
    var LastReading: String? = null,

    @ColumnInfo(name = "lastReadingTimestamp")
    var LastReadingTimestamp: String? = null,

    @ColumnInfo(name = "doctorId")
    var DoctorId: Int = 1,

    @ColumnInfo(name = "onlineId")
    var OnlineId: Int = -1,
) {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    var Id: Int? = null

    @Ignore
    lateinit var readings: ArrayList<BGLReading>
}