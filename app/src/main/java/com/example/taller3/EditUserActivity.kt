package com.example.taller3

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage

class EditUserActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var editEmail: TextInputEditText
    private lateinit var editName: TextInputEditText
    private lateinit var editLastName: TextInputEditText
    private lateinit var editIdentification: TextInputEditText
    private lateinit var editLatitude: TextInputEditText
    private lateinit var editLongitude: TextInputEditText
    private lateinit var selectImageButton: Button
    private lateinit var saveButton: Button

    private var imageUri: Uri? = null
    private val PICK_IMAGE_REQUEST = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_user)

        auth = FirebaseAuth.getInstance()
        editEmail = findViewById(R.id.editEmailEditText)
        editName = findViewById(R.id.editNameEditText)
        editLastName = findViewById(R.id.editLastNameEditText)
        editIdentification = findViewById(R.id.editIdentificationEditText)
        editLatitude = findViewById(R.id.editLatitudeEditText)
        editLongitude = findViewById(R.id.editLongitudeEditText)
        selectImageButton = findViewById(R.id.selectImageButton)
        saveButton = findViewById(R.id.saveButton)

        loadUserData()

        selectImageButton.setOnClickListener {
            openFileChooser()
        }

        saveButton.setOnClickListener {
            saveUserData()
        }
    }

    private fun loadUserData() {
        val userId = auth.currentUser?.uid ?: return
        val databaseRef = FirebaseDatabase.getInstance().getReference("users").child(userId)

        databaseRef.get().addOnSuccessListener { snapshot ->
            editEmail.setText(snapshot.child("correo").getValue(String::class.java))
            editName.setText(snapshot.child("nombre").getValue(String::class.java))
            editLastName.setText(snapshot.child("apellido").getValue(String::class.java))
            editIdentification.setText(snapshot.child("identificación").getValue(String::class.java))
            editLatitude.setText(snapshot.child("latitud").getValue(String::class.java))
            editLongitude.setText(snapshot.child("longitud").getValue(String::class.java))
        }
    }

    private fun openFileChooser() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.data != null) {
            imageUri = data.data
            Toast.makeText(this, "Imagen seleccionada", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveUserData() {
        val userId = auth.currentUser?.uid ?: return
        val databaseRef = FirebaseDatabase.getInstance().getReference("users").child(userId)

        val userUpdates = hashMapOf<String, Any>(
            "correo" to editEmail.text.toString(),
            "nombre" to editName.text.toString(),
            "apellido" to editLastName.text.toString(),
            "identificación" to editIdentification.text.toString(),
            "latitud" to editLatitude.text.toString(),
            "longitud" to editLongitude.text.toString()
        )

        databaseRef.updateChildren(userUpdates).addOnSuccessListener {
            Toast.makeText(this, "Información actualizada", Toast.LENGTH_SHORT).show()
            if (imageUri != null) {
                uploadImageToStorage(userId)
            } else {
                finish()
            }
        }.addOnFailureListener {
            Toast.makeText(this, "Error al actualizar la información", Toast.LENGTH_SHORT).show()
        }
    }

    private fun uploadImageToStorage(userId: String) {
        imageUri?.let {
            val storageRef = FirebaseStorage.getInstance().getReference("profile_images/$userId.jpg")
            storageRef.putFile(it)
                .addOnSuccessListener {
                    Toast.makeText(this, "Imagen subida exitosamente", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Error al subir la imagen", Toast.LENGTH_SHORT).show()
                }
        }
    }
}
