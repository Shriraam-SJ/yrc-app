package com.example.myapplication

import android.Manifest
import android.app.AlarmManager
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.myapplication.network.LoginRequest
import com.example.myapplication.network.RetrofitClient
import com.example.myapplication.network.SessionManager
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var sessionManager: SessionManager

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { _ ->
        // Permissions handled
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        sessionManager = SessionManager(this)
        
        // Check session
        if (sessionManager.fetchAuthToken() != null) {
            val email = sessionManager.fetchUserEmail() ?: ""
            navigateBasedOnEmail(email)
            return
        }

        setContentView(R.layout.activity_login)

        checkAndRequestPermissions()

        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val tvGoToSignup = findViewById<TextView>(R.id.tvGoToSignup)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val signupSuccessPopup = findViewById<LinearLayout>(R.id.signupSuccessPopup)
        val btnClosePopup = findViewById<Button>(R.id.btnClosePopup)

        tvGoToSignup.setOnClickListener {
            val intent = Intent(this, SignupActivity::class.java)
            startActivity(intent)
        }

        btnLogin.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            loginUser(email, password)
        }

        if (intent.getBooleanExtra("SIGNUP_SUCCESS", false)) {
            signupSuccessPopup.visibility = View.VISIBLE
            Handler(Looper.getMainLooper()).postDelayed({
                signupSuccessPopup.visibility = View.GONE
            }, 60000)
        }

        btnClosePopup.setOnClickListener {
            signupSuccessPopup.visibility = View.GONE
        }
    }

    private fun loginUser(email: String, password: String) {
        val progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Logging in...")
        progressDialog.setCancelable(false)
        progressDialog.show()

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.instance.login(LoginRequest(email, password))
                progressDialog.dismiss()

                if (response.isSuccessful && response.body() != null) {
                    val loginResponse = response.body()!!
                    sessionManager.saveAuthToken(loginResponse.token)
                    sessionManager.saveUser(loginResponse.user)
                    sessionManager.saveUserEmail(email) // Explicitly save email
                    
                    Toast.makeText(this@LoginActivity, "Login Successful", Toast.LENGTH_SHORT).show()
                    navigateBasedOnEmail(email)
                } else {
                    Toast.makeText(this@LoginActivity, "Invalid credentials", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                progressDialog.dismiss()
                Toast.makeText(this@LoginActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun navigateBasedOnEmail(email: String) {
        val domain = email.substringAfter("@", "")
        val intent = if (domain.contains("student.tce.edu")) {
            Intent(this, AdminDashboardActivity::class.java)
        } else {
            Intent(this, StudentDashboardActivity::class.java)
        }
        startActivity(intent)
        finish()
    }

    private fun checkAndRequestPermissions() {
        val permissions = mutableListOf<String>()
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.SEND_SMS)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
        if (permissions.isNotEmpty()) {
            requestPermissionLauncher.launch(permissions.toTypedArray())
        }
        checkExactAlarmPermission()
    }

    private fun checkExactAlarmPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
            if (!alarmManager.canScheduleExactAlarms()) {
                val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                    data = Uri.parse("package:$packageName")
                }
                startActivity(intent)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        val signupSuccessPopup = findViewById<LinearLayout>(R.id.signupSuccessPopup)
        if (intent.getBooleanExtra("SIGNUP_SUCCESS", false)) {
            signupSuccessPopup?.visibility = View.VISIBLE
            Handler(Looper.getMainLooper()).postDelayed({
                signupSuccessPopup?.visibility = View.GONE
            }, 60000)
        }
    }
}
