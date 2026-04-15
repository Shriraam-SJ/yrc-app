package com.example.myapplication

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.myapplication.network.RetrofitClient
import com.example.myapplication.network.Event
import com.example.myapplication.network.SessionManager
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.launch
import java.util.*

class AddEventActivity : AppCompatActivity() {

    private var selectedDate = ""
    private var fromTime = ""
    private var toTime = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_event)

        val rgEventType = findViewById<RadioGroup>(R.id.rgEventType)
        val tilVolunteers = findViewById<TextInputLayout>(R.id.tilVolunteers)
        val etEventName = findViewById<TextInputEditText>(R.id.etEventName)
        val etVenue = findViewById<TextInputEditText>(R.id.etVenue)
        val etVolunteers = findViewById<TextInputEditText>(R.id.etVolunteers)
        val etDate = findViewById<TextInputEditText>(R.id.etDate)
        val etFromTime = findViewById<TextInputEditText>(R.id.etFromTime)
        val etToTime = findViewById<TextInputEditText>(R.id.etToTime)
        val btnSubmit = findViewById<Button>(R.id.btnSubmit)
        val sessionManager = SessionManager(this)

        rgEventType.setOnCheckedChangeListener { _, checkedId ->
            tilVolunteers.visibility = if (checkedId == R.id.rbMeet) View.GONE else View.VISIBLE
        }

        etDate.setOnClickListener {
            val c = Calendar.getInstance()
            DatePickerDialog(this, { _, year, month, day ->
                selectedDate = String.format("%04d-%02d-%02d", year, month + 1, day)
                etDate.setText(selectedDate)
            }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show()
        }

        etFromTime.setOnClickListener {
            showTimePicker { time ->
                fromTime = time
                etFromTime.setText(fromTime)
            }
        }

        etToTime.setOnClickListener {
            showTimePicker { time ->
                toTime = time
                etToTime.setText(toTime)
            }
        }

        btnSubmit.setOnClickListener {
            val name = etEventName.text.toString()
            val venue = etVenue.text.toString()
            val desc = "Type: ${if (rgEventType.checkedRadioButtonId == R.id.rbEvent) "Event" else "Meet"}. Volunteers: ${etVolunteers.text}"
            
            if (name.isNotEmpty() && venue.isNotEmpty() && selectedDate.isNotEmpty() && fromTime.isNotEmpty() && toTime.isNotEmpty()) {
                val user = sessionManager.fetchUser()
                val userId = user?.id ?: ""
                
                if (userId.isEmpty()) {
                    Toast.makeText(this@AddEventActivity, "Error: User ID not found. Please re-login.", Toast.LENGTH_LONG).show()
                    return@setOnClickListener
                }

                val event = Event(
                    _id = null, // MongoDB generates this
                    title = name,
                    description = desc,
                    date = selectedDate,
                    fromTime = fromTime,
                    toTime = toTime,
                    location = venue,
                    postedBy = userId,
                    optedInStudents = emptyList(),
                    attendance = emptyList()
                )

                lifecycleScope.launch {
                    try {
                        val response = RetrofitClient.instance.createEvent(event)
                        if (response.isSuccessful) {
                            Toast.makeText(this@AddEventActivity, "Event Posted Successfully", Toast.LENGTH_SHORT).show()
                            finish()
                        } else {
                            val errorBody = response.errorBody()?.string()
                            android.util.Log.e("AddEvent", "Server Error: $errorBody")
                            Toast.makeText(this@AddEventActivity, "Failed: ${response.code()} $errorBody", Toast.LENGTH_LONG).show()
                        }
                    } catch (e: Exception) {
                        android.util.Log.e("AddEvent", "Exception", e)
                        Toast.makeText(this@AddEventActivity, "Network Error: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                    }
                }
            } else {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showTimePicker(onTimeSelected: (String) -> Unit) {
        val c = Calendar.getInstance()
        TimePickerDialog(this, { _, hour, minute ->
            onTimeSelected(String.format("%02d:%02d", hour, minute))
        }, c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), true).show()
    }
}
