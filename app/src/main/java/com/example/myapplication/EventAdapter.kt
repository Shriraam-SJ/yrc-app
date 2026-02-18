package com.example.myapplication

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*

class EventAdapter(private val events: List<Event>) :
    RecyclerView.Adapter<EventAdapter.EventViewHolder>() {

    class EventViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvEventType: TextView = view.findViewById(R.id.tvEventType)
        val tvStatus: TextView = view.findViewById(R.id.tvStatus)
        val tvEventName: TextView = view.findViewById(R.id.tvEventName)
        val tvEventVenue: TextView = view.findViewById(R.id.tvEventVenue)
        val tvEventDate: TextView = view.findViewById(R.id.tvEventDate)
        val tvEventTime: TextView = view.findViewById(R.id.tvEventTime)
        val tvVolunteers: TextView = view.findViewById(R.id.tvVolunteers)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_event, parent, false)
        return EventViewHolder(view)
    }

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        val event = events[position]
        holder.tvEventType.text = event.type
        holder.tvEventName.text = event.name
        holder.tvEventVenue.text = "Venue: ${event.venue}"
        holder.tvEventDate.text = "Date: ${event.date}"
        holder.tvEventTime.text = "Time: ${event.time}"

        if (event.type == "Event" && !event.volunteersRequired.isNullOrEmpty()) {
            holder.tvVolunteers.visibility = View.VISIBLE
            holder.tvVolunteers.text = "Volunteers Required: ${event.volunteersRequired}"
        } else {
            holder.tvVolunteers.visibility = View.GONE
        }

        // Logic to determine status
        val sdf = SimpleDateFormat("d/M/yyyy HH:mm", Locale.getDefault())
        try {
            val eventDateTime = sdf.parse("${event.date} ${event.time}")
            val currentTime = Calendar.getInstance().time
            
            if (eventDateTime != null && currentTime.after(eventDateTime)) {
                holder.tvStatus.text = "Ongoing"
                holder.tvStatus.setBackgroundResource(R.color.green_status)
            } else {
                holder.tvStatus.text = "Scheduled"
                holder.tvStatus.setBackgroundResource(R.color.orange_status)
            }
        } catch (e: Exception) {
            holder.tvStatus.text = "Scheduled"
            holder.tvStatus.setBackgroundResource(R.color.orange_status)
        }
    }

    override fun getItemCount() = events.size
}
