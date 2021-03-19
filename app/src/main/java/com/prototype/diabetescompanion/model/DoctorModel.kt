package com.prototype.diabetescompanion.model

import androidx.room.ColumnInfo
import androidx.room.Entity

@Entity(tableName = "DoctorTable")
class DoctorModel(
    @ColumnInfo(name = "name")
    var Name: String,

    ) {
}