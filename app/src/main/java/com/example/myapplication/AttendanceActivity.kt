package com.example.myapplication

import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.myapplication.network.AttendanceRequest
import com.example.myapplication.network.RetrofitClient
import com.example.myapplication.network.User
import kotlinx.coroutines.launch

class AttendanceActivity : AppCompatActivity() {
    private lateinit var eventId: String
    private val selectedUserIds = mutableSetOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_attendance)

        eventId = intent.getStringExtra("EVENT_ID") ?: ""
        val container = findViewById<LinearLayout>(R.id.llStudentsContainer)
        val btnSave = findViewById<Button>(R.id.btnSaveAttendance)

        loadStudents(container)

        btnSave.setOnClickListener {
            saveAttendance()
        }
    }

    private fun loadStudents(container: LinearLayout) {
        lifecycleScope.launch {
            try {
                val studentsResponse = RetrofitClient.instance.getOptedInStudents(eventId)
                val attendanceResponse = RetrofitClient.instance.getAttendance(eventId)
                
                if (studentsResponse.isSuccessful && attendanceResponse.isSuccessful) {
                    val students = studentsResponse.body() ?: emptyList()
                    val markedIds = attendanceResponse.body() ?: emptyList()
                    
                    markedIds.forEach {
                        val id = when (it) {
                            is String -> it
                            is Map<*, *> -> (it["_id"] ?: it["id"]) as? String
                            else -> it.toString()
                        }
                        if (id != null) selectedUserIds.add(id)
                    }

                    students.forEach { student ->
                        val cb = CheckBox(this@AttendanceActivity)
                        cb.text = "${student.fullName} (${student.email})"
                        
                        val isMarked = markedIds.any {
                            when (it) {
                                is String -> it == student.id
                                is Map<*, *> -> it["_id"] == student.id || it["id"] == student.id
                                else -> it.toString() == student.id
                            }
                        }
                        
                        cb.isChecked = isMarked
                        if (isMarked) selectedUserIds.add(student.id)

                        cb.setOnCheckedChangeListener { _, isChecked ->
                            if (isChecked) selectedUserIds.add(student.id)
                            else selectedUserIds.remove(student.id)
                        }
                        container.addView(cb)
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(this@AttendanceActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun saveAttendance() {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.instance.saveAttendance(eventId, AttendanceRequest(selectedUserIds.toList()))
                if (response.isSuccessful) {
                    Toast.makeText(this@AttendanceActivity, "Attendance Saved", Toast.LENGTH_SHORT).show()
                    finish()
                }
            } catch (e: Exception) {
                Toast.makeText(this@AttendanceActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
