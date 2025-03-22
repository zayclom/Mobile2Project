package com.example.projetsameh

import android.Manifest
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.projetsameh.adapter.CultureAdapter
import com.example.projetsameh.data.Culture
import com.example.projetsameh.viewmodel.CultureViewModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.text.DecimalFormat

class MainActivity : AppCompatActivity(), SensorEventListener {
    private lateinit var viewModel: CultureViewModel
    private lateinit var adapter: CultureAdapter
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var sensorManager: SensorManager
    private var temperatureSensor: Sensor? = null
    private var humiditySensor: Sensor? = null

    private lateinit var temperatureActuelle: TextView
    private lateinit var humiditeActuelle: TextView
    private val decimalFormat = DecimalFormat("#0.0")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialisation des vues
        temperatureActuelle = findViewById(R.id.temperatureActuelle)
        humiditeActuelle = findViewById(R.id.humiditeActuelle)

        // Initialisation du ViewModel
        viewModel = ViewModelProvider(this)[CultureViewModel::class.java]

        // Initialisation de la RecyclerView
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerViewCultures)
        adapter = CultureAdapter(
            onItemClick = { culture -> modifierCulture(culture) },
            onItemLongClick = { culture -> supprimerCulture(culture) }
        )
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Observation des cultures
        viewModel.allCultures.observe(this) { cultures ->
            adapter.submitList(cultures)
        }

        // Initialisation des services de localisation
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Initialisation des capteurs
        initialiserCapteurs()

        // Configuration du FAB
        findViewById<FloatingActionButton>(R.id.fabAjouterCulture).setOnClickListener {
            ajouterCulture()
        }

        // Vérification des permissions
        verifierPermissions()
    }

    private fun initialiserCapteurs() {
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        
        // Initialisation des capteurs individuels
        temperatureSensor = sensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE)
        humiditySensor = sensorManager.getDefaultSensor(Sensor.TYPE_RELATIVE_HUMIDITY)
        
        // Vérification des capteurs disponibles
        val sensors = sensorManager.getSensorList(Sensor.TYPE_ALL)
        Log.d("Sensors", "Liste des capteurs disponibles :")
        sensors.forEach { sensor ->
            Log.d("Sensors", "Capteur : ${sensor.name} (Type: ${sensor.type})")
        }
        
        if (temperatureSensor == null) {
            temperatureActuelle.text = "Température : Non disponible"
            Toast.makeText(this, "Capteur de température non disponible", Toast.LENGTH_LONG).show()
        } else {
            Log.d("Sensors", "Capteur de température trouvé : ${temperatureSensor?.name}")
            Log.d("Sensors", "Résolution du capteur de température : ${temperatureSensor?.resolution}")
            Log.d("Sensors", "Plage du capteur de température : ${temperatureSensor?.maximumRange}°C")
        }
        if (humiditySensor == null) {
            humiditeActuelle.text = "Humidité : Non disponible"
            Toast.makeText(this, "Capteur d'humidité non disponible", Toast.LENGTH_LONG).show()
        } else {
            Log.d("Sensors", "Capteur d'humidité trouvé : ${humiditySensor?.name}")
            Log.d("Sensors", "Résolution du capteur d'humidité : ${humiditySensor?.resolution}")
            Log.d("Sensors", "Plage du capteur d'humidité : ${humiditySensor?.maximumRange}%")
        }
    }

    override fun onResume() {
        super.onResume()
        // Enregistrement des capteurs disponibles
        temperatureSensor?.let { 
            val success = sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
            Log.d("Sensors", "Enregistrement du capteur de température : ${if (success) "succès" else "échec"}")
        }
        humiditySensor?.let { 
            val success = sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
            Log.d("Sensors", "Enregistrement du capteur d'humidité : ${if (success) "succès" else "échec"}")
        }
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    private fun verifierPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        }
    }

    private fun getLastLocation(onLocationReceived: (Location) -> Unit) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location: Location? ->
                    location?.let {
                        onLocationReceived(it)
                    } ?: run {
                        Toast.makeText(this, R.string.erreur_gps, Toast.LENGTH_LONG).show()
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("Location", "Erreur lors de l'obtention de la position", e)
                    Toast.makeText(this, R.string.erreur_gps, Toast.LENGTH_LONG).show()
                }
        } else {
            Toast.makeText(this, R.string.erreur_gps, Toast.LENGTH_LONG).show()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            LOCATION_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Permission de localisation refusée", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun ajouterCulture() {
        CultureDialog(this) { culture ->
            getLastLocation { location ->
                culture.latitude = location.latitude
                culture.longitude = location.longitude
                viewModel.insertCulture(culture)
            }
        }.show()
    }

    private fun modifierCulture(culture: Culture) {
        CultureDialog(this, culture) { updatedCulture ->
            getLastLocation { location ->
                updatedCulture.latitude = location.latitude
                updatedCulture.longitude = location.longitude
                viewModel.updateCulture(updatedCulture)
            }
        }.show()
    }

    private fun supprimerCulture(culture: Culture) {
        AlertDialog.Builder(this)
            .setTitle(R.string.confirmation_suppression)
            .setMessage(R.string.message_confirmation_suppression)
            .setPositiveButton(R.string.oui) { _, _ ->
                viewModel.deleteCulture(culture)
            }
            .setNegativeButton(R.string.non, null)
            .show()
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            Log.d("Sensors", "Valeurs reçues du capteur ${it.sensor.name} : ${it.values.joinToString()}")
            when (it.sensor.type) {
                Sensor.TYPE_AMBIENT_TEMPERATURE -> {
                    val temperature = it.values[0]
                    Log.d("Sensors", "Température brute : $temperature")
                    if (temperature in -100.0..100.0) {
                        temperatureActuelle.text = getString(R.string.temperature_actuelle, temperature)
                    } else {
                        Log.e("Sensors", "Température hors plage : $temperature")
                        temperatureActuelle.text = "Température : Erreur de lecture"
                    }
                }
                Sensor.TYPE_RELATIVE_HUMIDITY -> {
                    val humidite = it.values[0]
                    Log.d("Sensors", "Humidité brute : $humidite")
                    if (humidite in 0.0..100.0) {
                        humiditeActuelle.text = getString(R.string.humidite_actuelle, humidite)
                    } else {
                        Log.e("Sensors", "Humidité hors plage : $humidite")
                        humiditeActuelle.text = "Humidité : Erreur de lecture"
                    }
                }
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Non utilisé
    }

    companion object {
        private const val PERMISSIONS_REQUEST_CODE = 100
        private const val LOCATION_PERMISSION_REQUEST_CODE = 101
    }
}