package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.myapplication.network.SessionManager

class ProfileInfoFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile_info, container, false)

        val sessionManager = SessionManager(requireContext())
        val user = sessionManager.fetchUser()

        val tvName = view.findViewById<TextView>(R.id.tvProfileName)
        val tvRole = view.findViewById<TextView>(R.id.tvProfileRole)
        val tvEmail = view.findViewById<TextView>(R.id.tvProfileEmail)
        val tvPhone = view.findViewById<TextView>(R.id.tvProfilePhone)
        val tvYear = view.findViewById<TextView>(R.id.tvProfileYear)
        val tvBloodGroup = view.findViewById<TextView>(R.id.tvProfileBloodGroup)
        val btnLogout = view.findViewById<Button>(R.id.btnLogout)

        user?.let {
            tvName.text = it.fullName
            tvEmail.text = "Email: ${it.email}"
            tvPhone.text = "Phone: ${it.phoneNumber}"
            tvYear.text = "Year Joined: ${it.yearJoined}"
            tvBloodGroup.text = "Blood Group: ${it.bloodGroup}"
            
            val domain = it.email.substringAfter("@", "")
            tvRole.text = if (domain.contains("tce.edu") && !domain.contains("student")) {
                "Admin / Faculty"
            } else {
                "Student Member"
            }
        }

        btnLogout.setOnClickListener {
            sessionManager.clearSession()
            val intent = Intent(activity, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            requireActivity().finish()
        }

        return view
    }
}
