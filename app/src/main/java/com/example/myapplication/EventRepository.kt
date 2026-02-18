package com.example.myapplication

data class Event(
    val type: String, // "Event" or "Meet"
    val name: String,
    val venue: String,
    val volunteersRequired: String?,
    val date: String,
    val time: String
)

object EventRepository {
    private val events = mutableListOf<Event>()

    fun addEvent(event: Event) {
        events.add(event)
    }

    fun getEvents(): List<Event> {
        return events
    }
}
