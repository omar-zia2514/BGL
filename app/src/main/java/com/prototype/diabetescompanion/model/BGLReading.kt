package com.prototype.diabetescompanion.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.CASCADE
import androidx.room.PrimaryKey

@Entity(tableName = "BGLReadingTable", foreignKeys = [
    ForeignKey(entity = PatientModel::class,
        parentColumns = ["id"],
        childColumns = ["patientId"],
        onDelete = CASCADE,
        onUpdate = CASCADE)])

class BGLReading(
    @ColumnInfo(name = "patientId")
    var PatientId: Int,

    @ColumnInfo(name = "timestamp")
    var Timestamp: String,

    @ColumnInfo(name = "prickValue")
    var PrickValue: Int,

    @ColumnInfo(name = "sensorValue")
    var SensorValue: Int,
) {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    var Id: Int? = null
}