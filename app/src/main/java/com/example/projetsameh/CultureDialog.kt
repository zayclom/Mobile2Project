package com.example.projetsameh

import android.content.Context
import android.view.LayoutInflater
import android.widget.ArrayAdapter
import androidx.appcompat.app.AlertDialog
import com.example.projetsameh.data.Culture
import com.example.projetsameh.data.TypeCulture
import com.example.projetsameh.databinding.DialogCultureBinding

class CultureDialog(
    context: Context,
    private val culture: Culture? = null,
    private val onSave: (Culture) -> Unit
) {
    private val binding = DialogCultureBinding.inflate(LayoutInflater.from(context))
    private val dialog = AlertDialog.Builder(context)
        .setTitle(if (culture == null) R.string.ajouter_culture else R.string.modifier_culture)
        .setView(binding.root)
        .setPositiveButton(R.string.enregistrer, null)
        .setNegativeButton(R.string.annuler, null)
        .create()

    init {
        // Configuration du dropdown pour le type de culture
        val types = TypeCulture.values().map { it.name }
        val adapter = ArrayAdapter(context, android.R.layout.simple_dropdown_item_1line, types)
        binding.typeCultureInput.setAdapter(adapter)

        // Remplissage des champs si modification
        culture?.let {
            binding.nomInput.setText(it.nom)
            binding.typeCultureInput.setText(it.typeCulture.name)
            binding.temperatureMinInput.setText(it.temperatureMin.toString())
            binding.temperatureMaxInput.setText(it.temperatureMax.toString())
            binding.humiditeMinInput.setText(it.humiditeMin.toString())
            binding.humiditeMaxInput.setText(it.humiditeMax.toString())
            binding.commentairesInput.setText(it.commentaires)
        }

        // Validation avant sauvegarde
        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                if (validateInputs()) {
                    val newCulture = Culture(
                        id = culture?.id ?: 0,
                        nom = binding.nomInput.text.toString(),
                        typeCulture = TypeCulture.valueOf(binding.typeCultureInput.text.toString()),
                        temperatureMin = binding.temperatureMinInput.text.toString().toDouble(),
                        temperatureMax = binding.temperatureMaxInput.text.toString().toDouble(),
                        humiditeMin = binding.humiditeMinInput.text.toString().toDouble(),
                        humiditeMax = binding.humiditeMaxInput.text.toString().toDouble(),
                        dateEnregistrement = culture?.dateEnregistrement ?: java.util.Date(),
                        adresse = "", // Sera mis à jour avec le GPS
                        latitude = 0.0, // Sera mis à jour avec le GPS
                        longitude = 0.0, // Sera mis à jour avec le GPS
                        commentaires = binding.commentairesInput.text.toString()
                    )
                    onSave(newCulture)
                    dialog.dismiss()
                }
            }
        }
    }

    private fun validateInputs(): Boolean {
        var isValid = true

        // Validation du nom
        if (binding.nomInput.text.isNullOrBlank()) {
            binding.nomLayout.error = binding.root.context.getString(R.string.erreur_champ_obligatoire)
            isValid = false
        }

        // Validation du type de culture
        if (binding.typeCultureInput.text.isNullOrBlank()) {
            binding.typeCultureLayout.error = binding.root.context.getString(R.string.erreur_champ_obligatoire)
            isValid = false
        }

        // Validation des températures
        val tempMin = binding.temperatureMinInput.text.toString().toDoubleOrNull()
        val tempMax = binding.temperatureMaxInput.text.toString().toDoubleOrNull()
        if (tempMin == null || tempMin < -50 || tempMin > 50) {
            binding.temperatureMinLayout.error = binding.root.context.getString(R.string.erreur_temperature_invalide)
            isValid = false
        }
        if (tempMax == null || tempMax < -50 || tempMax > 50) {
            binding.temperatureMaxLayout.error = binding.root.context.getString(R.string.erreur_temperature_invalide)
            isValid = false
        }

        // Validation des humidités
        val humiditeMin = binding.humiditeMinInput.text.toString().toDoubleOrNull()
        val humiditeMax = binding.humiditeMaxInput.text.toString().toDoubleOrNull()
        if (humiditeMin == null || humiditeMin < 0 || humiditeMin > 100) {
            binding.humiditeMinLayout.error = binding.root.context.getString(R.string.erreur_humidite_invalide)
            isValid = false
        }
        if (humiditeMax == null || humiditeMax < 0 || humiditeMax > 100) {
            binding.humiditeMaxLayout.error = binding.root.context.getString(R.string.erreur_humidite_invalide)
            isValid = false
        }

        return isValid
    }

    fun show() {
        dialog.show()
    }
} 