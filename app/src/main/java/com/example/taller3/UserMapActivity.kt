package com.example.taller3

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase

class UserMapActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var auth: FirebaseAuth
    private var userLatitude: Double = 0.0
    private var userLongitude: Double = 0.0
    private var availableUserLocation: LatLng? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_map)

        auth = Firebase.auth
        userLatitude = intent.getDoubleExtra("USER_LATITUDE", 0.0)
        userLongitude = intent.getDoubleExtra("USER_LONGITUDE", 0.0)

        // Inicializar el fragmento del mapa
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        updateUserMarkers()
    }

    private fun updateUserMarkers() {
        // Mostrar el marcador azul de la ubicación del usuario autenticado
        val userLocation = LatLng(userLatitude, userLongitude)
        mMap.addMarker(
            MarkerOptions().position(userLocation).title("Tú")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
        )
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 10f))

        // Obtener y mostrar la ubicación del usuario "Disponible" de Firebase
        val databaseRef = FirebaseDatabase.getInstance().getReference("users")
        databaseRef.child("disponible").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val lat = snapshot.child("latitud").getValue(Double::class.java) ?: 0.0
                val lng = snapshot.child("longitud").getValue(Double::class.java) ?: 0.0
                availableUserLocation = LatLng(lat, lng)

                // Remover el marcador verde previo si existe y agregar uno nuevo
                mMap.clear()
                mMap.addMarker(
                    MarkerOptions().position(userLocation).title("Tú")
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
                )
                mMap.addMarker(
                    MarkerOptions().position(availableUserLocation!!).title("Usuario Disponible")
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                )
            }

            override fun onCancelled(error: DatabaseError) {
                // Manejar el error
            }
        })
    }
}

