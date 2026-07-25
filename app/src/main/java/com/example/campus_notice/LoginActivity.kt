// Campus Pulse — customized for Aman
package com.example.campus_notice

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.campus_notice.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Check if user is already logged in
        val sharedPrefs = getSharedPreferences("NoticeBoardPrefs", Context.MODE_PRIVATE)
        val savedEmail = sharedPrefs.getString("email", null)
        val savedRole = sharedPrefs.getString("role", null)
        if (savedEmail != null && savedRole != null) {
            routeUser(savedRole)
            return
        }

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        binding.btnLogin.setOnClickListener {
            loginUser()
        }

        binding.tvRegisterLink.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun loginUser() {
        if (!Utils.isNetworkAvailable(this)) {
            Toast.makeText(this, "Please connect to the internet", Toast.LENGTH_SHORT).show()
            return
        }

        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()

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
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val uid = auth.currentUser?.uid
                    if (uid != null) {
                        val databaseRef = FirebaseDatabase.getInstance().getReference("users").child(uid)
                        databaseRef.addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                val user = snapshot.getValue(User::class.java)
                                val role = user?.role ?: "Student" // default to Student if failed
                                
                                val sharedPrefs = getSharedPreferences("NoticeBoardPrefs", Context.MODE_PRIVATE)
                                val editor = sharedPrefs.edit()
                                editor.putString("email", email)
                                editor.putString("role", role)
                                editor.apply()
                                
                                routeUser(role)
                            }

                            override fun onCancelled(error: DatabaseError) {
                                setLoading(false)
                                Toast.makeText(this@LoginActivity, "Failed to read user data", Toast.LENGTH_SHORT).show()
                            }
                        })
                    } else {
                        setLoading(false)
                    }
                } else {
                    setLoading(false)
                    android.util.Log.e("FirebaseAuth", "Login Failed", task.exception)
                    Toast.makeText(this, "Login Failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                }
            }
    }

    private fun setLoading(isLoading: Boolean) {
        binding.btnLogin.isEnabled = !isLoading
        binding.btnLogin.text = getString(if (isLoading) R.string.login_loading else R.string.login)
        binding.etEmail.isEnabled = !isLoading
        binding.etPassword.isEnabled = !isLoading
    }

    private fun routeUser(role: String) {
        if (role == "Staff") {
            startActivity(Intent(this, StaffDashboardActivity::class.java))
        } else {
            startActivity(Intent(this, StudentDashboardActivity::class.java))
        }
        finish()
    }
}
