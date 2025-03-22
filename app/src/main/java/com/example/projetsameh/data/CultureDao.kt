package com.example.projetsameh.data

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface CultureDao {
    @Query("SELECT * FROM cultures ORDER BY dateEnregistrement DESC")
    fun getAllCultures(): LiveData<List<Culture>>

    @Query("SELECT * FROM cultures WHERE id = :id")
    suspend fun getCultureById(id: Long): Culture?

    @Insert
    suspend fun insertCulture(culture: Culture): Long

    @Update
    suspend fun updateCulture(culture: Culture)

    @Delete
    suspend fun deleteCulture(culture: Culture)

    @Query("DELETE FROM cultures WHERE id = :id")
    suspend fun deleteCultureById(id: Long)
} 