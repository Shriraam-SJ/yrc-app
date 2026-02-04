package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val etEmail = findViewById<EditText>(R.id.etEmail)
        val tvGoToSignup = findViewById<TextView>(R.id.tvGoToSignup)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val signupSuccessPopup = findViewById<LinearLayout>(R.id.signupSuccessPopup)
        val btnClosePopup = findViewById<Button>(R.id.btnClosePopup)

        tvGoToSignup.setOnClickListener {
            val intent = Intent(this, SignupActivity::class.java)
            startActivity(intent)
        }

        btnLogin.setOnClickListener {
            val email = etEmail.text.toString()
            val domain = email.substringAfter("@", "")

            val intent = when {
                domain.contains("student.tce.edu") -> Intent(this, StudentDashboardActivity::class.java)
                domain.contains("tce.edu") -> Intent(this, AdminDashboardActivity::class.java)
                else -> Intent(this, MainActivity::class.java)
            }
            startActivity(intent)
            finish()
        }

        if (intent.getBooleanExtra("SIGNUP_SUCCESS", false)) {
            signupSuccessPopup.visibility = View.VISIBLE
            Handler(Looper.getMainLooper()).postDelayed({
                signupSuccessPopup.visibility = View.GONE
            }, 60000) // 1 minute
        }

        btnClosePopup.setOnClickListener {
            signupSuccessPopup.visibility = View.GONE
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        val signupSuccessPopup = findViewById<LinearLayout>(R.id.signupSuccessPopup)
        if (intent.getBooleanExtra("SIGNUP_SUCCESS", false)) {
            signupSuccessPopup.visibility = View.VISIBLE
            Handler(Looper.getMainLooper()).postDelayed({
                signupSuccessPopup.visibility = View.GONE
            }, 60000)
        }
    }
}