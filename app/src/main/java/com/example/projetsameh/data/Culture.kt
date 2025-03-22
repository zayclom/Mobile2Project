package com.example.projetsameh.data

import android.os.Parcelable
import com.google.firebase.database.IgnoreExtraProperties
import kotlinx.parcelize.Parcelize

@Parcelize
@IgnoreExtraProperties
data class Culture(
    var id: String = "",
    var nom: String = "",
    var type: String = "",
    var datePlantation: String = "",
    var etat: String = "",
    var besoins: String = "",
    var latitude: Double = 0.0,
    var longitude: Double = 0.0,
    var adresse: String = "",
    var temperatureMin: Double = 0.0,
    var temperatureMax: Double = 0.0,
    var humiditeMin: Double = 0.0,
    var humiditeMax: Double = 0.0,
    var commentaires: String = ""
) : Parcelable 