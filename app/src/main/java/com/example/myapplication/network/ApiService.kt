package com.example.myapplication.network

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface ApiService {
    @POST("auth/send-otp")
    suspend fun sendOtp(@Body request: OtpRequest): Response<ApiResponse>

    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<ApiResponse>

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    // Event Endpoints
    @POST("events")
    suspend fun createEvent(@Body event: Event): Response<ApiResponse>

    @GET("events")
    suspend fun getEvents(): Response<List<Event>>

    @POST("events/{id}/opt-in")
    suspend fun optIn(@Path("id") id: String, @Body request: OptRequest): Response<ApiResponse>

    @POST("events/{id}/opt-out")
    suspend fun optOut(@Path("id") id: String, @Body request: OptRequest): Response<ApiResponse>

    @GET("events/{id}/students")
    suspend fun getOptedInStudents(@Path("id") id: String): Response<List<User>>

    @POST("events/{id}/attendance")
    suspend fun saveAttendance(@Path("id") id: String, @Body request: AttendanceRequest): Response<ApiResponse>

    @GET("events/{id}/attendance")
    suspend fun getAttendance(@Path("id") id: String): Response<List<Any>>

    // Messaging Endpoints
    @GET("messages")
    suspend fun getMessages(): Response<List<Message>>

    @POST("messages")
    suspend fun sendMessage(@Body request: MessageRequest): Response<Message>

    @POST("messages/{id}/edit")
    suspend fun editMessage(@Path("id") id: String, @Body request: MessageRequest): Response<ApiResponse>

    @POST("messages/{id}/delete")
    suspend fun deleteMessage(@Path("id") id: String): Response<ApiResponse>

    // User Endpoints
    @GET("users/search-blood")
    suspend fun searchBlood(@retrofit2.http.Query("bloodGroup") bloodGroup: String): Response<List<User>>
}
