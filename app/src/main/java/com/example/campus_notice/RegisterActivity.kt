// Campus Pulse — customized for Aman
package com.example.campus_notice

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.campus_notice.databinding.ActivityRegisterBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        val roles = arrayOf("Student", "Staff")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, roles)
        binding.spinnerRole.adapter = adapter

        binding.btnRegister.setOnClickListener {
            registerUser()
        }

        binding.tvLoginLink.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun registerUser() {
        if (!Utils.isNetworkAvailable(this)) {
            Toast.makeText(this, "Please connect to the internet", Toast.LENGTH_SHORT).show()
            return
        }

        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()
        val role = binding.spinnerRole.selectedItem.toString()

        binding.tilEmail.error = null
        binding.tilPassword.error = null

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tilEmail.error = getString(R.string.invalid_email)
            return
        }

        if (password.length < 6) {
            binding.tilPassword.error = getString(R.string.password_too_short)
            return
        }

        setLoading(true)
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val uid = auth.currentUser?.uid
                    if (uid != null) {
                        val user = User(email = email, role = role)
                        val databaseRef = FirebaseDatabase.getInstance().getReference("users")
                        databaseRef.child(uid).setValue(user).addOnCompleteListener { dbTask ->
                            if (dbTask.isSuccessful) {
                                auth.signOut()
                                Toast.makeText(this, "Account Created Successfully", Toast.LENGTH_SHORT).show()
                                startActivity(Intent(this, LoginActivity::class.java))
                                finish()
                            } else {
                                setLoading(false)
                                Toast.makeText(this, "Failed to save user info.", Toast.LENGTH_SHORT).show()
                            }
                        }
                    } else {
                        setLoading(false)
                    }
                } else {
                    setLoading(false)
                    android.util.Log.e("FirebaseAuth", "Registration Failed", task.exception)
                    Toast.makeText(this, "Registration Failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                }
            }
    }

    private fun setLoading(isLoading: Boolean) {
        binding.btnRegister.isEnabled = !isLoading
        binding.btnRegister.text = getString(if (isLoading) R.string.register_loading else R.string.register)
        binding.etEmail.isEnabled = !isLoading
        binding.etPassword.isEnabled = !isLoading
        binding.spinnerRole.isEnabled = !isLoading
    }
}
