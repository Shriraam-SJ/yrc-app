package com.example.myapplication

import android.app.AlarmManager
import android.app.DatePickerDialog
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import java.text.SimpleDateFormat
import java.util.*

class AddEventActivity : AppCompatActivity() {

    private var selectedYear = 0
    private var selectedMonth = 0
    private var selectedDay = 0
    private var selectedHour = 0
    private var selectedMinute = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_event)

        val rgEventType = findViewById<RadioGroup>(R.id.rgEventType)
        val tilVolunteers = findViewById<TextInputLayout>(R.id.tilVolunteers)
        val etEventName = findViewById<TextInputEditText>(R.id.etEventName)
        val etVenue = findViewById<TextInputEditText>(R.id.etVenue)
        val etVolunteers = findViewById<TextInputEditText>(R.id.etVolunteers)
        val etDate = findViewById<TextInputEditText>(R.id.etDate)
        val etTime = findViewById<TextInputEditText>(R.id.etTime)
        val btnSubmit = findViewById<Button>(R.id.btnSubmit)

        rgEventType.setOnCheckedChangeListener { _, checkedId ->
            if (checkedId == R.id.rbMeet) {
                tilVolunteers.visibility = View.GONE
            } else {
                tilVolunteers.visibility = View.VISIBLE
            }
        }

        etDate.setOnClickListener {
            val c = Calendar.getInstance()
            val year = c.get(Calendar.YEAR)
            val month = c.get(Calendar.MONTH)
            val day = c.get(Calendar.DAY_OF_MONTH)

            val dpd = DatePickerDialog(this, { _, year, monthOfYear, dayOfMonth ->
                selectedYear = year
                selectedMonth = monthOfYear
                selectedDay = dayOfMonth
                etDate.setText("$dayOfMonth/${monthOfYear + 1}/$year")
            }, year, month, day)
            dpd.show()
        }

        etTime.setOnClickListener {
            val c = Calendar.getInstance()
            val hour = c.get(Calendar.HOUR_OF_DAY)
            val minute = c.get(Calendar.MINUTE)

            val tpd = TimePickerDialog(this, { _, hourOfDay, minute ->
                selectedHour = hourOfDay
                selectedMinute = minute
                etTime.setText(String.format("%02d:%02d", hourOfDay, minute))
            }, hour, minute, true)
            tpd.show()
        }

        btnSubmit.setOnClickListener {
            val type = if (rgEventType.checkedRadioButtonId == R.id.rbEvent) "Event" else "Meet"
            val name = etEventName.text.toString()
            val venue = etVenue.text.toString()
            val volunteers = if (type == "Event") etVolunteers.text.toString() else null
            val date = etDate.text.toString()
            val time = etTime.text.toString()

            if (name.isNotEmpty() && venue.isNotEmpty() && date.isNotEmpty() && time.isNotEmpty()) {
                val newEvent = Event(type, name, venue, volunteers, date, time)
                EventRepository.addEvent(newEvent)
                
                scheduleNotification(name)
                
                Toast.makeText(this, "Event Added successfully", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun scheduleNotification(eventName: String) {
        val calendar = Calendar.getInstance()
        calendar.set(selectedYear, selectedMonth, selectedDay, selectedHour, selectedMinute, 0)

        val intent = Intent(this, EventNotificationReceiver::class.java)
        intent.putExtra("EVENT_NAME", eventName)
        
        val pendingIntent = PendingIntent.getBroadcast(
            this,
            System.currentTimeMillis().toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
            } else {
                alarmManager.set(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
            }
        } else {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )
        }
    }
}
