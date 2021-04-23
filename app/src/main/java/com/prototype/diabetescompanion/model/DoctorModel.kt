package com.prototype.diabetescompanion.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey

@Entity(tableName = "DoctorTable")
class DoctorModel(
    @ColumnInfo(name = "name")
    var Name: String,

    @ColumnInfo(name = "number")
    var ContactNumber: String,

    @ColumnInfo(name = "designation")
    var Designation: String,

    @ColumnInfo(name = "hospital")
    var Hospital: String,

    @ColumnInfo(name = "onlineId")
    var OnlineId: Int = -1,
) {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    var Id: Int? = null

    @Ignore
    var patients: ArrayList<PatientModel>? = null
}