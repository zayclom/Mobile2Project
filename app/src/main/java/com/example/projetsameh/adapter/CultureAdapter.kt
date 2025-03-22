package com.example.projetsameh.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.projetsameh.R
import com.example.projetsameh.data.Culture
import java.text.DecimalFormat

class CultureAdapter(
    private val onItemClick: (Culture) -> Unit,
    private val onItemLongClick: (Culture) -> Unit
) : ListAdapter<Culture, CultureAdapter.CultureViewHolder>(CultureDiffCallback()) {

    private val decimalFormat = DecimalFormat("#0.0")

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CultureViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_culture, parent, false)
        return CultureViewHolder(view)
    }

    override fun onBindViewHolder(holder: CultureViewHolder, position: Int) {
        val culture = getItem(position)
        Log.d("CultureAdapter", "Binding culture at position $position: ${culture.nom}")
        holder.bind(culture)
    }

    override fun submitList(list: List<Culture>?) {
        Log.d("CultureAdapter", "Submitting new list with ${list?.size ?: 0} items")
        super.submitList(list)
    }

    inner class CultureViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imageViewType: ImageView = itemView.findViewById(R.id.imageViewType)
        private val textViewNom: TextView = itemView.findViewById(R.id.textViewNom)
        private val textViewAdresse: TextView = itemView.findViewById(R.id.textViewAdresse)
        private val textViewDate: TextView = itemView.findViewById(R.id.textViewDate)
        private val textViewEtat: TextView = itemView.findViewById(R.id.textViewEtat)
        private val textViewBesoins: TextView = itemView.findViewById(R.id.textViewBesoins)
        private val textViewTemperature: TextView = itemView.findViewById(R.id.textViewTemperature)
        private val textViewHumidite: TextView = itemView.findViewById(R.id.textViewHumidite)

        init {
            itemView.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClick(getItem(position))
                }
            }

            itemView.setOnLongClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemLongClick(getItem(position))
                }
                true
            }
        }

        fun bind(culture: Culture) {
            Log.d("CultureAdapter", "Binding culture: ${culture.nom}")
            textViewNom.text = culture.nom
            textViewAdresse.text = culture.adresse
            textViewDate.text = culture.datePlantation
            textViewEtat.text = culture.etat
            textViewBesoins.text = culture.besoins
            
            // Affichage des températures min/max
            textViewTemperature.text = "Temp: ${decimalFormat.format(culture.temperatureMin)}°C - ${decimalFormat.format(culture.temperatureMax)}°C"
            
            // Affichage des humidités min/max
            textViewHumidite.text = "Hum: ${decimalFormat.format(culture.humiditeMin)}% - ${decimalFormat.format(culture.humiditeMax)}%"

            // Définition de l'icône selon le type de culture
            val iconResId = when (culture.type) {
                "Céréales" -> R.drawable.ic_cereales
                "Légumes" -> R.drawable.ic_legumes
                "Fruits" -> R.drawable.ic_fruits
                else -> R.drawable.ic_autre
            }
            imageViewType.setImageResource(iconResId)
        }
    }

    private class CultureDiffCallback : DiffUtil.ItemCallback<Culture>() {
        override fun areItemsTheSame(oldItem: Culture, newItem: Culture): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Culture, newItem: Culture): Boolean {
            return oldItem == newItem
        }
    }
} 