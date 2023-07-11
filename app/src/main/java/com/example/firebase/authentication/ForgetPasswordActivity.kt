package com.example.firebase.authentication

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.firebase.R
import com.example.firebase.databinding.ActivityForgetPasswordBinding
import com.google.firebase.auth.FirebaseAuth

class ForgetPasswordActivity : AppCompatActivity() {

    private lateinit var binding: ActivityForgetPasswordBinding

    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityForgetPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.title = "Log In"
        supportActionBar?.subtitle = "Firebase"

        binding.btnResetPass.setOnClickListener {
            val userEmail = binding.etEmail.text.toString()
            resetPassword(userEmail)
        }
    }

    private fun resetPassword(userEmail: String) {
        auth.sendPasswordResetEmail(userEmail).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(applicationContext, "We sent a password reset mail to your email address...", Toast.LENGTH_LONG).show()
                finish()
            } else {
                Toast.makeText(applicationContext, task.exception.toString(), Toast.LENGTH_LONG).show()
            }
        }
    }
}