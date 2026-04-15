package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.myapplication.network.OtpRequest
import com.example.myapplication.network.RegisterRequest
import com.example.myapplication.network.RetrofitClient
import kotlinx.coroutines.launch

class SignupActivity : AppCompatActivity() {
    private lateinit var etFullName: EditText
    private lateinit var etEmail: EditText
    private lateinit var etPhone: EditText
    private lateinit var etYearJoined: EditText
    private lateinit var etBloodGroup: AutoCompleteTextView
    private lateinit var etPassword: EditText
    private lateinit var etOtp: EditText
    private lateinit var btnSignup: Button
    private lateinit var btnVerifyOtp: Button
    private lateinit var llOtpSection: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        etFullName = findViewById(R.id.etFullName)
        etEmail = findViewById(R.id.etEmail)
        etPhone = findViewById(R.id.etPhone)
        etYearJoined = findViewById(R.id.etYearJoined)
        etBloodGroup = findViewById(R.id.etBloodGroup)
        etPassword = findViewById(R.id.etPassword)

        val bloodGroups = arrayOf("A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-")
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, bloodGroups)
        etBloodGroup.setAdapter(adapter)

        etOtp = findViewById(R.id.etOtp)
        btnSignup = findViewById(R.id.btnSignup)
        btnVerifyOtp = findViewById(R.id.btnVerifyOtp)
        llOtpSection = findViewById(R.id.llOtpSection)
        val tvGoToLogin = findViewById<TextView>(R.id.tvGoToLogin)

        tvGoToLogin.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
            finish()
        }

        btnSignup.setOnClickListener {
            sendOtp()
        }

        btnVerifyOtp.setOnClickListener {
            registerUser()
        }
    }

    private fun sendOtp() {
        val email = etEmail.text.toString().trim()
        if (email.isEmpty()) {
            etEmail.error = "Email required"
            return
        }

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.instance.sendOtp(OtpRequest(email))
                if (response.isSuccessful && response.body()?.success == true) {
                    Toast.makeText(this@SignupActivity, "OTP sent to $email", Toast.LENGTH_SHORT).show()
                    llOtpSection.visibility = View.VISIBLE
                    btnSignup.visibility = View.GONE
                } else {
                    Toast.makeText(this@SignupActivity, response.body()?.message ?: "Failed to send OTP", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@SignupActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun registerUser() {
        val fullName = etFullName.text.toString().trim()
        val email = etEmail.text.toString().trim()
        val phone = etPhone.text.toString().trim()
        val year = etYearJoined.text.toString().trim()
        val blood = etBloodGroup.text.toString().trim()
        val password = etPassword.text.toString().trim()
        val otp = etOtp.text.toString().trim()

        if (otp.isEmpty()) {
            etOtp.error = "Enter OTP"
            return
        }

        val request = RegisterRequest(fullName, email, phone, year, blood, password, otp)

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.instance.register(request)
                if (response.isSuccessful && response.body()?.success == true) {
                    Toast.makeText(this@SignupActivity, "Registration successful!", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this@SignupActivity, LoginActivity::class.java)
                    intent.putExtra("SIGNUP_SUCCESS", true)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this@SignupActivity, response.body()?.message ?: "Registration failed", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@SignupActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
