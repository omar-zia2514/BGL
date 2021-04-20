package com.prototype.diabetescompanion.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
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

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("CREATE TABLE `DoctorTable` (`id` INTEGER PRIMARY KEY AUTOINCREMENT, `name` TEXT NOT NULL, `number` TEXT NOT NULL, `experience` INTEGER NOT NULL, `hospital` TEXT NOT NULL)")
            }
        }

        fun getDatabase(context: Context): DiabetesDatabase {
            if (INSTANCE != null) return INSTANCE!!

            synchronized(this) {
                INSTANCE =
                    Room.databaseBuilder(context, DiabetesDatabase::class.java, "DIABETES_DATABASE")
                        .fallbackToDestructiveMigration()
                        .addMigrations(MIGRATION_1_2)
                        .allowMainThreadQueries()
                        .build()
                return INSTANCE!!
            }
        }
    }
}