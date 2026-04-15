package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton

import androidx.lifecycle.lifecycleScope
import com.example.myapplication.network.RetrofitClient
import com.example.myapplication.network.SessionManager
import com.google.gson.Gson
import kotlinx.coroutines.launch

class ScheduledEventsFragment : Fragment() {

    private lateinit var rvEvents: RecyclerView
    private lateinit var fabAddEvent: FloatingActionButton
    private lateinit var sessionManager: SessionManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_scheduled_events, container, false)

        sessionManager = SessionManager(requireContext())
        rvEvents = view.findViewById(R.id.rvEvents)
        fabAddEvent = view.findViewById(R.id.fabAddEvent)

        val email = sessionManager.fetchUserEmail() ?: ""
        if (email.contains("@student.tce.edu")) {
            fabAddEvent.visibility = View.VISIBLE
        } else {
            fabAddEvent.visibility = View.GONE
        }

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
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.instance.getEvents()
                if (response.isSuccessful && response.body() != null) {
                    val events = response.body()!!
                    android.util.Log.d("ScheduledEvents", "Fetched ${events.size} events")
                    rvEvents.adapter = EventAdapter(events) { event ->
                        val intent = Intent(requireContext(), EventDetailsActivity::class.java)
                        intent.putExtra("EVENT_JSON", Gson().toJson(event))
                        startActivity(intent)
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    android.util.Log.e("ScheduledEvents", "Error response: $errorBody")
                }
            } catch (e: Exception) {
                android.util.Log.e("ScheduledEvents", "Fetch failed", e)
            }
        }
    }
}
