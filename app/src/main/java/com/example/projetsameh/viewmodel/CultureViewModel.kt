package com.example.projetsameh.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.projetsameh.data.AppDatabase
import com.example.projetsameh.data.Culture
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CultureViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)
    private val cultureDao = database.cultureDao()

    val allCultures: LiveData<List<Culture>> = cultureDao.getAllCultures()

    fun insertCulture(culture: Culture) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                cultureDao.insertCulture(culture)
            }
        }
    }

    fun updateCulture(culture: Culture) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                cultureDao.updateCulture(culture)
            }
        }
    }

    fun deleteCulture(culture: Culture) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                cultureDao.deleteCulture(culture)
            }
        }
    }

    fun deleteCultureById(id: Long) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                cultureDao.deleteCultureById(id)
            }
        }
    }

    suspend fun getCultureById(id: Long): Culture? {
        return withContext(Dispatchers.IO) {
            cultureDao.getCultureById(id)
        }
    }
} 