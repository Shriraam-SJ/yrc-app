package com.example.myapplication.network

import com.google.gson.annotations.SerializedName

data class RegisterRequest(
    val fullName: String,
    val email: String,
    val phoneNumber: String,
    val yearJoined: String,
    val bloodGroup: String,
    val password: String,
    val otp: String? = null
)

data class LoginRequest(
    val email: String,
    val password: String
)

data class LoginResponse(
    val token: String,
    val user: User
)

data class User(
    @SerializedName("_id", alternate = ["id"])
    val id: String,
    val fullName: String,
    val email: String,
    val phoneNumber: String,
    val yearJoined: String,
    val bloodGroup: String
)

data class Event(
    @SerializedName("_id", alternate = ["id"])
    val _id: String? = null,
    val title: String? = null,
    val description: String? = null,
    val date: String? = null,
    val fromTime: String? = null,
    val toTime: String? = null,
    val location: String? = null,
    val postedBy: Any? = null, // Can be String ID or populated Object
    val optedInStudents: List<Any> = emptyList(),
    val attendance: List<Any> = emptyList()
)

data class OptRequest(
    val userId: String
)

data class AttendanceRequest(
    val attendanceList: List<String>
)

data class OtpRequest(
    val email: String
)

data class ApiResponse(
    val success: Boolean,
    val message: String
)

data class Message(
    @SerializedName("_id", alternate = ["id"])
    val id: String? = null,
    val sender: Any? = null, // Can be String ID or User object
    val content: String,
    val timestamp: String? = null,
    val isEdited: Boolean = false,
    val isDeleted: Boolean = false,
    val isEmergency: Boolean = false
)

data class MessageRequest(
    val senderId: String? = null,
    val content: String,
    val isEmergency: Boolean = false
)
