package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.myapplication.network.OptRequest
import com.example.myapplication.network.RetrofitClient
import com.example.myapplication.network.SessionManager
import com.google.gson.Gson
import kotlinx.coroutines.launch
import com.example.myapplication.network.Event
import java.text.SimpleDateFormat
import java.util.*

class EventDetailsActivity : AppCompatActivity() {
    private lateinit var event: Event
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_event_details)

        sessionManager = SessionManager(this)
        val eventJson = intent.getStringExtra("EVENT_JSON")
        event = Gson().fromJson(eventJson, Event::class.java)

        val tvTitle = findViewById<TextView>(R.id.tvEventTitle)
        val tvDesc = findViewById<TextView>(R.id.tvEventDescription)
        val tvDate = findViewById<TextView>(R.id.tvEventDate)
        val tvTime = findViewById<TextView>(R.id.tvEventTime)
        val tvLocation = findViewById<TextView>(R.id.tvEventLocation)
        val btnOptIn = findViewById<Button>(R.id.btnOptIn)
        val btnOptOut = findViewById<Button>(R.id.btnOptOut)
        val btnAttendance = findViewById<Button>(R.id.btnMarkAttendance)
        val tvOptedCount = findViewById<TextView>(R.id.tvOptedCount)
        val tvStatus = findViewById<TextView>(R.id.tvEventStatus)

        tvTitle.text = event.title ?: "No Title"
        tvDesc.text = event.description ?: "No Description"
        tvDate.text = "Date: ${event.date ?: "N/A"}"
        tvTime.text = "Time: ${event.fromTime ?: ""} - ${event.toTime ?: ""}"
        tvLocation.text = "Location: ${event.location ?: "N/A"}"
        tvOptedCount.text = "Students Opted In: ${event.optedInStudents.size}"

        val userId = sessionManager.fetchUser()?.id ?: ""
        val isOptedIn = event.optedInStudents.any {
            when (it) {
                is String -> it == userId
                is Map<*, *> -> it["_id"] == userId || it["id"] == userId
                else -> it.toString() == userId
            }
        }
        
        val eventState = getEventState() // "UPCOMING", "ONGOING", "FINISHED"
        tvStatus.text = "Status: $eventState"

        // Visibility based on User Role (Admin vs Student)
        val domain = sessionManager.fetchUserEmail()?.substringAfter("@", "") ?: ""
        if (domain.contains("student.tce.edu")) {
            // ADMIN logic
            btnOptIn.visibility = View.GONE
            btnOptOut.visibility = View.GONE
            tvOptedCount.visibility = View.VISIBLE
            
            if (eventState == "UPCOMING") {
                btnAttendance.visibility = View.GONE
                Toast.makeText(this, "Attendance can be marked once event starts", Toast.LENGTH_SHORT).show()
            } else {
                btnAttendance.visibility = View.VISIBLE
            }
        } else {
            // STUDENT logic
            btnAttendance.visibility = View.GONE
            tvOptedCount.visibility = View.GONE
            
            if (eventState == "FINISHED") {
                btnOptIn.visibility = View.GONE
                btnOptOut.visibility = View.GONE
            } else {
                if (isOptedIn) {
                    btnOptIn.visibility = View.GONE
                    btnOptOut.visibility = View.VISIBLE
                } else {
                    btnOptIn.visibility = View.VISIBLE
                    btnOptOut.visibility = View.GONE
                }
            }
        }

        btnOptIn.setOnClickListener { handleOpt(true) }
        btnOptOut.setOnClickListener { handleOpt(false) }
        
        btnAttendance.setOnClickListener {
            val intent = Intent(this, AttendanceActivity::class.java)
            intent.putExtra("EVENT_ID", event._id ?: "")
            startActivity(intent)
        }
    }

    private fun getEventState(): String {
        try {
            val date = event.date ?: return "UNKNOWN"
            val fromTime = event.fromTime ?: return "UNKNOWN"
            val toTime = event.toTime ?: return "UNKNOWN"

            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
            val now = Calendar.getInstance().time
            val startTime = sdf.parse("$date $fromTime")
            val endTime = sdf.parse("$date $toTime")

            if (startTime == null || endTime == null) return "UNKNOWN"

            return when {
                now.before(startTime) -> "UPCOMING"
                now.after(endTime) -> "FINISHED"
                else -> "ONGOING"
            }
        } catch (e: Exception) {
            return "UNKNOWN"
        }
    }

    private fun handleOpt(isOptIn: Boolean) {
        val userId = sessionManager.fetchUser()?.id ?: return
        val eventId = event._id ?: return
        lifecycleScope.launch {
            try {
                val response = if (isOptIn) {
                    RetrofitClient.instance.optIn(eventId, OptRequest(userId))
                } else {
                    RetrofitClient.instance.optOut(eventId, OptRequest(userId))
                }
                if (response.isSuccessful) {
                    Toast.makeText(this@EventDetailsActivity, "Success", Toast.LENGTH_SHORT).show()
                    finish()
                }
            } catch (e: Exception) {
                Toast.makeText(this@EventDetailsActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
