package com.prototype.diabetescompanion.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.prototype.diabetescompanion.model.BGLReading
import com.prototype.diabetescompanion.model.DoctorModel
import com.prototype.diabetescompanion.model.PatientModel

@Database(entities = [DoctorModel::class, PatientModel::class, BGLReading::class],
    version = 2,
    exportSchema = false)
abstract class DiabetesDatabase : RoomDatabase() {
    abstract fun diabetesDAO(): DiabetesDAO

    companion object {
        @Volatile
        private var INSTANCE: DiabetesDatabase? = null

        fun getDatabase(context: Context): DiabetesDatabase {
            if (INSTANCE != null) return INSTANCE!!

            synchronized(this) {
                INSTANCE =
                    Room.databaseBuilder(context, DiabetesDatabase::class.java, "DIABETES_DATABASE")
                        .fallbackToDestructiveMigration()
                        .allowMainThreadQueries()
                        .build()
                return INSTANCE!!
            }
        }
    }
}