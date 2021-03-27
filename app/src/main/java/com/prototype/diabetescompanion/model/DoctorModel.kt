package com.prototype.diabetescompanion.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "DoctorTable")
class DoctorModel(
    @ColumnInfo(name = "name")
    var Name: String,

    @ColumnInfo(name = "number")
    var ContactNumber: String,

    @ColumnInfo(name = "experience")
    var Experience: Int,

    @ColumnInfo(name = "hospital")
    var Hospital: String,
) {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    var Id: Int? = null
}