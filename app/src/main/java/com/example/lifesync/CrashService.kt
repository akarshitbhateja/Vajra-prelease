package com.example.lifesync

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority

class CrashService : Service() {
    private lateinit var crashDetector: CrashDetector
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onCreate() {
        super.onCreate()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        createNotificationChannel()
        val notification = createNotification()
        startForeground(1, notification)

        crashDetector = CrashDetector(this) { gForce ->
            fetchLocationAndTrigger(gForce)
        }
        crashDetector.start()
    }

    private fun fetchLocationAndTrigger(gForce: Double) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            triggerEmergency(gForce, null)
            return
        }

        // FIXED: Using lastLocation. This takes 0.001 seconds instead of 5-10 seconds!
        // It provides an instant fallback so the SOS screen pops up immediately.
        // The SosScreen will handle getting the high-accuracy live location once it opens.
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                triggerEmergency(gForce, location)
            }
            .addOnFailureListener {
                triggerEmergency(gForce, null)
            }
    }

    private fun triggerEmergency(gForce: Double, location: Location?) {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel("Vajra_Emergency", "Vajra Emergency Alerts", NotificationManager.IMPORTANCE_HIGH)
            manager.createNotificationChannel(channel)
        }

        val locationString = if (location != null) "${location.latitude},${location.longitude}" else "Locating..."
        val lat = location?.latitude ?: 0.0
        val lng = location?.longitude ?: 0.0

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra("CRASH_TRIGGERED", true)
            putExtra("G_FORCE", gForce)
            putExtra("LOCATION_DATA", locationString)
            putExtra("LATITUDE", lat)
            putExtra("LONGITUDE", lng)
        }

        startActivity(intent)

        val pendingIntent = PendingIntent.getActivity(
            this, 100, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val emergencyNotification = NotificationCompat.Builder(this, "Vajra_Emergency")
            .setContentTitle("SEVERE IMPACT DETECTED")
            .setContentText("Impact: ${String.format("%.1f", gForce)}G. VAJRA rescue protocol initiated.")
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setFullScreenIntent(pendingIntent, true)
            .setAutoCancel(true)
            .build()

        manager.notify(999, emergencyNotification)

        Handler(Looper.getMainLooper()).postDelayed({
            crashDetector.start()
        }, 30000)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel("Vajra_Guardian", "Vajra Guardian Service", NotificationManager.IMPORTANCE_LOW)
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(): Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        return NotificationCompat.Builder(this, "Vajra_Guardian")
            .setContentTitle("VAJRA Sentinel Active")
            .setContentText("Actively monitoring for severe impacts.")
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }
}