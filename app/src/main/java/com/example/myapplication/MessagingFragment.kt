package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.network.Message
import com.example.myapplication.network.MessageRequest
import com.example.myapplication.network.RetrofitClient
import com.example.myapplication.network.SessionManager
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class MessagingFragment : Fragment() {

    private lateinit var rvMessages: RecyclerView
    private lateinit var etMessage: EditText
    private lateinit var btnSend: FloatingActionButton
    private lateinit var btnAddRequest: FloatingActionButton
    private lateinit var adapter: MessageAdapter
    private lateinit var sessionManager: SessionManager
    
    private var currentUserId: String = ""
    private var isAdmin: Boolean = false
    
    private val handler = Handler(Looper.getMainLooper())
    private val refreshRunnable = object : Runnable {
        override fun run() {
            fetchMessages()
            handler.postDelayed(this, 5000) // Refresh every 5 seconds
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_messaging, container, false)
        
        sessionManager = SessionManager(requireContext())
        val user = sessionManager.fetchUser()
        currentUserId = user?.id ?: ""
        val email = sessionManager.fetchUserEmail() ?: ""
        isAdmin = email.contains("@student.tce.edu")

        rvMessages = view.findViewById(R.id.rvMessages)
        etMessage = view.findViewById(R.id.etMessage)
        btnSend = view.findViewById(R.id.btnSend)
        btnAddRequest = view.findViewById(R.id.btnAddRequest)

        adapter = MessageAdapter(emptyList(), currentUserId, isAdmin) { message ->
            showOptionsDialog(message)
        }
        
        rvMessages.layoutManager = LinearLayoutManager(requireContext()).apply {
            stackFromEnd = true
        }
        rvMessages.adapter = adapter

        btnSend.setOnClickListener {
            val content = etMessage.text.toString().trim()
            if (content.isNotEmpty()) {
                sendMessage(content)
            }
        }

        btnAddRequest.setOnClickListener {
            showBloodRequestDialog()
        }

        fetchMessages()
        return view
    }

    override fun onResume() {
        super.onResume()
        handler.post(refreshRunnable)
    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(refreshRunnable)
    }

    private fun fetchMessages() {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.instance.getMessages()
                if (response.isSuccessful) {
                    val messages = response.body() ?: emptyList()
                    adapter.updateMessages(messages)
                    if (messages.isNotEmpty()) {
                        rvMessages.scrollToPosition(adapter.itemCount - 1)
                    }
                }
            } catch (e: Exception) {
                // Silently fail for background refresh
            }
        }
    }

    private fun sendMessage(content: String, isEmergency: Boolean = false) {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.instance.sendMessage(
                    MessageRequest(senderId = currentUserId, content = content, isEmergency = isEmergency)
                )
                if (response.isSuccessful) {
                    etMessage.text.clear()
                    fetchMessages()
                } else {
                    Toast.makeText(requireContext(), "Failed to send message", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showBloodRequestDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_blood_request, null)
        val etBloodType = dialogView.findViewById<EditText>(R.id.etBloodType)
        val etPatientName = dialogView.findViewById<EditText>(R.id.etPatientName)
        val etHospital = dialogView.findViewById<EditText>(R.id.etHospital)
        val etLocation = dialogView.findViewById<EditText>(R.id.etLocation)
        val etGender = dialogView.findViewById<EditText>(R.id.etGender)
        val etContactPhone = dialogView.findViewById<EditText>(R.id.etContactPhone)

        AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setPositiveButton("Send Request") { _, _ ->
                val bloodType = etBloodType.text.toString().trim().uppercase()
                val name = etPatientName.text.toString().trim().uppercase()
                val hospital = etHospital.text.toString().trim().uppercase()
                val location = etLocation.text.toString().trim().uppercase()
                val gender = etGender.text.toString().trim().uppercase()
                val phone = etContactPhone.text.toString().trim()

                if (bloodType.isNotEmpty() && name.isNotEmpty() && phone.isNotEmpty()) {
                    val formattedMessage = """
                        🚨 EMERGENCY BLOOD REQUEST 🚨
                        
                        BLOOD TYPE: $bloodType
                        PATIENT NAME: $name
                        GENDER: $gender
                        HOSPITAL: $hospital
                        LOCATION: $location
                        CONTACT: $phone
                        
                        PLEASE REACH OUT IF YOU CAN DONATE OR HELP!
                    """.trimIndent()
                    
                    sendMessage(formattedMessage, isEmergency = true)
                } else {
                    Toast.makeText(requireContext(), "Please fill essential fields", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showOptionsDialog(message: Message) {
        if (message.isDeleted) return

        val options = mutableListOf<String>()

        val senderId = when (val sender = message.sender) {
            is String -> sender
            is Map<*, *> -> (sender["_id"] ?: sender["id"]) as? String
            else -> null
        }

        val isOwnMessage = senderId == currentUserId
        
        // Edit option: if own message and within 15 mins
        if (isOwnMessage && isWithinTimeLimit(message.timestamp, 15)) {
            options.add("Edit")
        }
        
        // Delete option: if own message OR if admin
        if (isOwnMessage || isAdmin) {
            options.add("Delete for everyone")
        }

        if (options.isEmpty()) return

        AlertDialog.Builder(requireContext())
            .setItems(options.toTypedArray()) { _, which ->
                when (options[which]) {
                    "Edit" -> showEditDialog(message)
                    "Delete for everyone" -> deleteMessage(message)
                }
            }
            .show()
    }

    private fun isWithinTimeLimit(timestamp: String?, limitMinutes: Int): Boolean {
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
            sdf.timeZone = TimeZone.getTimeZone("UTC")
            val date = sdf.parse(timestamp ?: "")
            val now = Calendar.getInstance().time
            val diff = now.time - (date?.time ?: 0)
            diff < limitMinutes * 60 * 1000
        } catch (e: Exception) {
            false
        }
    }

    private fun showEditDialog(message: Message) {
        val input = EditText(requireContext())
        input.setText(message.content)
        
        AlertDialog.Builder(requireContext())
            .setTitle("Edit Message")
            .setView(input)
            .setPositiveButton("Save") { _, _ ->
                val newContent = input.text.toString().trim()
                if (newContent.isNotEmpty()) {
                    editMessage(message.id!!, newContent)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun editMessage(messageId: String, content: String) {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.instance.editMessage(messageId, MessageRequest(content = content))
                if (response.isSuccessful) {
                    fetchMessages()
                } else {
                    Toast.makeText(requireContext(), "Failed to edit message", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun deleteMessage(message: Message) {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.instance.deleteMessage(message.id!!)
                if (response.isSuccessful) {
                    fetchMessages()
                } else {
                    Toast.makeText(requireContext(), "Failed to delete message", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
