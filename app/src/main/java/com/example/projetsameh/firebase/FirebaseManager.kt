package com.example.projetsameh.firebase

import com.example.projetsameh.data.Culture
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirebaseManager {
    private val database = FirebaseDatabase.getInstance()
    private val culturesRef = database.getReference("cultures")

    fun addCulture(culture: Culture, onSuccess: () -> Unit, onError: (Exception) -> Unit) {
        val newCultureRef = culturesRef.push()
        culture.id = newCultureRef.key ?: ""
        newCultureRef.setValue(culture)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onError(it) }
    }

    fun updateCulture(culture: Culture, onSuccess: () -> Unit, onError: (Exception) -> Unit) {
        culturesRef.child(culture.id).setValue(culture)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onError(it) }
    }

    fun deleteCulture(cultureId: String, onSuccess: () -> Unit, onError: (Exception) -> Unit) {
        culturesRef.child(cultureId).removeValue()
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onError(it) }
    }

    fun getCulture(cultureId: String, onSuccess: (Culture?) -> Unit, onError: (Exception) -> Unit) {
        culturesRef.child(cultureId).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val culture = snapshot.getValue(Culture::class.java)
                culture?.id = snapshot.key ?: ""
                onSuccess(culture)
            }

            override fun onCancelled(error: DatabaseError) {
                onError(error.toException())
            }
        })
    }

    fun getAllCultures(onSuccess: (List<Culture>) -> Unit, onError: (Exception) -> Unit) {
        culturesRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val cultures = mutableListOf<Culture>()
                for (cultureSnapshot in snapshot.children) {
                    cultureSnapshot.getValue(Culture::class.java)?.let { culture ->
                        culture.id = cultureSnapshot.key ?: ""
                        cultures.add(culture)
                    }
                }
                onSuccess(cultures)
            }

            override fun onCancelled(error: DatabaseError) {
                onError(error.toException())
            }
        })
    }

    suspend fun syncCultures(localCultures: List<Culture>) {
        val remoteSnapshot = culturesRef.get().await()
        val remoteCultures = remoteSnapshot.children.mapNotNull { it.getValue(Culture::class.java) }

        // Update local cultures with remote data
        localCultures.forEach { localCulture ->
            val remoteCulture = remoteCultures.find { it.id == localCulture.id }
            if (remoteCulture != null) {
                updateCulture(remoteCulture, {}, {})
            } else {
                updateCulture(localCulture, {}, {})
            }
        }

        // Add new remote cultures
        remoteCultures.forEach { remoteCulture ->
            if (!localCultures.any { it.id == remoteCulture.id }) {
                updateCulture(remoteCulture, {}, {})
            }
        }
    }
} 