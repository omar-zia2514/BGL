package com.prototype.diabetescompanion.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "DiabetesTable")
class PatientModel(
    @ColumnInfo(name = "name")
    var Name: String,

    @ColumnInfo(name = "gender")
    var Gender: String,

    @ColumnInfo(name = "age")
    var Age: Int,
) {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    var Id: Int? = null

    @ColumnInfo(name = "lastReading")
    var LastReading: Int = -1
}