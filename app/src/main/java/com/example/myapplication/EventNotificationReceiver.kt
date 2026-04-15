package com.example.myapplication

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat

class EventNotificationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val eventName = intent.getStringExtra("EVENT_NAME") ?: "An event"
        

        val serviceIntent = Intent(context, EventService::class.java).apply {
            putExtra("EVENT_NAME", eventName)
        }
        
        ContextCompat.startForegroundService(context, serviceIntent)
    }
}
