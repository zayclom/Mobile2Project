package com.example.projetsameh.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "cultures")
data class Culture(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    var nom: String,
    var adresse: String,
    var latitude: Double,
    var longitude: Double,
    var temperatureMin: Double,
    var temperatureMax: Double,
    var humiditeMin: Double,
    var humiditeMax: Double,
    val dateEnregistrement: Date,
    var typeCulture: TypeCulture,
    var commentaires: String? = null
)

enum class TypeCulture {
    CEREALES,
    LEGUMES,
    FRUITS,
    AUTRE
} 