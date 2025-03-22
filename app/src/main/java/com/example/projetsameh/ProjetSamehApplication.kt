package com.example.projetsameh

import android.app.Application
import android.util.Log
import com.google.firebase.FirebaseApp

class ProjetSamehApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        Log.e("ProjetSamehApplication", "Application onCreate started")
        try {
            FirebaseApp.initializeApp(this)
            Log.e("ProjetSamehApplication", "Firebase initialized successfully")
        } catch (e: Exception) {
            Log.e("ProjetSamehApplication", "Error initializing Firebase", e)
        }
    }
} 