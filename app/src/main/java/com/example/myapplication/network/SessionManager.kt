package com.example.myapplication.network

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {
    private var prefs: SharedPreferences = context.getSharedPreferences("AppSession", Context.MODE_PRIVATE)

    companion object {
        const val USER_TOKEN = "user_token"
        const val USER_ID = "user_id"
        const val USER_EMAIL = "user_email"
        const val USER_NAME = "user_name"
        const val USER_PHONE = "user_phone"
        const val USER_YEAR = "user_year"
        const val USER_BLOOD = "user_blood"
    }

    fun saveUser(user: User) {
        val editor = prefs.edit()
        editor.putString(USER_ID, user.id)
        editor.putString(USER_NAME, user.fullName)
        editor.putString(USER_EMAIL, user.email)
        editor.putString(USER_PHONE, user.phoneNumber)
        editor.putString(USER_YEAR, user.yearJoined)
        editor.putString(USER_BLOOD, user.bloodGroup)
        editor.apply()
    }

    fun fetchUser(): User? {
        val id = prefs.getString(USER_ID, null) ?: return null
        return User(
            id = id,
            fullName = prefs.getString(USER_NAME, "") ?: "",
            email = prefs.getString(USER_EMAIL, "") ?: "",
            phoneNumber = prefs.getString(USER_PHONE, "") ?: "",
            yearJoined = prefs.getString(USER_YEAR, "") ?: "",
            bloodGroup = prefs.getString(USER_BLOOD, "") ?: ""
        )
    }

    fun saveAuthToken(token: String) {
        val editor = prefs.edit()
        editor.putString(USER_TOKEN, token)
        editor.apply()
    }

    fun fetchAuthToken(): String? {
        return prefs.getString(USER_TOKEN, null)
    }

    fun saveUserEmail(email: String) {
        val editor = prefs.edit()
        editor.putString(USER_EMAIL, email)
        editor.apply()
    }

    fun fetchUserEmail(): String? {
        return prefs.getString(USER_EMAIL, null)
    }

    fun clearSession() {
        val editor = prefs.edit()
        editor.clear()
        editor.apply()
    }
}
