package com.example.projetsameh.adapter

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
import com.example.projetsameh.data.TypeCulture
import java.text.SimpleDateFormat
import java.util.Locale

class CultureAdapter(
    private val onItemClick: (Culture) -> Unit,
    private val onItemLongClick: (Culture) -> Unit
) : ListAdapter<Culture, CultureAdapter.CultureViewHolder>(CultureDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CultureViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_culture, parent, false)
        return CultureViewHolder(view)
    }

    override fun onBindViewHolder(holder: CultureViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class CultureViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val typeCultureIcon: ImageView = itemView.findViewById(R.id.typeCultureIcon)
        private val nomCulture: TextView = itemView.findViewById(R.id.nomCulture)
        private val adresseCulture: TextView = itemView.findViewById(R.id.adresseCulture)
        private val temperatureText: TextView = itemView.findViewById(R.id.temperatureText)
        private val humiditeText: TextView = itemView.findViewById(R.id.humiditeText)
        private val dateEnregistrement: TextView = itemView.findViewById(R.id.dateEnregistrement)

        private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClick(getItem(position))
                }
            }

            itemView.setOnLongClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemLongClick(getItem(position))
                }
                true
            }
        }

        fun bind(culture: Culture) {
            nomCulture.text = culture.nom
            adresseCulture.text = culture.adresse
            temperatureText.text = itemView.context.getString(
                R.string.temperature_range,
                culture.temperatureMin,
                culture.temperatureMax
            )
            humiditeText.text = itemView.context.getString(
                R.string.humidite_range,
                culture.humiditeMin,
                culture.humiditeMax
            )
            dateEnregistrement.text = dateFormat.format(culture.dateEnregistrement)

            typeCultureIcon.setImageResource(
                when (culture.typeCulture) {
                    TypeCulture.CEREALES -> R.drawable.ic_cereales
                    TypeCulture.LEGUMES -> R.drawable.ic_legumes
                    TypeCulture.FRUITS -> R.drawable.ic_fruits
                    TypeCulture.AUTRE -> R.drawable.ic_autre
                }
            )
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