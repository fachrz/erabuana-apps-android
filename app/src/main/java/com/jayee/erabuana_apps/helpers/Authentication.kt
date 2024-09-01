package com.jayee.erabuana_apps.helpers

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.util.Base64
import com.jayee.erabuana_apps.LoginActivity
import org.json.JSONObject
import java.nio.charset.Charset
import java.util.Date

object AuthenticationHelper {

    fun authenticate(context: Context): Boolean {
        if (!isTokenValid(context)) {
            context.startActivity(Intent(context, LoginActivity::class.java))
            return false
        }
        return true
    }

    private fun isTokenValid(context: Context): Boolean {
        val sharedPreferences: SharedPreferences = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val authToken = sharedPreferences.getString("authToken", null)

        if (authToken.isNullOrEmpty()) {
            return false
        }

        try {
            val parts = authToken.split(".")
            if (parts.size < 3) return false

            val payload = String(Base64.decode(parts[1], Base64.URL_SAFE), Charset.forName("UTF-8"))
            val jsonObject = JSONObject(payload)
            val now = Date().time / 1000

            // Check if the token has an "exp" (expiration) claim
            if (jsonObject.getLong("exp") < now) {
                sharedPreferences.edit().remove("authToken").apply()
                return false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }

        return true
    }
}