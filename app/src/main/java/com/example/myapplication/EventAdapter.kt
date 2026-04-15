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

import com.example.myapplication.network.Event

class EventAdapter(
    private val events: List<Event>,
    private val onItemClick: (Event) -> Unit
) : RecyclerView.Adapter<EventAdapter.EventViewHolder>() {

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
        
        // Extract type from description if possible, or default to "Event"
        val description = event.description ?: ""
        val type = if (description.contains("Type: Meet")) "Meet" else "Event"
        holder.tvEventType.text = type
        
        holder.tvEventName.text = event.title ?: "No Title"
        holder.tvEventVenue.text = "Venue: ${event.location ?: "N/A"}"
        holder.tvEventDate.text = "Date: ${event.date ?: "N/A"}"
        holder.tvEventTime.text = "Time: ${event.fromTime ?: ""} - ${event.toTime ?: ""}"

        if (type == "Event" && description.contains("Volunteers:")) {
            holder.tvVolunteers.visibility = View.VISIBLE
            holder.tvVolunteers.text = description.substringAfter("Volunteers:").trim()
        } else {
            holder.tvVolunteers.visibility = View.GONE
        }

        // Logic to determine status
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        try {
            val dateStr = event.date ?: ""
            val fromTimeStr = event.fromTime ?: ""
            val toTimeStr = event.toTime ?: ""
            
            val startTime = sdf.parse("$dateStr $fromTimeStr")
            val endTime = sdf.parse("$dateStr $toTimeStr")
            val now = Calendar.getInstance().time
            
            if (startTime != null && endTime != null) {
                when {
                    now.before(startTime) -> {
                        holder.tvStatus.text = "Scheduled"
                        holder.tvStatus.setBackgroundResource(R.color.orange_status)
                    }
                    now.after(endTime) -> {
                        holder.tvStatus.text = "Finished"
                        holder.tvStatus.setBackgroundResource(R.color.red_primary)
                    }
                    else -> {
                        holder.tvStatus.text = "Ongoing"
                        holder.tvStatus.setBackgroundResource(R.color.green_status)
                    }
                }
            } else {
                holder.tvStatus.text = "Scheduled"
                holder.tvStatus.setBackgroundResource(R.color.orange_status)
            }
        } catch (e: Exception) {
            holder.tvStatus.text = "Scheduled"
            holder.tvStatus.setBackgroundResource(R.color.orange_status)
        }

        holder.itemView.setOnClickListener { onItemClick(event) }
    }

    override fun getItemCount() = events.size
}
