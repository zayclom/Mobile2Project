package com.example.projetsameh

import android.Manifest
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.os.Bundle
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
        
        // Vérification des capteurs disponibles
        val sensors = sensorManager.getSensorList(Sensor.TYPE_ALL)
        val capteursDisponibles = sensors.map { it.type }.toSet()
        
        // Initialisation du capteur de température
        temperatureSensor = if (capteursDisponibles.contains(Sensor.TYPE_AMBIENT_TEMPERATURE)) {
            sensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE)
        } else {
            null
        }
        
        // Initialisation du capteur d'humidité
        humiditySensor = if (capteursDisponibles.contains(Sensor.TYPE_RELATIVE_HUMIDITY)) {
            sensorManager.getDefaultSensor(Sensor.TYPE_RELATIVE_HUMIDITY)
        } else {
            null
        }

        // Affichage des messages si les capteurs ne sont pas disponibles
        if (temperatureSensor == null) {
            temperatureActuelle.text = "Température : Non disponible"
            Toast.makeText(this, "Capteur de température non disponible", Toast.LENGTH_LONG).show()
        }
        if (humiditySensor == null) {
            humiditeActuelle.text = "Humidité : Non disponible"
            Toast.makeText(this, "Capteur d'humidité non disponible", Toast.LENGTH_LONG).show()
        }
    }

    override fun onResume() {
        super.onResume()
        // Enregistrement des capteurs disponibles
        temperatureSensor?.let { 
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
        humiditySensor?.let { 
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    private fun verifierPermissions() {
        val permissions = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )

        val permissionsToRequest = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }.toTypedArray()

        if (permissionsToRequest.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, permissionsToRequest, PERMISSIONS_REQUEST_CODE)
        }
    }

    private fun ajouterCulture() {
        CultureDialog(this) { culture ->
            // Mise à jour de la position GPS
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                fusedLocationClient.lastLocation
                    .addOnSuccessListener { location: Location? ->
                        location?.let {
                            culture.latitude = it.latitude
                            culture.longitude = it.longitude
                            // TODO: Implémenter le géocodage pour obtenir l'adresse
                            viewModel.insertCulture(culture)
                        } ?: run {
                            Toast.makeText(this, R.string.erreur_gps, Toast.LENGTH_LONG).show()
                        }
                    }
            } else {
                Toast.makeText(this, R.string.erreur_gps, Toast.LENGTH_LONG).show()
                viewModel.insertCulture(culture)
            }
        }.show()
    }

    private fun modifierCulture(culture: Culture) {
        CultureDialog(this, culture) { updatedCulture ->
            // Mise à jour de la position GPS
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                fusedLocationClient.lastLocation
                    .addOnSuccessListener { location: Location? ->
                        location?.let {
                            updatedCulture.latitude = it.latitude
                            updatedCulture.longitude = it.longitude
                            // TODO: Implémenter le géocodage pour obtenir l'adresse
                            viewModel.updateCulture(updatedCulture)
                        } ?: run {
                            Toast.makeText(this, R.string.erreur_gps, Toast.LENGTH_LONG).show()
                            viewModel.updateCulture(updatedCulture)
                        }
                    }
            } else {
                Toast.makeText(this, R.string.erreur_gps, Toast.LENGTH_LONG).show()
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
            when (it.sensor.type) {
                Sensor.TYPE_AMBIENT_TEMPERATURE -> {
                    val temperature = it.values[0]
                    if (temperature in -50.0..50.0) { // Vérification de la plage valide
                        temperatureActuelle.text = getString(R.string.temperature_actuelle, temperature)
                    } else {
                        temperatureActuelle.text = "Température : Erreur de lecture"
                    }
                }
                Sensor.TYPE_RELATIVE_HUMIDITY -> {
                    val humidite = it.values[0]
                    if (humidite in 0.0..100.0) { // Vérification de la plage valide
                        humiditeActuelle.text = getString(R.string.humidite_actuelle, humidite)
                    } else {
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
    }
}