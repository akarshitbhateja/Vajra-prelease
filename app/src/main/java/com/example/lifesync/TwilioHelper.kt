package com.example.lifesync

import android.util.Base64
import android.util.Log
import okhttp3.*
import java.io.IOException

object TwilioHelper {
    private val ACCOUNT_SID = BuildConfig.TWILIO_ACCOUNT_SID
    private val AUTH_TOKEN = BuildConfig.TWILIO_AUTH_TOKEN
    private val FROM_NUMBER = BuildConfig.TWILIO_FROM_NUMBER

    private val client = OkHttpClient()

    fun sendSmsAsync(toNumber: String, message: String, onResult: (Boolean) -> Unit) {
        if (ACCOUNT_SID.isBlank() || AUTH_TOKEN.isBlank() || FROM_NUMBER.isBlank()) {
            Log.w("Twilio", "Twilio credentials incomplete. Falling back to automatic background SIM SMS.")
            onResult(false)
            return
        }

        val url = "https://api.twilio.com/2010-04-01/Accounts/$ACCOUNT_SID/Messages.json"

        var cleanNumber = toNumber.replace(Regex("[^0-9+]"), "")
        if (!cleanNumber.startsWith("+")) {
            if (cleanNumber.length == 10) {
                cleanNumber = "+91$cleanNumber"
            } else {
                cleanNumber = "+$cleanNumber"
            }
        }

        val safeMessage = message.replace("🚨", "!")

        val requestBody = FormBody.Builder()
            .add("To", cleanNumber)
            .add("From", FROM_NUMBER)
            .add("Body", safeMessage)
            .build()

        val auth = "$ACCOUNT_SID:$AUTH_TOKEN"
        val base64Auth = Base64.encodeToString(auth.toByteArray(), Base64.NO_WRAP)

        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .addHeader("Authorization", "Basic $base64Auth")
            .addHeader("Accept", "application/json")
            .build()

        Log.d("Twilio", "Dispatching SMS via Twilio API...")

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("Twilio", "Twilio Network request failed: ${e.message}")
                onResult(false)
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string() ?: ""
                val success = response.isSuccessful
                if (!success) {
                    Log.e("Twilio", "Twilio API Error [HTTP ${response.code}]: $responseBody")
                } else {
                    Log.d("Twilio", "Twilio API Success: $responseBody")
                }
                onResult(success)
                response.close()
            }
        })
    }
}