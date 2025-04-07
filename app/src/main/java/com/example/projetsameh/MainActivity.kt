package com.example.projetsameh

import android.Manifest
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.ViewGroup
import android.widget.LinearLayout
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
import android.location.Geocoder
import java.util.*

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

    // Variables pour la gestion de l'orientation
    private var currentOrientation: Int = Configuration.ORIENTATION_PORTRAIT
    private var lastTemperature: Float? = null
    private var lastHumidity: Float? = null

    companion object {
        private const val PERMISSIONS_REQUEST_CODE = 100
        private const val LOCATION_PERMISSION_REQUEST_CODE = 101
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        try {
            super.onCreate(savedInstanceState)
            Log.e("MainActivity", "onCreate started")

            // Détecter et configurer l'orientation initiale
            configureOrientation(resources.configuration.orientation)

            // Initialisation des vues
            initializeViews()

            // Initialisation du ViewModel
            initializeViewModel()

            // Initialisation de la RecyclerView
            initializeRecyclerView()

            // Observation des cultures
            observeCultures()

            // Initialisation des services de localisation
            initializeLocationServices()

            // Initialisation des capteurs
            initialiserCapteurs()

            // Configuration du FAB
            setupFab()

            // Vérification des permissions
            verifierPermissions()
            Log.e("MainActivity", "onCreate completed successfully")
        } catch (e: Exception) {
            Log.e("MainActivity", "Fatal error in onCreate", e)
            Toast.makeText(this, "Error starting app: ${e.message}", Toast.LENGTH_LONG).show()
            throw e
        }
    }

    private fun configureOrientation(orientation: Int) {
        currentOrientation = orientation
        when (orientation) {
            Configuration.ORIENTATION_LANDSCAPE -> setContentView(R.layout.activity_main)
            else -> setContentView(R.layout.activity_main)
        }
    }

    private fun initializeViews() {
        try {
            temperatureActuelle = findViewById(R.id.temperatureActuelle)
            humiditeActuelle = findViewById(R.id.humiditeActuelle)
            Log.e("MainActivity", "Views initialized")
        } catch (e: Exception) {
            Log.e("MainActivity", "Error initializing views", e)
            throw e
        }
    }

    private fun initializeViewModel() {
        try {
            viewModel = ViewModelProvider(this)[CultureViewModel::class.java]
            Log.e("MainActivity", "ViewModel initialized")
        } catch (e: Exception) {
            Log.e("MainActivity", "Error initializing ViewModel", e)
            throw e
        }
    }

    private fun initializeRecyclerView() {
        try {
            val recyclerView = findViewById<RecyclerView>(R.id.recyclerViewCultures)
            adapter = CultureAdapter(
                onItemClick = { culture -> modifierCulture(culture) },
                onItemLongClick = { culture -> supprimerCulture(culture) }
            )
            recyclerView.adapter = adapter
            recyclerView.layoutManager = if (currentOrientation == Configuration.ORIENTATION_LANDSCAPE) {
                LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
            } else {
                LinearLayoutManager(this)
            }
            Log.e("MainActivity", "RecyclerView initialized")
        } catch (e: Exception) {
            Log.e("MainActivity", "Error initializing RecyclerView", e)
            throw e
        }
    }

    private fun observeCultures() {
        viewModel.allCultures.observe(this) { cultures ->
            Log.e("MainActivity", "Received cultures from ViewModel: ${cultures.size}")
            adapter.submitList(cultures)
        }
    }

    private fun initializeLocationServices() {
        try {
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
            Log.e("MainActivity", "Location services initialized")
        } catch (e: Exception) {
            Log.e("MainActivity", "Error initializing location services", e)
            throw e
        }
    }

    private fun setupFab() {
        try {
            findViewById<FloatingActionButton>(R.id.fabAjouterCulture).setOnClickListener {
                Log.e("MainActivity", "FAB clicked")
                ajouterCulture()
            }
            Log.e("MainActivity", "FAB initialized")
        } catch (e: Exception) {
            Log.e("MainActivity", "Error initializing FAB", e)
            throw e
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        Log.d("Orientation", "Changement d'orientation détecté")

        // Sauvegarder les valeurs des capteurs
        saveSensorValues()

        // Reconfigurer l'interface
        configureOrientation(newConfig.orientation)

        // Réinitialiser les composants
        initializeAfterRotation()
    }

    private fun saveSensorValues() {
        try {
            temperatureActuelle.text.toString().let {
                if (it.contains("Température : ")) {
                    lastTemperature = it.replace("Température : ", "").replace("°C", "").toFloatOrNull()
                }
            }
            humiditeActuelle.text.toString().let {
                if (it.contains("Humidité : ")) {
                    lastHumidity = it.replace("Humidité : ", "").replace("%", "").toFloatOrNull()
                }
            }
        } catch (e: Exception) {
            Log.e("Orientation", "Erreur sauvegarde valeurs capteurs", e)
        }
    }

    private fun initializeAfterRotation() {
        // Réinitialiser les vues
        initializeViews()

        // Restaurer les valeurs des capteurs
        restoreSensorValues()

        // Réinitialiser la RecyclerView
        initializeRecyclerView()

        // Réinitialiser le FAB
        setupFab()

        // Réenregistrer les capteurs
        registerSensors()
    }

    private fun restoreSensorValues() {
        lastTemperature?.let {
            temperatureActuelle.text = getString(R.string.temperature_actuelle, it)
        }
        lastHumidity?.let {
            humiditeActuelle.text = getString(R.string.humidite_actuelle, it)
        }
    }

    private fun registerSensors() {
        temperatureSensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
        humiditySensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    private fun initialiserCapteurs() {
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager

        temperatureSensor = sensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE)
        humiditySensor = sensorManager.getDefaultSensor(Sensor.TYPE_RELATIVE_HUMIDITY)

        val sensors = sensorManager.getSensorList(Sensor.TYPE_ALL)
        Log.d("Sensors", "Liste des capteurs disponibles :")
        sensors.forEach { sensor ->
            Log.d("Sensors", "Capteur : ${sensor.name} (Type: ${sensor.type})")
        }

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
        registerSensors()

        // Ajustements spécifiques à l'orientation
        if (currentOrientation == Configuration.ORIENTATION_LANDSCAPE) {
            adjustLandscapeLayout()
        }
    }

    private fun adjustLandscapeLayout() {
        // Exemple d'ajustement pour le paysage
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerViewCultures)
        val params = recyclerView.layoutParams as LinearLayout.LayoutParams
        params.weight = 2f
        recyclerView.layoutParams = params
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

    private fun getAddressFromLocation(latitude: Double, longitude: Double, onAddressReceived: (String) -> Unit) {
        try {
            val geocoder = Geocoder(this, Locale.getDefault())
            val addresses = geocoder.getFromLocation(latitude, longitude, 1)

            if (addresses != null && addresses.isNotEmpty()) {
                val address = addresses[0]
                val addressText = buildString {
                    address.getAddressLine(0)?.let { append(it) }
                    address.locality?.let { append(", $it") }
                    address.postalCode?.let { append(" $it") }
                    address.countryName?.let { append(", $it") }
                }
                onAddressReceived(addressText)
            } else {
                onAddressReceived("Adresse non disponible")
            }
        } catch (e: Exception) {
            Log.e("Geocoding", "Erreur lors du géocodage", e)
            onAddressReceived("Erreur de géocodage")
        }
    }

    private fun ajouterCulture() {
        Log.d("MainActivity", "Starting ajouterCulture")
        getLastLocation { location ->
            Log.d("MainActivity", "Location received: ${location.latitude}, ${location.longitude}")
            val dialog = CultureDialog.newInstance(
                culture = null,
                onCultureSaved = { culture ->
                    Log.d("MainActivity", "Culture saved callback received")
                    culture.latitude = location.latitude
                    culture.longitude = location.longitude
                    getAddressFromLocation(location.latitude, location.longitude) { address ->
                        Log.d("MainActivity", "Address received: $address")
                        culture.adresse = address
                        val currentDate = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
                            .format(java.util.Date())
                        culture.datePlantation = currentDate
                        culture.etat = "En cours"
                        culture.besoins = "Normal"

                        Log.d("MainActivity", "Inserting culture into Firebase: ${culture.nom}")
                        viewModel.insertCulture(culture)

                        Toast.makeText(this, "Culture ajoutée avec succès", Toast.LENGTH_SHORT).show()
                    }
                }
            )
            dialog.show(supportFragmentManager, "CultureDialog")
        }
    }

    private fun modifierCulture(culture: Culture) {
        val dialog = CultureDialog.newInstance(
            culture = culture,
            onCultureSaved = { updatedCulture ->
                viewModel.updateCulture(updatedCulture)
                Toast.makeText(this, "Culture modifiée avec succès", Toast.LENGTH_SHORT).show()
            }
        )
        dialog.show(supportFragmentManager, "CultureDialog")
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
                        lastTemperature = temperature
                    } else {
                        Log.e("Sensors", "Température hors plage : $temperature")
                        temperatureActuelle.text = "Température : En attente de réponse"
                    }
                }
                Sensor.TYPE_RELATIVE_HUMIDITY -> {
                    val humidite = it.values[0]
                    Log.d("Sensors", "Humidité brute : $humidite")
                    if (humidite in 0.0..100.0) {
                        humiditeActuelle.text = getString(R.string.humidite_actuelle, humidite)
                        lastHumidity = humidite
                    } else {
                        Log.e("Sensors", "Humidité hors plage : $humidite")
                        humiditeActuelle.text = "Humidité : En attente de réponse"
                    }
                }
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Non utilisé
    }
}