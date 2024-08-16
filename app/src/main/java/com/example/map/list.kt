package com.example.map

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.map.databinding.ActivityMainBinding
import com.example.map.monitoring.MainActivity
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class list : AppCompatActivity() {
    private val binding by lazy(LazyThreadSafetyMode.NONE) {
        ActivityMainBinding.inflate(layoutInflater)
    }

    private lateinit var databaseReference: DatabaseReference
    private lateinit var recyclerView: RecyclerView
    private lateinit var locationAdapter: LocationAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list)

        // Inisialisasi referensi Firebase Realtime Database
        databaseReference = FirebaseDatabase.getInstance().reference.child("DATA")

        // Inisialisasi RecyclerView
        recyclerView = findViewById(R.id.RecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Membuat opsi untuk FirebaseRecyclerAdapter
        val options = FirebaseRecyclerOptions.Builder<LocationData>()
            .setQuery(databaseReference, LocationData::class.java)
            .build()

        // Inisialisasi adapter dengan opsi
        locationAdapter = LocationAdapter(options)
        recyclerView.adapter = locationAdapter

        //tombol back
        val btnback = findViewById<Button>(R.id.btnback)
        btnback.setOnClickListener() {
            val intent = Intent (this, MainActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onStart() {
        super.onStart()
        locationAdapter.startListening()
    }

    override fun onStop() {
        super.onStop()
        locationAdapter.stopListening()
    }
}
