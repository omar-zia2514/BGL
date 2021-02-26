package com.prototype.diabetescompanion.viewmodel

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.prototype.diabetescompanion.model.PatientModel
import com.prototype.diabetescompanion.repository.DiabetesRepository

class DiabetesViewModel : ViewModel() {

    fun insertData(context: Context, patient: PatientModel) {
        DiabetesRepository.insertData(context, patient)
    }

    fun getAllPatients(context: Context): LiveData<List<PatientModel>> {
        return DiabetesRepository.getAllPatients(context)
    }
}