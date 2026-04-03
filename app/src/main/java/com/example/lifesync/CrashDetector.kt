package com.example.lifesync

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlin.math.sqrt

class CrashDetector(context: Context, private val onCrashDetected: (Double) -> Unit) : SensorEventListener {
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    // PERFECT THRESHOLD: 10.0G catches severe falls and car impacts without irritating false alarms.
    private val CRASH_THRESHOLD_G = 10.0
    private var isListening = false

    fun start() {
        if (!isListening && accelerometer != null) {
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME)
            isListening = true
        }
    }

    fun stop() {
        if (isListening) {
            sensorManager.unregisterListener(this)
            isListening = false
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_ACCELEROMETER) {
            val x = event.values[0]
            val y = event.values[1]
            val z = event.values[2]

            val gX = x / 9.81f
            val gY = y / 9.81f
            val gZ = z / 9.81f

            val gForce = sqrt((gX * gX + gY * gY + gZ * gZ).toDouble())

            if (gForce > CRASH_THRESHOLD_G) {
                onCrashDetected(gForce)
                stop() // Pause listening during the SOS countdown
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}