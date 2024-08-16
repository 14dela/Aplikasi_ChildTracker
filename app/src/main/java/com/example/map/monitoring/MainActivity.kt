package com.example.map.monitoring

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.map.R
import com.example.map.databinding.ActivityMainBinding
import com.example.map.list
import com.example.project1.Notification.NotificationSetup
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlin.math.log

class MainActivity : AppCompatActivity(), OnMapReadyCallback {
    private val binding by lazy(LazyThreadSafetyMode.NONE) {
        ActivityMainBinding.inflate(layoutInflater)
    }
    private lateinit var googleMap: GoogleMap
    private lateinit var databaseReference: DatabaseReference
    private val database: FirebaseDatabase by lazy { FirebaseDatabase.getInstance() }
    private var markerGPS1: Marker? = null
    private var markerGPS2: Marker? = null
    private var markerSchool: Marker? = null
    private lateinit var geofencingClient: GeofencingClient
    private val GEOFENCE_RADIUS = 100f // radius geofence dalam meter
    private val GEOFENCE_ID = "School_Geofence" // ID unik untuk geofence
    private var geofencePendingIntent: PendingIntent? = null
    private var centerLocation: LatLng?=null
    private val notificationSetup by lazy { NotificationSetup(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        databaseReference = FirebaseDatabase.getInstance().reference
        geofencingClient = LocationServices.getGeofencingClient(this)

        // tombol lacak
        val btnlacak = binding.buttonlacak
        btnlacak.setOnClickListener {
            getLocationFromGPS1()
            getLocationFromGPS2()
        }

        // tombol list
        val btnlist = binding.recyclerviewbtn
        btnlist.setOnClickListener {
            val intent = Intent(this, list::class.java)
            startActivity(intent)
        }

        // Inisialisasi Map Fragment
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)


    }

    override fun onMapReady(gMap: GoogleMap) {
        googleMap = gMap
        googleMap.mapType = GoogleMap.MAP_TYPE_NORMAL
        getLocationFromMain()

        // Buat dan tambahkan geofence saat peta siap
        createGeofence()
    }

    private fun getLocationFromMain() {
        databaseReference.child("Main").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val latitude = dataSnapshot.child("mainlat").value as Double
                val longitude = dataSnapshot.child("mainlong").value as Double

                centerLocation = LatLng(latitude,longitude) //Simpan lokasi Utama

                val location = LatLng(latitude, longitude)
                if (markerSchool == null) {
                    val markerOptions = MarkerOptions()
                        .position(location)
                        .title("Lokasi Sekolah")
                        .snippet("Latitude: $latitude, longitude: $longitude")
                    markerSchool = googleMap.addMarker(markerOptions)
                } else {
                    markerSchool?.position = location
                }
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 15f))


            }

            override fun onCancelled(databaseError: DatabaseError) {
                println("Database error: ${databaseError.message}")
            }
        })
    }

    private fun getLocationFromGPS1() {
        val locationRef = database.getReference("DATA/GPS1")
        locationRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                try {


                    val latitude = dataSnapshot.child("latitude").getValue().toString().toDouble()
                    val longitude = dataSnapshot.child("longitude").getValue().toString().toDouble()

                    val location = LatLng(latitude, longitude)
                    if (markerGPS1 == null) {
                        val markerOptions = MarkerOptions()
                            .position(location)
                            .title("Gelang 1")
                            .snippet("Latitude: $latitude, longitude: $longitude")
                        markerGPS1 = googleMap.addMarker(markerOptions)
                    } else {
                        markerGPS1?.position = location
                    }
                    adjustCamera()
                    updateLocation1(latitude,longitude)


                }catch (e: NumberFormatException) {
                    Log.e("Error", "Invalid range value: ${e.message}")
                }


            }

            override fun onCancelled(databaseError: DatabaseError) {
                println("Database error: ${databaseError.message}")
                Log.e("Database error:", "${databaseError.message}")
            }
        })
    }

    private fun getLocationFromGPS2() {
        database.getReference("DATA/GPS2").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val latitude = dataSnapshot.child("latitude").value as Double
                val longitude = dataSnapshot.child("longitude").value as Double

                val location = LatLng(latitude, longitude)
                if (markerGPS2 == null) {
                    val markerOptions = MarkerOptions()
                        .position(location)
                        .title("Gelang 2")
                        .snippet("Latitude: $latitude, longitude: $longitude")
                    markerGPS2 = googleMap.addMarker(markerOptions)
                } else {
                    markerGPS2?.position = location
                }
                adjustCamera()
                updateLocation2(latitude,longitude)

            }

            override fun onCancelled(databaseError: DatabaseError) {
                println("Database error: ${databaseError.message}")
            }
        })
    }

    private fun adjustCamera() {
        val builder = LatLngBounds.Builder()
        markerGPS1?.position?.let { builder.include(it) }
        markerGPS2?.position?.let { builder.include(it) }
        val bounds = builder.build()
        val padding = 100 // padding in pixels
        val cu = CameraUpdateFactory.newLatLngBounds(bounds, padding)
        googleMap.moveCamera(cu)
    }

    private fun createGeofence() {
        // Mendapatkan koordinat sekolah dari Firebase
        databaseReference.child("Main").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val latitude = dataSnapshot.child("mainlat").value as? Double
                val longitude = dataSnapshot.child("mainlong").value as? Double

                if (latitude != null && longitude != null) {
                    val schoolLocation = LatLng(latitude, longitude)

                    // Menambahkan lingkaran (geofence) ke peta
                    val circleOptions = CircleOptions()
                        .center(schoolLocation)
                        .radius(20.0) // Jarak 10 meter (sesuaikan dengan kebutuhan)
                        .strokeWidth(2f)
                        .strokeColor(Color.RED)
                        .fillColor(Color.argb(70, 255, 0, 0))
                    googleMap.addCircle(circleOptions)

                    val geofence = Geofence.Builder()
                        .setRequestId(GEOFENCE_ID)
                        .setCircularRegion(schoolLocation.latitude, schoolLocation.longitude, GEOFENCE_RADIUS)
                        .setExpirationDuration(Geofence.NEVER_EXPIRE)
                        .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER or Geofence.GEOFENCE_TRANSITION_EXIT)
                        .build()

                    val geofencingRequest = GeofencingRequest.Builder()
                        .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
                        .addGeofence(geofence)
                        .build()

                    val intent = Intent(this@MainActivity, GeofenceBroadcastReceiver::class.java)
                    geofencePendingIntent = PendingIntent.getBroadcast(
                        this@MainActivity,
                        0,
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE // Tambahkan flag FLAG_IMMUTABLE atau FLAG_MUTABLE sesuai kebutuhan
                    )

                    if (geofencePendingIntent != null && ActivityCompat.checkSelfPermission(
                            this@MainActivity,
                            Manifest.permission.ACCESS_FINE_LOCATION
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {
                        // Menambahkan geofence jika PendingIntent tidak null dan izin sudah diberikan
                        geofencingClient.addGeofences(geofencingRequest, geofencePendingIntent!!)?.run {
                            addOnSuccessListener {
                                // Handle successful addition of geofence
                                println("Geofence added successfully")
                            }
                            addOnFailureListener {
                                // Handle failure to add geofence
                                println("Failed to add geofence: ${it.message}")
                            }
                        }
                    } else {
                        // Handle case where PendingIntent is null or permission is not granted
                        println("PendingIntent is null or permission is not granted")
                        REQUEST_LOCATION_PERMISSION
                    }
                } else {
                    println("Latitude or longitude is null")
                }
            }
            override fun onCancelled(databaseError: DatabaseError) {
                println("Database error: ${databaseError.message}")
            }
        })
    }
    private fun updateLocation1(lat:Double,lng:Double){
        val location = LatLng(lat, lng)
        // Remove the previous marker if it exists
        markerGPS1?.remove()

        markerGPS1= googleMap.addMarker(MarkerOptions().position(location)
            .title(getString(R.string.now_location1))
            .snippet(location.toString()))
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location,17f))
                try {
                    centerLocation?.let{


                        // Menghitung jarak
                        val distance = FloatArray(1)
                        Location.distanceBetween(lat, lng, it.latitude, it.longitude, distance)
                        if (distance[0] > 15) {
                            notificationSetup.sendRangeNotification1()
                            Toast.makeText(this@MainActivity, getString(R.string.keluargps1), Toast.LENGTH_LONG).show()

                        }

                    }
                }catch (e:Exception){
                    Log.e("lat","$lat")
                    Log.e("lng","$lng")
                }
    }

    private fun updateLocation2(lat:Double,lng:Double) {
        val location = LatLng(lat, lng)
        // Remove the previous marker if it exists
        markerGPS2?.remove()

        markerGPS2 = googleMap.addMarker(
            MarkerOptions().position(location)
                .title(getString(R.string.now_location2))
                .snippet(location.toString())
        )
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 17f))

            try {
                centerLocation?.let {
                    // Menghitung jarak
                    val distance = FloatArray(1)
                    Location.distanceBetween(lat, lng, it.latitude, it.longitude, distance)
                    if (distance[0] > 15) {
                        notificationSetup.sendRangeNotification2()
                        Toast.makeText(this@MainActivity, getString(R.string.keluargps2), Toast.LENGTH_LONG).show()
                    }
                }
            }catch (e: NumberFormatException) {
                Log.e("Error", "Invalid range value: ${e.message}")
            }

    }


    companion object {
        private const val REQUEST_LOCATION_PERMISSION = 1
    }
}
