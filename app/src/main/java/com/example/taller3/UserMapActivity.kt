package com.example.taller3

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class UserMapActivity : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var mMap: GoogleMap
    private lateinit var auth: FirebaseAuth
    private var userMarker: Marker? = null
    private var availableUserMarker: Marker? = null
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_map)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        setupAuthenticatedUserMarker()
        setupAvailableUserMarker()
    }

    private fun setupAuthenticatedUserMarker() {
        val userId = auth.currentUser?.uid ?: return

        // Escuchar cambios de ubicación del usuario autenticado
        val userLocationRef = database.child("users").child(userId).child("location")
        userLocationRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val latitude = snapshot.child("lat").getValue(Double::class.java) ?: 0.0
                val longitude = snapshot.child("lng").getValue(Double::class.java) ?: 0.0
                val userLocation = LatLng(latitude, longitude)

                // Actualizar o crear el marcador del usuario autenticado
                if (userMarker == null) {
                    userMarker = mMap.addMarker(
                        MarkerOptions()
                            .position(userLocation)
                            .title("Tú")
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
                    )
                } else {
                    userMarker?.position = userLocation
                }
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 12f))
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun setupAvailableUserMarker() {
        // Escuchar cambios de ubicación de otros usuarios disponibles
        val availableUsersRef = database.child("users")
        availableUsersRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (userSnapshot in snapshot.children) {
                    val userId = userSnapshot.key ?: continue
                    if (userId == auth.currentUser?.uid) continue // Ignorar usuario autenticado

                    val estado = userSnapshot.child("estado").getValue(String::class.java)
                    if (estado == "Disponible") {
                        val latitude = userSnapshot.child("location/lat").getValue(Double::class.java) ?: continue
                        val longitude = userSnapshot.child("location/lng").getValue(Double::class.java) ?: continue
                        val availableUserLocation = LatLng(latitude, longitude)

                        // Actualizar o crear el marcador del usuario disponible
                        if (availableUserMarker == null) {
                            availableUserMarker = mMap.addMarker(
                                MarkerOptions()
                                    .position(availableUserLocation)
                                    .title("Usuario Disponible")
                                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                            )
                        } else {
                            availableUserMarker?.position = availableUserLocation
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }
}

