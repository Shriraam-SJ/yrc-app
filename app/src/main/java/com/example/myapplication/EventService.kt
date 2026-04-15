package com.example.myapplication

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.telephony.SmsManager
import androidx.core.app.NotificationCompat

class EventService : Service() {

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val eventName = intent?.getStringExtra("EVENT_NAME") ?: "An event"
        
        // This is the mandatory foreground service notification
        val foregroundNotification = NotificationCompat.Builder(this, "event_notifications")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Processing Event")
            .setContentText("Handling notification for $eventName")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true) // Makes it non-dismissible
            .build()
        
        startForeground(1, foregroundNotification)
        
        // Perform background work
        sendNotification(eventName)
        sendSMS(eventName)
        
        // Note: stopSelf() will remove the 'Processing Event' notification immediately.
        // If you want to keep the service running longer, you'd delay this.
        stopSelf()
        return START_NOT_STICKY
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = "event_notifications"
            val channel = NotificationChannel(
                channelId,
                "Event Notifications",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for event starts"
            }
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun sendNotification(eventName: String) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "event_notifications"

        // This is the actual event alert notification
        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("yrc admin")
            .setContentText("$eventName has started.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setOngoing(true) // Makes it non-dismissible by swiping
            .setAutoCancel(false) // Won't disappear when clicked
            .build()

        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }

    private fun sendSMS(eventName: String) {
        val phoneNumber = "9600300311"
        val message = "yrc admin: $eventName has started."
        
        try {
            val smsManager: SmsManager = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                applicationContext.getSystemService(SmsManager::class.java)
            } else {
                SmsManager.getDefault()
            }
            smsManager.sendTextMessage(phoneNumber, null, message, null, null)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}
