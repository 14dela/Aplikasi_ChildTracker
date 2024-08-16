package com.example.map

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions

class LocationAdapter(
    options: FirebaseRecyclerOptions<LocationData>
) : FirebaseRecyclerAdapter<LocationData, LocationAdapter.LocationViewHolder>(options) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LocationViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_list, parent, false)
        return LocationViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: LocationViewHolder, position: Int, model: LocationData) {
        holder.bind(model)
    }

    class LocationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val bateraiTextView: TextView = itemView.findViewById(R.id.baterai)
        private val jarakTextView: TextView = itemView.findViewById(R.id.jarak)
        private val latitudeTextView: TextView = itemView.findViewById(R.id.latitude)
        private val longitudeTextView: TextView = itemView.findViewById(R.id.longitude)
        private val namaTextView: TextView = itemView.findViewById(R.id.nama)

        fun bind(locationData: LocationData) {
            bateraiTextView.text = locationData.baterai?.toString() ?: ""
            jarakTextView.text = locationData.jarak?.toString() ?: ""
            latitudeTextView.text = locationData.latitude?.toString() ?: ""
            longitudeTextView.text = locationData.longitude?.toString() ?: ""
            namaTextView.text = locationData.nama ?: ""
        }
    }
}
