package com.example.projetsameh.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.projetsameh.data.Culture
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.GenericTypeIndicator

class CultureViewModel(application: Application) : AndroidViewModel(application) {
    private val _allCultures = MutableLiveData<List<Culture>>()
    val allCultures: LiveData<List<Culture>> = _allCultures

    private val database: FirebaseDatabase = try {
        FirebaseDatabase.getInstance("https://mobile2bd-default-rtdb.firebaseio.com")
    } catch (e: Exception) {
        Log.e("CultureViewModel", "Error initializing Firebase Database", e)
        throw e
    }

    init {
        Log.e("CultureViewModel", "Initializing ViewModel")
        try {
            loadCultures()
        } catch (e: Exception) {
            Log.e("CultureViewModel", "Error in init", e)
        }
    }

    private fun loadCultures() {
        Log.e("CultureViewModel", "Starting to load cultures from Firebase")
        try {
            database.reference.child("cultures").addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    try {
                        val cultures = mutableListOf<Culture>()
                        Log.e("CultureViewModel", "Snapshot exists: ${snapshot.exists()}")
                        Log.e("CultureViewModel", "Snapshot has children: ${snapshot.hasChildren()}")
                        Log.e("CultureViewModel", "Snapshot value type: ${snapshot.value?.javaClass?.simpleName}")
                        
                        // Get the value as a map of culture objects
                        val value = snapshot.value
                        Log.e("CultureViewModel", "Raw value: $value")
                        
                        if (value is Map<*, *>) {
                            Log.e("CultureViewModel", "Value is a Map with ${value.size} entries")
                            
                            // First, identify all valid culture entries (those that are maps)
                            val validCultures = value.filter { (_, data) -> data is Map<*, *> }
                            val invalidKeys = value.keys.filter { key -> !validCultures.containsKey(key) }
                            
                            // Clean up all invalid entries at once
                            if (invalidKeys.isNotEmpty()) {
                                val updates = invalidKeys.associateWith { null }.mapKeys { it.key.toString() }
                                database.reference.child("cultures").updateChildren(updates)
                                    .addOnSuccessListener {
                                        Log.e("CultureViewModel", "Cleaned up ${invalidKeys.size} invalid entries")
                                    }
                                    .addOnFailureListener { e ->
                                        Log.e("CultureViewModel", "Failed to clean up invalid entries: ${e.message}")
                                    }
                            }
                            
                            // Now process the valid culture entries
                            for ((key, data) in validCultures) {
                                try {
                                    val cultureData = data as Map<*, *>
                                    val cultureId = key.toString()
                                    
                                    val culture = Culture(
                                        id = cultureId,
                                        nom = cultureData["nom"]?.toString() ?: "",
                                        type = cultureData["type"]?.toString() ?: "",
                                        datePlantation = cultureData["datePlantation"]?.toString() ?: "",
                                        etat = cultureData["etat"]?.toString() ?: "",
                                        besoins = cultureData["besoins"]?.toString() ?: "",
                                        latitude = (cultureData["latitude"] as? Number)?.toDouble() ?: 0.0,
                                        longitude = (cultureData["longitude"] as? Number)?.toDouble() ?: 0.0,
                                        adresse = cultureData["adresse"]?.toString() ?: "",
                                        temperatureMin = (cultureData["temperatureMin"] as? Number)?.toDouble() ?: 0.0,
                                        temperatureMax = (cultureData["temperatureMax"] as? Number)?.toDouble() ?: 0.0,
                                        humiditeMin = (cultureData["humiditeMin"] as? Number)?.toDouble() ?: 0.0,
                                        humiditeMax = (cultureData["humiditeMax"] as? Number)?.toDouble() ?: 0.0,
                                        commentaires = cultureData["commentaires"]?.toString() ?: ""
                                    )
                                    
                                    if (culture.nom.isNotEmpty()) {
                                        cultures.add(culture)
                                        Log.e("CultureViewModel", "Successfully loaded culture: ${culture.nom}")
                                    } else {
                                        // Clean up invalid culture entry
                                        database.reference.child("cultures").child(cultureId).removeValue()
                                            .addOnSuccessListener {
                                                Log.e("CultureViewModel", "Cleaned up invalid culture entry: $cultureId")
                                            }
                                            .addOnFailureListener { e ->
                                                Log.e("CultureViewModel", "Failed to clean up invalid culture entry: ${e.message}")
                                            }
                                    }
                                } catch (e: Exception) {
                                    Log.e("CultureViewModel", "Error creating Culture object for ID $key: ${e.message}")
                                    Log.e("CultureViewModel", "Stack trace: ${e.stackTraceToString()}")
                                }
                            }
                        } else {
                            Log.e("CultureViewModel", "Value is not a Map, it is: ${value?.javaClass?.simpleName}")
                        }
                        
                        _allCultures.value = cultures
                        Log.e("CultureViewModel", "Successfully loaded ${cultures.size} cultures")
                    } catch (e: Exception) {
                        Log.e("CultureViewModel", "Error parsing cultures from Firebase", e)
                        Log.e("CultureViewModel", "Stack trace: ${e.stackTraceToString()}")
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("CultureViewModel", "Error loading cultures from Firebase: ${error.message}")
                    Log.e("CultureViewModel", "Error details: ${error.details}")
                }
            })
        } catch (e: Exception) {
            Log.e("CultureViewModel", "Error setting up Firebase listener", e)
            Log.e("CultureViewModel", "Stack trace: ${e.stackTraceToString()}")
        }
    }

    fun insertCulture(culture: Culture) {
        Log.e("CultureViewModel", "Attempting to insert culture: ${culture.nom}")
        try {
            // Generate a unique ID if one isn't provided
            val cultureId = if (culture.id.isBlank()) {
                database.reference.child("cultures").push().key ?: throw Exception("Failed to generate unique ID")
            } else {
                culture.id
            }
            
            val cultureRef = database.reference.child("cultures").child(cultureId)
            
            // Create a map of the culture data
            val cultureMap = mapOf(
                "id" to cultureId,
                "nom" to culture.nom,
                "type" to culture.type,
                "datePlantation" to culture.datePlantation,
                "etat" to culture.etat,
                "besoins" to culture.besoins,
                "latitude" to culture.latitude,
                "longitude" to culture.longitude,
                "adresse" to culture.adresse,
                "temperatureMin" to culture.temperatureMin,
                "temperatureMax" to culture.temperatureMax,
                "humiditeMin" to culture.humiditeMin,
                "humiditeMax" to culture.humiditeMax,
                "commentaires" to culture.commentaires
            )
            
            // Save the entire culture object at once
            cultureRef.setValue(cultureMap)
                .addOnSuccessListener {
                    Log.e("CultureViewModel", "Successfully inserted culture: ${culture.nom} with ID: $cultureId")
                }
                .addOnFailureListener { e ->
                    Log.e("CultureViewModel", "Failed to insert culture: ${e.message}")
                    Log.e("CultureViewModel", "Error details: ${e.stackTraceToString()}")
                }
        } catch (e: Exception) {
            Log.e("CultureViewModel", "Error inserting culture", e)
        }
    }

    fun updateCulture(culture: Culture) {
        Log.e("CultureViewModel", "Attempting to update culture: ${culture.nom}")
        try {
            // If the culture has no ID, treat it as a new culture
            if (culture.id.isBlank()) {
                insertCulture(culture)
                return
            }

            val cultureRef = database.reference.child("cultures").child(culture.id)
            
            // Create a map of the culture data
            val cultureMap = mapOf(
                "id" to culture.id,
                "nom" to culture.nom,
                "type" to culture.type,
                "datePlantation" to culture.datePlantation,
                "etat" to culture.etat,
                "besoins" to culture.besoins,
                "latitude" to culture.latitude,
                "longitude" to culture.longitude,
                "adresse" to culture.adresse,
                "temperatureMin" to culture.temperatureMin,
                "temperatureMax" to culture.temperatureMax,
                "humiditeMin" to culture.humiditeMin,
                "humiditeMax" to culture.humiditeMax,
                "commentaires" to culture.commentaires
            )
            
            // Update the entire culture object at once
            cultureRef.setValue(cultureMap)
                .addOnSuccessListener {
                    Log.e("CultureViewModel", "Successfully updated culture: ${culture.nom}")
                }
                .addOnFailureListener { e ->
                    Log.e("CultureViewModel", "Failed to update culture: ${e.message}")
                    Log.e("CultureViewModel", "Error details: ${e.stackTraceToString()}")
                }
        } catch (e: Exception) {
            Log.e("CultureViewModel", "Error updating culture", e)
        }
    }

    fun deleteCulture(culture: Culture) {
        Log.e("CultureViewModel", "Attempting to delete culture: ${culture.nom}")
        try {
            database.reference.child("cultures").child(culture.id).removeValue()
                .addOnSuccessListener {
                    Log.e("CultureViewModel", "Successfully deleted culture: ${culture.nom}")
                }
                .addOnFailureListener { e ->
                    Log.e("CultureViewModel", "Failed to delete culture: ${e.message}")
                    Log.e("CultureViewModel", "Error details: ${e.stackTraceToString()}")
                }
        } catch (e: Exception) {
            Log.e("CultureViewModel", "Error deleting culture", e)
        }
    }

    fun getCultureById(id: String): Culture? {
        return _allCultures.value?.find { it.id == id }
    }
} 