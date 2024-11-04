package com.example.taller3

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase

class UserListActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var userAdapter: UserAdapter
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.user_list_activity)

        // Configurar el RecyclerView
        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        loadUsers()
        auth = Firebase.auth
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.user_list_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_logout -> {
                Toast.makeText(this, "Cerrando sesión...", Toast.LENGTH_SHORT).show()
                auth.signOut()
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                true
            }
            R.id.action_edit_user -> {
                val intent = Intent(this, EditUserActivity::class.java)
                startActivity(intent)
                true
            }
            R.id.action_toggle_status -> {
                toggleUserStatus()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun toggleUserStatus() {
        val userId = auth.currentUser?.uid ?: return
        val databaseRef = FirebaseDatabase.getInstance().getReference("users").child(userId)

        databaseRef.child("estado").get().addOnSuccessListener { snapshot ->
            val currentStatus = snapshot.getValue(String::class.java) ?: "No disponible"

            // Alternar el estado
            val newStatus = if (currentStatus == "Disponible") "No disponible" else "Disponible"

            // Actualizar el nuevo estado en la base de datos
            databaseRef.child("estado").setValue(newStatus)
                .addOnSuccessListener {
                    Toast.makeText(this, "Estado cambiado a $newStatus", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Error al cambiar el estado", Toast.LENGTH_SHORT).show()
                }
        }.addOnFailureListener {
            Toast.makeText(this, "Error al obtener el estado actual", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadUsers() {
        val databaseRef = FirebaseDatabase.getInstance().getReference("users")
        val availableUsers = mutableListOf<User>()

        val userListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                availableUsers.clear()
                Log.d("UserListActivity", "DataSnapshot received: ${dataSnapshot.childrenCount} users")

                for (userSnapshot in dataSnapshot.children) {
                    val user = userSnapshot.getValue(User::class.java)

                    if (user != null) {
                        Log.d("UserListActivity", "User found: ${user.nombre} with status ${user.estado}")

                        if (user.estado == "Disponible") {
                            availableUsers.add(user)
                        }
                    } else {
                        Log.d("UserListActivity", "User data is null")
                    }
                }

                userAdapter = UserAdapter(availableUsers)
                recyclerView.adapter = userAdapter
                Log.d("UserListActivity", "RecyclerView updated with ${availableUsers.size} users")
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.w("UserListActivity", "loadUsers:onCancelled", databaseError.toException())
                Toast.makeText(this@UserListActivity, "Error al cargar los usuarios", Toast.LENGTH_SHORT).show()
            }
        }

        databaseRef.addValueEventListener(userListener)
    }
}
