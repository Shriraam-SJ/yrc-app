package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton

class ScheduledEventsFragment : Fragment() {

    private lateinit var rvEvents: RecyclerView
    private lateinit var fabAddEvent: FloatingActionButton

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_scheduled_events, container, false)

        rvEvents = view.findViewById(R.id.rvEvents)
        fabAddEvent = view.findViewById(R.id.fabAddEvent)

        fabAddEvent.setOnClickListener {
            val intent = Intent(requireContext(), AddEventActivity::class.java)
            startActivity(intent)
        }

        updateUI()

        return view
    }

    override fun onResume() {
        super.onResume()
        updateUI()
    }

    private fun updateUI() {
        val events = EventRepository.getEvents()
        rvEvents.adapter = EventAdapter(events)
    }
}
