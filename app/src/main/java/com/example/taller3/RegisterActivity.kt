package com.example.taller3


import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage

class RegisterActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var registerEmail: TextInputEditText
    private lateinit var registerPassword: TextInputEditText
    private lateinit var registerName: TextInputEditText
    private lateinit var registerLastName: TextInputEditText
    private lateinit var registerIdentification: TextInputEditText
    private lateinit var registerLatitude: TextInputEditText
    private lateinit var registerLongitude: TextInputEditText
    private lateinit var selectImageButton: Button
    private lateinit var registerButton: Button

    private var imageUri: Uri? = null
    private val PICK_IMAGE_REQUEST = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.register_activity)

        auth = FirebaseAuth.getInstance()
        registerEmail = findViewById(R.id.registerEmailEditText)
        registerPassword = findViewById(R.id.registerPasswordEditText)
        registerName = findViewById(R.id.registerNameEditText)
        registerLastName = findViewById(R.id.registerLastNameEditText)
        registerIdentification = findViewById(R.id.registerIdentificationEditText)
        registerLatitude = findViewById(R.id.registerLatitudeEditText)
        registerLongitude = findViewById(R.id.registerLongitudeEditText)
        selectImageButton = findViewById(R.id.selectImageButton)
        registerButton = findViewById(R.id.registerButton)

        selectImageButton.setOnClickListener {
            openFileChooser()
        }

        registerButton.setOnClickListener {
            val email = registerEmail.text.toString()
            val password = registerPassword.text.toString()
            val name = registerName.text.toString()
            val lastName = registerLastName.text.toString()
            val identification = registerIdentification.text.toString()
            val latitude = registerLatitude.text.toString()
            val longitude = registerLongitude.text.toString()

            if (validateForm(email, password, name, lastName, identification, latitude, longitude)) {
                registerUser(email, password, name, lastName, identification, latitude, longitude)
            }
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

    private fun validateForm(email: String, password: String, name: String, lastName: String, identification: String, latitude: String, longitude: String): Boolean {
        var valid = true

        if (TextUtils.isEmpty(email)) {
            registerEmail.error = "Requerido"
            valid = false
        } else {
            registerEmail.error = null
        }

        if (TextUtils.isEmpty(password)) {
            registerPassword.error = "Requerido"
            valid = false
        } else {
            registerPassword.error = null
        }

        if (TextUtils.isEmpty(name)) {
            registerName.error = "Requerido"
            valid = false
        } else {
            registerName.error = null
        }

        if (TextUtils.isEmpty(lastName)) {
            registerLastName.error = "Requerido"
            valid = false
        } else {
            registerLastName.error = null
        }

        if (TextUtils.isEmpty(identification)) {
            registerIdentification.error = "Requerido"
            valid = false
        } else {
            registerIdentification.error = null
        }

        if (TextUtils.isEmpty(latitude) || !latitude.matches(Regex("-?\\d+(\\.\\d+)?"))) {
            registerLatitude.error = "Latitud inválida"
            valid = false
        } else {
            registerLatitude.error = null
        }

        if (TextUtils.isEmpty(longitude) || !longitude.matches(Regex("-?\\d+(\\.\\d+)?"))) {
            registerLongitude.error = "Longitud inválida"
            valid = false
        } else {
            registerLongitude.error = null
        }

        return valid
    }

    private fun registerUser(email: String, password: String, name: String, lastName: String, identification: String, latitude: String, longitude: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val userId = auth.currentUser?.uid
                    userId?.let {
                        saveUserInfo(it, email, name, lastName, identification, latitude, longitude)
                        uploadImageToStorage(it)
                    }
                    Toast.makeText(this, "Registro exitoso", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this, "Error en el registro", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun saveUserInfo(userId: String, email: String, name: String, lastName: String, identification: String, latitude: String, longitude: String) {
        val database = FirebaseDatabase.getInstance().reference

        val user = HashMap<String, Any>()
        user["nombre"] = name
        user["apellido"] = lastName
        user["correo"] = email
        user["identificación"] = identification
        user["latitud"] = latitude
        user["longitud"] = longitude
        user["estado"] = "Disponible"

        database.child("users").child(userId).setValue(user)
            .addOnSuccessListener {
                Toast.makeText(this, "Información guardada", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al guardar la información", Toast.LENGTH_SHORT).show()
            }
    }

    private fun uploadImageToStorage(userId: String) {
        imageUri?.let {
            val storageRef = FirebaseStorage.getInstance().getReference("profile_images/$userId.jpg")
            storageRef.putFile(it)
                .addOnSuccessListener {
                    Toast.makeText(this, "Imagen subida exitosamente", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Error al subir la imagen", Toast.LENGTH_SHORT).show()
                }
        }
    }
}
