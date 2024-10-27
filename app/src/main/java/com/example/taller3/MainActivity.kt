package com.example.taller3

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.ktx.Firebase
import com.google.firebase.auth.ktx.auth

class MainActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var loginEmail: TextInputEditText
    private lateinit var loginPassword: TextInputEditText
    private lateinit var loginButton: Button
    private lateinit var registerButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        auth = Firebase.auth
        loginEmail = findViewById(R.id.loginEmailEditText)
        loginPassword = findViewById(R.id.loginPasswordEditText)
        loginButton = findViewById(R.id.loginButton)
        registerButton = findViewById(R.id.registerButton)

        loginButton.setOnClickListener {
            val email = loginEmail.text.toString()
            val password = loginPassword.text.toString()
            signInUser(email, password)
        }

        registerButton.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
    }

    private fun validateForm(): Boolean {
        var valid = true
        val email = loginEmail.text.toString()
        if (TextUtils.isEmpty(email)) {
            loginEmail.error = "Requerido"
            valid = false
        } else {
            loginEmail.error = null
        }

        val password = loginPassword.text.toString()
        if (TextUtils.isEmpty(password)) {
            loginPassword.error = "Requerido"
            valid = false
        } else {
            loginPassword.error = null
        }
        return valid
    }

    private fun signInUser(email: String, password: String) {
        if (validateForm()) {
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this, "Inicio de sesión exitoso", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this, UserListActivity::class.java)
                        startActivity(intent)
                    } else {
                        Toast.makeText(this, "Error en el inicio de sesión", Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }
}
