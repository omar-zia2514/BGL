package com.prototype.diabetescompanion.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "PatientTable")
class PatientModel(
    @ColumnInfo(name = "name")
    var Name: String,

    @ColumnInfo(name = "gender")
    var Gender: String,

    @ColumnInfo(name = "age")
    var Age: Int,

    @ColumnInfo(name = "lastReading")
    var LastReading: String? = null,

    @ColumnInfo(name = "lastReadingTimestamp")
    var LastReadingTimestamp: String? = null,
) {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    var Id: Int? = null
}