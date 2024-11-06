package com.example.taller3

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.getValue
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
    private lateinit var profileImageView: ImageView

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
        profileImageView = findViewById(R.id.profileImageView)

        loadUserData()

        selectImageButton.setOnClickListener {
            openFileChooser()
        }

        saveButton.setOnClickListener {
            if (validateFields()) {
                saveUserData()
            }
        }
    }

    private fun loadUserData() {
        val userId = auth.currentUser?.uid ?: return
        val databaseRef = FirebaseDatabase.getInstance().getReference("users")

        databaseRef.child(userId).get().addOnSuccessListener { snapshot ->
            Log.d("EditListActivity", "DataSnapshot recibido: ${snapshot.getValue()} users")
            editEmail.setText(snapshot.child("correo").getValue(String::class.java))
            editName.setText(snapshot.child("nombre").getValue(String::class.java))
            editLastName.setText(snapshot.child("apellido").getValue(String::class.java))
            editIdentification.setText(snapshot.child("identificación").getValue(String::class.java))
            editLatitude.setText(snapshot.child("latitud").getValue(String::class.java))
            editLongitude.setText(snapshot.child("longitud").getValue(String::class.java))
        }

        val storageRef = FirebaseStorage.getInstance().getReference("profile_images/$userId.jpg")
        storageRef.downloadUrl.addOnSuccessListener { uri ->
            Glide.with(this)
                .load(uri)
                .into(profileImageView)
        }.addOnFailureListener {
            Toast.makeText(this, "Error al cargar la imagen de perfil", Toast.LENGTH_SHORT).show()
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
            profileImageView.setImageURI(imageUri)
        }
    }

    private fun validateFields(): Boolean {
        if (editEmail.text.isNullOrEmpty() || editName.text.isNullOrEmpty() || editLastName.text.isNullOrEmpty() ||
            editIdentification.text.isNullOrEmpty() || editLatitude.text.isNullOrEmpty() || editLongitude.text.isNullOrEmpty()
        ) {
            Toast.makeText(this, "Por favor, completa todos los campos.", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    private fun saveUserData() {
        val userId = auth.currentUser?.uid ?: return
        val databaseRef = FirebaseDatabase.getInstance().getReference("users").child(userId)

        val userUpdates = mutableMapOf<String, Any>()

        userUpdates["correo"] = editEmail.text.toString()
        userUpdates["nombre"] = editName.text.toString()
        userUpdates["apellido"] = editLastName.text.toString()
        userUpdates["identificación"] = editIdentification.text.toString()
        userUpdates["latitud"] = editLatitude.text.toString()
        userUpdates["longitud"] = editLongitude.text.toString()

        databaseRef.updateChildren(userUpdates).addOnSuccessListener {
            Toast.makeText(this, "Información actualizada", Toast.LENGTH_SHORT).show()
            if (imageUri != null) {
                uploadImageToStorage(userId) // Solo sube la imagen si se seleccionó una nueva
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
