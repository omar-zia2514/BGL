package com.prototype.diabetescompanion.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "BGLReadingTable")

class BGLReading(
    @ColumnInfo(name = "patientId")
//    @ForeignKey()
    var PatientId: Int,

    @ColumnInfo(name = "timestamp")
    var Timestamp: String,

    @ColumnInfo(name = "sensorValue")
    var SensorValue: Int,

    @ColumnInfo(name = "prickValue")
    var PrickValue: Int,
) {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    var Id: Int? = null
}