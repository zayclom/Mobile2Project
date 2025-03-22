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
    private val database: FirebaseDatabase = Firebase.database
    private val culturesRef = database.getReference("cultures")

    suspend fun saveCulture(culture: Culture) {
        culturesRef.child(culture.id.toString()).setValue(culture).await()
    }

    suspend fun deleteCulture(cultureId: Long) {
        culturesRef.child(cultureId.toString()).removeValue().await()
    }

    fun getCulturesFlow(): Flow<List<Culture>> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val cultures = snapshot.children.mapNotNull { it.getValue(Culture::class.java) }
                trySend(cultures)
            }

            override fun onCancelled(error: DatabaseError) {
                // Gérer l'erreur si nécessaire
            }
        }

        culturesRef.addValueEventListener(listener)

        awaitClose {
            culturesRef.removeEventListener(listener)
        }
    }

    suspend fun syncCultures(localCultures: List<Culture>) {
        val remoteSnapshot = culturesRef.get().await()
        val remoteCultures = remoteSnapshot.children.mapNotNull { it.getValue(Culture::class.java) }

        // Mettre à jour les cultures locales avec les données distantes
        localCultures.forEach { localCulture ->
            val remoteCulture = remoteCultures.find { it.id == localCulture.id }
            if (remoteCulture != null && remoteCulture.dateEnregistrement.after(localCulture.dateEnregistrement)) {
                saveCulture(remoteCulture)
            } else {
                saveCulture(localCulture)
            }
        }

        // Ajouter les nouvelles cultures distantes
        remoteCultures.forEach { remoteCulture ->
            if (!localCultures.any { it.id == remoteCulture.id }) {
                saveCulture(remoteCulture)
            }
        }
    }
} 