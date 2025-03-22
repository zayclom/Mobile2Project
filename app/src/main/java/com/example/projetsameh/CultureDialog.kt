package com.example.projetsameh

import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.example.projetsameh.data.Culture
import com.google.android.material.textfield.TextInputLayout

class CultureDialog : DialogFragment() {
    private var culture: Culture? = null
    private var onCultureSaved: ((Culture) -> Unit)? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("CultureDialog", "onCreate called")
        arguments?.let {
            culture = it.getParcelable(ARG_CULTURE)
            Log.d("CultureDialog", "Culture from arguments: ${culture?.nom}")
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        Log.d("CultureDialog", "onCreateDialog called")
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_culture, null)
        
        // Initialize views
        val nomInput = view.findViewById<TextInputLayout>(R.id.nomInput)
        val typeCultureInput = view.findViewById<TextInputLayout>(R.id.typeCultureInput)
        val temperatureMinInput = view.findViewById<TextInputLayout>(R.id.temperatureMinInput)
        val temperatureMaxInput = view.findViewById<TextInputLayout>(R.id.temperatureMaxInput)
        val humiditeMinInput = view.findViewById<TextInputLayout>(R.id.humiditeMinInput)
        val humiditeMaxInput = view.findViewById<TextInputLayout>(R.id.humiditeMaxInput)
        val commentairesInput = view.findViewById<TextInputLayout>(R.id.commentairesInput)

        // Setup type culture dropdown
        val types = arrayOf("Céréales", "Légumes", "Fruits", "Autre")
        val adapter = ArrayAdapter<String>(requireContext(), android.R.layout.simple_dropdown_item_1line, types)
        (typeCultureInput.editText as? AutoCompleteTextView)?.setAdapter(adapter)

        // Populate fields if editing
        culture?.let {
            Log.d("CultureDialog", "Populating fields with existing culture: ${it.nom}")
            nomInput.editText?.setText(it.nom)
            typeCultureInput.editText?.setText(it.type)
            temperatureMinInput.editText?.setText(it.temperatureMin.toString())
            temperatureMaxInput.editText?.setText(it.temperatureMax.toString())
            humiditeMinInput.editText?.setText(it.humiditeMin.toString())
            humiditeMaxInput.editText?.setText(it.humiditeMax.toString())
            commentairesInput.editText?.setText(it.commentaires)
        }

        val dialog = AlertDialog.Builder(requireContext())
            .setTitle(if (culture == null) "Nouvelle culture" else "Modifier la culture")
            .setView(view)
            .setPositiveButton("Enregistrer", null) // Set to null initially
            .setNegativeButton("Annuler") { dialog, _ ->
                Log.d("CultureDialog", "Negative button clicked")
                dialog.dismiss()
            }
            .create()

        // Set up the positive button click listener after dialog creation
        dialog.setOnShowListener {
            val positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            positiveButton.setOnClickListener {
                Log.d("CultureDialog", "Positive button clicked")
                if (validateInputs(nomInput, typeCultureInput, temperatureMinInput, temperatureMaxInput, 
                    humiditeMinInput, humiditeMaxInput)) {
                    val updatedCulture = if (culture != null) {
                        // Update existing culture
                        culture!!.copy(
                            nom = nomInput.editText?.text.toString(),
                            type = typeCultureInput.editText?.text.toString(),
                            temperatureMin = temperatureMinInput.editText?.text.toString().toDoubleOrNull() ?: 0.0,
                            temperatureMax = temperatureMaxInput.editText?.text.toString().toDoubleOrNull() ?: 0.0,
                            humiditeMin = humiditeMinInput.editText?.text.toString().toDoubleOrNull() ?: 0.0,
                            humiditeMax = humiditeMaxInput.editText?.text.toString().toDoubleOrNull() ?: 0.0,
                            commentaires = commentairesInput.editText?.text.toString()
                        )
                    } else {
                        // Create new culture
                        Culture(
                            nom = nomInput.editText?.text.toString(),
                            type = typeCultureInput.editText?.text.toString(),
                            temperatureMin = temperatureMinInput.editText?.text.toString().toDoubleOrNull() ?: 0.0,
                            temperatureMax = temperatureMaxInput.editText?.text.toString().toDoubleOrNull() ?: 0.0,
                            humiditeMin = humiditeMinInput.editText?.text.toString().toDoubleOrNull() ?: 0.0,
                            humiditeMax = humiditeMaxInput.editText?.text.toString().toDoubleOrNull() ?: 0.0,
                            commentaires = commentairesInput.editText?.text.toString()
                        )
                    }
                    Log.d("CultureDialog", "Created ${if (culture != null) "updated" else "new"} culture: ${updatedCulture.nom}")
                    onCultureSaved?.invoke(updatedCulture)
                    dialog.dismiss()
                }
            }
        }

        return dialog
    }

    private fun validateInputs(
        nomInput: TextInputLayout,
        typeCultureInput: TextInputLayout,
        temperatureMinInput: TextInputLayout,
        temperatureMaxInput: TextInputLayout,
        humiditeMinInput: TextInputLayout,
        humiditeMaxInput: TextInputLayout
    ): Boolean {
        Log.d("CultureDialog", "Validating inputs")
        var isValid = true

        // Clear previous errors
        nomInput.error = null
        typeCultureInput.error = null
        temperatureMinInput.error = null
        temperatureMaxInput.error = null
        humiditeMinInput.error = null
        humiditeMaxInput.error = null

        // Validate name
        if (nomInput.editText?.text.isNullOrBlank()) {
            Log.d("CultureDialog", "Validation failed: Name is empty")
            nomInput.error = "Le nom est requis"
            isValid = false
        }

        // Validate type
        if (typeCultureInput.editText?.text.isNullOrBlank()) {
            Log.d("CultureDialog", "Validation failed: Type is empty")
            typeCultureInput.error = "Le type est requis"
            isValid = false
        }

        // Validate temperatures
        val tempMin = temperatureMinInput.editText?.text.toString().toDoubleOrNull()
        val tempMax = temperatureMaxInput.editText?.text.toString().toDoubleOrNull()
        
        if (tempMin == null || tempMin < -50 || tempMin > 50) {
            Log.d("CultureDialog", "Validation failed: Temperature min is invalid: $tempMin")
            temperatureMinInput.error = "Température entre -50°C et 50°C"
            isValid = false
        }

        if (tempMax == null || tempMax < -50 || tempMax > 50) {
            Log.d("CultureDialog", "Validation failed: Temperature max is invalid: $tempMax")
            temperatureMaxInput.error = "Température entre -50°C et 50°C"
            isValid = false
        }

        if (tempMin != null && tempMax != null && tempMin > tempMax) {
            Log.d("CultureDialog", "Validation failed: Temperature min ($tempMin) is greater than max ($tempMax)")
            temperatureMinInput.error = "La température minimale doit être inférieure à la maximale"
            temperatureMaxInput.error = "La température maximale doit être supérieure à la minimale"
            isValid = false
        }

        // Validate humidities
        val humMin = humiditeMinInput.editText?.text.toString().toDoubleOrNull()
        val humMax = humiditeMaxInput.editText?.text.toString().toDoubleOrNull()
        
        if (humMin == null || humMin < 0 || humMin > 100) {
            Log.d("CultureDialog", "Validation failed: Humidity min is invalid: $humMin")
            humiditeMinInput.error = "Humidité entre 0% et 100%"
            isValid = false
        }

        if (humMax == null || humMax < 0 || humMax > 100) {
            Log.d("CultureDialog", "Validation failed: Humidity max is invalid: $humMax")
            humiditeMaxInput.error = "Humidité entre 0% et 100%"
            isValid = false
        }

        if (humMin != null && humMax != null && humMin > humMax) {
            Log.d("CultureDialog", "Validation failed: Humidity min ($humMin) is greater than max ($humMax)")
            humiditeMinInput.error = "L'humidité minimale doit être inférieure à la maximale"
            humiditeMaxInput.error = "L'humidité maximale doit être supérieure à la minimale"
            isValid = false
        }

        Log.d("CultureDialog", "Validation result: $isValid")
        return isValid
    }

    companion object {
        private const val ARG_CULTURE = "culture"

        fun newInstance(culture: Culture? = null, onCultureSaved: (Culture) -> Unit): CultureDialog {
            Log.d("CultureDialog", "Creating new instance with culture: ${culture?.nom}")
            return CultureDialog().apply {
                arguments = Bundle().apply {
                    putParcelable(ARG_CULTURE, culture)
                }
                this.onCultureSaved = onCultureSaved
            }
        }
    }
} 