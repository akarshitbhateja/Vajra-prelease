package com.example.lifesync

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Build.VERSION_CODES
import android.telephony.SmsManager
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.lifesync.ui.theme.LexendFont
import com.example.lifesync.ui.theme.PlusJakartaSansFont
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun SosScreen(gForce: Double, locationData: String, onCancel: () -> Unit, onTimerFinished: () -> Unit) {

    BackHandler(enabled = true) {}

    var remainingTime by remember { mutableIntStateOf(15) }
    var currentAddress by remember { mutableStateOf("Locating...") }
    var currentCoords by remember { mutableStateOf(locationData) }
    var hasDispatched by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    val animatedProgress by animateFloatAsState(
        targetValue = remainingTime / 15f,
        animationSpec = tween(1000, easing = LinearEasing),
        label = "progress"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "SosGlow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.1f,
        targetValue = 0.4f,
        animationSpec = infiniteRepeatable(animation = tween(1000, easing = FastOutSlowInEasing), repeatMode = RepeatMode.Reverse),
        label = "GlowAlpha"
    )

    val mediaPlayer = remember {
        MediaPlayer.create(context, R.raw.sos_siren).apply { isLooping = true }
    }

    // --- AUTOMATIC VOLUME OVERRIDE ---
    LaunchedEffect(Unit) {
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val maxVol = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        val currentVol = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
        
        // Target volume based on Master Alert Volume (0.0 to 1.0)
        val targetVol = (maxVol * AuthManager.alertVolume).roundToInt()
        
        // If current volume is lower than the threshold, increase it. 
        // If it's already higher, don't decrease it.
        if (currentVol < targetVol) {
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, targetVol, AudioManager.FLAG_SHOW_UI)
        }
        
        mediaPlayer.start()
    }

    DisposableEffect(Unit) {
        onDispose {
            mediaPlayer.stop()
            mediaPlayer.release()
        }
    }

    // HIGH-SPEED LOCATION TRACKING
    LaunchedEffect(Unit) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.lastLocation.addOnSuccessListener { loc ->
                if (loc != null && currentCoords == "Locating...") {
                    currentCoords = "${loc.latitude},${loc.longitude}"
                }
            }

            while (!hasDispatched) {
                fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                    .addOnSuccessListener { loc ->
                        loc?.let {
                            val newCoords = "${it.latitude},${it.longitude}"
                            currentCoords = newCoords
                            launch(Dispatchers.IO) {
                                try {
                                    val geocoder = Geocoder(context, Locale.getDefault())
                                    if (Build.VERSION.SDK_INT >= VERSION_CODES.TIRAMISU) {
                                        geocoder.getFromLocation(it.latitude, it.longitude, 1) { addresses ->
                                            if (addresses.isNotEmpty()) currentAddress = addresses[0].getAddressLine(0) ?: newCoords
                                        }
                                    } else {
                                        @Suppress("DEPRECATION")
                                        val addresses = geocoder.getFromLocation(it.latitude, it.longitude, 1)
                                        currentAddress = if (!addresses.isNullOrEmpty()) addresses[0].getAddressLine(0) ?: newCoords else newCoords
                                    }
                                } catch (e: Exception) { currentAddress = newCoords }
                            }
                        }
                    }
                delay(3000L)
            }
        }
    }

    LaunchedEffect(key1 = true) {
        while (remainingTime > 0 && !hasDispatched) {
            delay(1000L)
            remainingTime--
        }
        if (remainingTime == 0 && !hasDispatched) {
            hasDispatched = true
            triggerFullEmergencyProtocol(context, currentCoords, currentAddress, gForce, onTimerFinished)
        }
    }

    val bgBrush = Brush.verticalGradient(colors = listOf(Color(0xFF5D0000), Color(0xFF1A0000)))

    Box(
        modifier = Modifier.fillMaxSize().background(bgBrush).statusBarsPadding().navigationBarsPadding().padding(horizontal = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "✱", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Black)
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "VAJRA", fontFamily = LexendFont, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 24.sp, letterSpacing = 2.sp)
            }

            Spacer(modifier = Modifier.height(32.dp))
            Text(text = "CRASH\nDETECTED!", fontFamily = LexendFont, color = Color.White, fontSize = 54.sp, fontWeight = FontWeight.Black, fontStyle = FontStyle.Italic, textAlign = TextAlign.Center, lineHeight = 52.sp, letterSpacing = (-1.5).sp)
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = "INITIATING EMERGENCY PROTOCOL IN...", fontFamily = PlusJakartaSansFont, color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
            Spacer(modifier = Modifier.height(32.dp))

            Box(contentAlignment = Alignment.Center) {
                Canvas(modifier = Modifier.size(260.dp)) {
                    val strokeWidth = 8.dp.toPx()
                    drawCircle(brush = Brush.radialGradient(colors = listOf(Color.White.copy(alpha = glowAlpha), Color.Transparent), center = center, radius = size.width / 2 + 40f), radius = size.width / 2 + 40f)
                    drawArc(color = Color.White.copy(alpha = 0.1f), startAngle = 0f, sweepAngle = 360f, useCenter = false, style = Stroke(width = strokeWidth), topLeft = Offset(strokeWidth / 2, strokeWidth / 2), size = Size(size.width - strokeWidth, size.height - strokeWidth))
                    drawArc(color = Color.White, startAngle = -90f, sweepAngle = 360f * animatedProgress, useCenter = false, style = Stroke(width = strokeWidth, cap = StrokeCap.Round), topLeft = Offset(strokeWidth / 2, strokeWidth / 2), size = Size(size.width - strokeWidth, size.height - strokeWidth))
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = remainingTime.toString(), fontFamily = LexendFont, color = Color.White, fontSize = 110.sp, fontWeight = FontWeight.Bold)
                    Text(text = "SECONDS", fontFamily = PlusJakartaSansFont, color = Color.White.copy(alpha = 0.8f), fontSize = 16.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                }
            }

            Spacer(modifier = Modifier.height(48.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                SosInfoCard(icon = Icons.Default.LocationOn, label = "LOCATION", value = currentAddress, modifier = Modifier.weight(1f))
                SosInfoCard(icon = Icons.Default.Speed, label = "G-FORCE", value = if (gForce > 0) String.format(Locale.US, "%.1f Impact", gForce) else "Manual Alert", modifier = Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(32.dp))
            SwipeToCancelSlider(onSwiped = { if (!hasDispatched) { hasDispatched = true; onCancel() } })
            Spacer(modifier = Modifier.height(24.dp))

            // THE INSTANT OVERRIDE BUTTON
            Box(
                modifier = Modifier.border(1.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(50.dp)).clip(RoundedCornerShape(50.dp)).background(Color(0xFF380005)).clickable {
                    if (!hasDispatched) {
                        hasDispatched = true
                        // SIMULTANEOUS DISPATCH - CALL START IMMEDIATELY
                        triggerFullEmergencyProtocol(context, currentCoords, currentAddress, gForce, onTimerFinished)
                    }
                }.padding(horizontal = 24.dp, vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Call, null, tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("CALL PRIMARY GUARDIAN", fontFamily = PlusJakartaSansFont, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
                }
            }
            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}

private fun triggerFullEmergencyProtocol(context: Context, coords: String, address: String, gForce: Double, onFinished: () -> Unit) {
    // 1. INITIATE CALL IMMEDIATELY (Highest Priority)
    initiateEmergencyCall(context, AuthManager.primaryGuardianPhone)

    // 2. DISPATCH BACKGROUND ALERTS
    sendEmergencyAlerts(context, coords, address, gForce)

    // 3. UI FINISH
    onFinished()
}

private fun initiateEmergencyCall(context: Context, phone: String?) {
    val phoneToCall = phone?.replace(Regex("[^0-9+]"), "") ?: "112"
    val isEmergencyNumber = phoneToCall == "112" || phoneToCall == "911" || phoneToCall == "100" || phoneToCall == "108"
    
    // ACTION_CALL connects directly without showing the dialer screen (requires CALL_PHONE permission)
    val intentAction = if (isEmergencyNumber) Intent.ACTION_DIAL else Intent.ACTION_CALL

    val callIntent = Intent(intentAction).apply {
        data = Uri.parse("tel:$phoneToCall")
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }

    try {
        if (intentAction == Intent.ACTION_CALL && ContextCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            callIntent.action = Intent.ACTION_DIAL
        }
        context.startActivity(callIntent)
    } catch (e: Exception) {
        val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phoneToCall")).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
        context.startActivity(dialIntent)
    }
}

private fun sendEmergencyAlerts(context: Context, coords: String, address: String, gForce: Double) {
    val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
    val db = FirebaseFirestore.getInstance()

    val lat = coords.split(",").getOrNull(0)?.trim()?.toDoubleOrNull()
    val lng = coords.split(",").getOrNull(1)?.trim()?.toDoubleOrNull()

    if (lat == null || lng == null || (lat == 0.0 && lng == 0.0)) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
            fusedLocationClient.lastLocation.addOnSuccessListener { loc ->
                if (loc != null) {
                    dispatchAlertsWithCoords(context, uid, db, loc.latitude, loc.longitude, address, gForce)
                } else {
                    dispatchAlertsWithCoords(context, uid, db, 28.6139, 77.2090, if (address == "Locating...") "New Delhi, India" else address, gForce)
                }
            }.addOnFailureListener {
                dispatchAlertsWithCoords(context, uid, db, 28.6139, 77.2090, if (address == "Locating...") "New Delhi, India" else address, gForce)
            }
        } else {
            dispatchAlertsWithCoords(context, uid, db, 28.6139, 77.2090, if (address == "Locating...") "New Delhi, India" else address, gForce)
        }
    } else {
        dispatchAlertsWithCoords(context, uid, db, lat, lng, address, gForce)
    }
}

private fun dispatchAlertsWithCoords(
    context: Context,
    uid: String,
    db: FirebaseFirestore,
    lat: Double,
    lng: Double,
    address: String,
    gForce: Double
) {
    val cleanAddress = if (address.isBlank() || address == "Locating...") "Coordinates ($lat, $lng)" else address
    val mapsUrl = "https://maps.google.com/?q=$lat,$lng"

    val impactText = if (gForce > 0) String.format(Locale.US, "%.1fG", gForce) else "Manual Alert"
    val patientName = AuthManager.currentUserFullName.ifBlank { "User" }

    val message = "URGENT: VAJRA EMERGENCY SOS\n\nPatient: $patientName\nImpact: $impactText\nLocation: $cleanAddress\n\nLive Google Maps:\n$mapsUrl\n\nCoordinates: $lat, $lng\n\nPlease coordinate rescue immediately."

    AuthManager.dispatchEmergencyAlert(gForce, lat, lng, cleanAddress, {}, {})

    db.collection("Users").document(uid).collection("contacts").get().addOnSuccessListener { snapshot ->
        val fetchedContacts = snapshot.documents.mapNotNull { it.getString("phone") }
        val targetContacts = if (fetchedContacts.isNotEmpty()) {
            fetchedContacts
        } else {
            listOfNotNull(AuthManager.primaryGuardianPhone)
        }

        if (targetContacts.isNotEmpty()) {
            targetContacts.forEach { phone ->
                TwilioHelper.sendSmsAsync(phone, message) { success ->
                    if (!success) {
                        sendLocalSms(context, phone, message)
                    }
                }
            }
            Toast.makeText(context, "Emergency SOS Dispatched to Guardians.", Toast.LENGTH_LONG).show()
        } else {
            // Fallback: If no contacts saved in DB, open default SMS composer with emergency message
            Toast.makeText(context, "SOS Triggered. Opening Emergency SMS Composer.", Toast.LENGTH_LONG).show()
            openSmsAppFallback(context, "", message)
        }
    }.addOnFailureListener {
        // Network failure fetching contacts: Open default SMS composer
        openSmsAppFallback(context, AuthManager.primaryGuardianPhone ?: "", message)
    }
}

private fun sendLocalSms(context: Context, phone: String, message: String) {
    val cleanPhone = phone.replace(Regex("[^0-9+]"), "")
    if (cleanPhone.isBlank()) return

    if (ContextCompat.checkSelfPermission(context, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED) {
        try {
            val smsManager = if (Build.VERSION.SDK_INT >= VERSION_CODES.S) {
                context.getSystemService(SmsManager::class.java)
            } else {
                @Suppress("DEPRECATION") SmsManager.getDefault()
            }
            
            if (smsManager != null) {
                val parts = smsManager.divideMessage(message)
                if (parts.size > 1) {
                    smsManager.sendMultipartTextMessage(cleanPhone, null, parts, null, null)
                } else {
                    smsManager.sendTextMessage(cleanPhone, null, message, null, null)
                }
                Log.d("VAJRA", "Background SMS dispatched automatically to $cleanPhone")
                Toast.makeText(context, "Emergency SMS dispatched automatically to $cleanPhone", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Log.e("VAJRA", "Automatic background SMS failed for $cleanPhone: ${e.message}", e)
        }
    } else {
        Log.w("VAJRA", "SEND_SMS permission missing. Cannot send background SMS automatically.")
    }
}

private fun openSmsAppFallback(context: Context, phone: String, message: String) {
    val cleanPhone = phone.replace(Regex("[^0-9+]"), "")
    try {
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("smsto:${Uri.encode(cleanPhone)}")
            putExtra("sms_body", message)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    } catch (e: Exception) {
        Log.e("VAJRA", "Open SMS App failed", e)
    }
}

@Composable
fun SosInfoCard(icon: ImageVector, label: String, value: String, modifier: Modifier = Modifier) {
    Surface(modifier = modifier.height(72.dp), color = Color.White.copy(alpha = 0.08f), shape = RoundedCornerShape(24.dp)) {
        Row(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(36.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.1f)), contentAlignment = Alignment.Center) { Icon(icon, null, tint = Color(0xFFFFB3AC), modifier = Modifier.size(18.dp)) }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(label, fontFamily = PlusJakartaSansFont, color = Color.White.copy(alpha = 0.5f), fontSize = 9.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
                Text(value, fontFamily = PlusJakartaSansFont, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

@Composable
fun SwipeToCancelSlider(onSwiped: () -> Unit) {
    val coroutineScope = rememberCoroutineScope()
    val sliderWidth = 320.dp
    val thumbSize = 72.dp
    val maxDragPx = with(LocalDensity.current) { (sliderWidth - thumbSize).toPx() }
    val offsetX = remember { Animatable(0f) }

    Box(
        modifier = Modifier.width(sliderWidth).height(thumbSize).clip(RoundedCornerShape(50.dp)).background(Color(0xFF1F0000)).border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(50.dp))
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { coroutineScope.launch { offsetX.stop() } },
                    onDragEnd = { coroutineScope.launch { if (offsetX.value >= maxDragPx * 0.8f) { offsetX.animateTo(maxDragPx, tween(200)); onSwiped() } else { offsetX.animateTo(0f, tween(300)) } } },
                    onDrag = { change, dragAmount -> change.consume(); coroutineScope.launch { offsetX.snapTo((offsetX.value + dragAmount.x).coerceIn(0f, maxDragPx)) } }
                )
            },
        contentAlignment = Alignment.CenterStart
    ) {
        Row(modifier = Modifier.fillMaxWidth().alpha(1f - (offsetX.value / maxDragPx)), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
            Text("SWIPE TO CANCEL", fontFamily = PlusJakartaSansFont, color = Color.White.copy(alpha = 0.7f), fontWeight = FontWeight.Bold, fontSize = 13.sp, letterSpacing = 1.5.sp)
            Spacer(modifier = Modifier.width(8.dp))
            Text(">>", color = Color.White.copy(alpha = 0.4f), fontWeight = FontWeight.Black, fontSize = 14.sp)
        }
        Box(modifier = Modifier.offset { IntOffset(offsetX.value.roundToInt(), 0) }.padding(6.dp).size(thumbSize - 12.dp).clip(CircleShape).background(Color.White), contentAlignment = Alignment.Center) { Icon(Icons.Default.ChevronRight, null, tint = Color(0xFF1F0000), modifier = Modifier.size(32.dp)) }
    }
}