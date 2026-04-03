package com.example.lifesync

import android.widget.Toast
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.example.lifesync.ui.theme.*

@Composable
fun MedicalProfileScreen(onBackClick: () -> Unit, onNavClick: () -> Unit, onAvatarClick: () -> Unit) {
    var selectedBloodType by remember { mutableStateOf("O+") }
    val bloodTypes = listOf("A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-")
    
    var allergies by remember { mutableStateOf(listOf("Penicillin", "Latex")) }
    var conditions by remember { mutableStateOf(listOf("Asthma")) }
    var medications by remember { mutableStateOf(listOf("Albuterol")) }
    
    var emergencyBroadcast by remember { mutableStateOf(true) }
    var isLoading by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    // Initialize with existing data from Firebase
    LaunchedEffect(Unit) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            AuthManager.db.collection("Users").document(userId)
                .collection("medical").document("profile")
                .get()
                .addOnSuccessListener { doc ->
                    if (doc.exists()) {
                        selectedBloodType = doc.getString("bloodType") ?: "O+"
                        emergencyBroadcast = doc.getBoolean("emergencyBroadcast") ?: true
                        
                        @Suppress("UNCHECKED_CAST")
                        (doc.get("allergies") as? List<String>)?.let { allergies = it }
                        @Suppress("UNCHECKED_CAST")
                        (doc.get("conditions") as? List<String>)?.let { conditions = it }
                        @Suppress("UNCHECKED_CAST")
                        (doc.get("medications") as? List<String>)?.let { medications = it }
                    }
                }
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFFF9F9FF)).statusBarsPadding()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp)
        ) {
            VajraHeader(currentScreen = AppScreen.MedicalProfile, onNavClick = onNavClick, onAvatarClick = onAvatarClick)

            Spacer(modifier = Modifier.height(32.dp))

            // --- TITLE SECTION ---
            Text(
                text = "The Sentinel\nSetup", 
                fontFamily = LexendFont,
                style = MaterialTheme.typography.displayLarge.copy(
                    fontSize = 34.sp, 
                    fontWeight = FontWeight.Black, 
                    color = Primary, 
                    lineHeight = 40.sp
                )
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Establish your core medical baseline. This critical data enables immediate, precise care when seconds matter.", 
                fontFamily = PlusJakartaSansFont,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontSize = 15.sp, 
                    color = Color.Gray, 
                    lineHeight = 22.sp
                )
            )
            
            Spacer(modifier = Modifier.height(40.dp))

            // --- BLOOD TYPE CARD ---
            SetupCard {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Blood Type", fontFamily = PlusJakartaSansFont, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = Primary))
                    Icon(Icons.Default.WaterDrop, null, tint = Color.LightGray, modifier = Modifier.size(20.dp))
                }
                Spacer(modifier = Modifier.height(24.dp))

                // 2x4 Grid
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        bloodTypes.take(4).forEach { type -> 
                            BloodTypeCircle(type, selectedBloodType == type, modifier = Modifier.weight(1f)) { selectedBloodType = type } 
                        }
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        bloodTypes.drop(4).forEach { type -> 
                            BloodTypeCircle(type, selectedBloodType == type, modifier = Modifier.weight(1f)) { selectedBloodType = type } 
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // --- KNOWN ALLERGIES CARD ---
            SetupCard {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Known Allergies", fontFamily = PlusJakartaSansFont, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = Primary))
                    Surface(color = Color(0xFFFFF0F0), shape = CircleShape, modifier = Modifier.size(28.dp)) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Warning, null, tint = Color(0xFFD32F2F), modifier = Modifier.size(14.dp))
                        }
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
                
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    allergies.forEach { allergy ->
                        MedicalProfileTag(allergy) { allergies = allergies.filter { it != allergy } }
                    }
                    AddTagPill { /* Open dialog to add allergy */ }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // --- CONDITIONS CARD ---
            SetupCard {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Medical Conditions", fontFamily = PlusJakartaSansFont, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = Primary))
                    Icon(Icons.Default.History, null, tint = Color.LightGray, modifier = Modifier.size(20.dp))
                }
                Spacer(modifier = Modifier.height(20.dp))
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    conditions.forEach { condition ->
                        MedicalProfileTag(condition) { conditions = conditions.filter { it != condition } }
                    }
                    AddTagPill { }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // --- MEDICATIONS CARD ---
            SetupCard {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Current Medications", fontFamily = PlusJakartaSansFont, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = Primary))
                    Icon(Icons.Default.Medication, null, tint = Color.LightGray, modifier = Modifier.size(20.dp))
                }
                Spacer(modifier = Modifier.height(20.dp))
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    medications.forEach { med ->
                        MedicalProfileTag(med) { medications = medications.filter { it != med } }
                    }
                    AddTagPill { }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // --- LIVE SENTINEL CARD (ATTRACTIVE GRADIENT) ---
            Surface(
                modifier = Modifier.fillMaxWidth().shadow(12.dp, RoundedCornerShape(32.dp), spotColor = Primary.copy(alpha = 0.3f)),
                shape = RoundedCornerShape(32.dp),
                color = Color.Transparent
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(Primary, Color(0xFF511F52), Color(0xFF2D0005)),
                                start = androidx.compose.ui.geometry.Offset(0f, 0f),
                                end = androidx.compose.ui.geometry.Offset(1000f, 1000f)
                            )
                        )
                ) {
                    Column(modifier = Modifier.padding(32.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(color = Color.White.copy(alpha = 0.15f), shape = CircleShape) {
                                Box(modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp), contentAlignment = Alignment.Center) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(Color(0xFFFFB3AC)))
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text("LIVE SENTINEL", fontFamily = PlusJakartaSansFont, color = Color.White, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp))
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.weight(1f))
                            Icon(Icons.Default.Shield, null, tint = Color.White.copy(alpha = 0.3f), modifier = Modifier.size(24.dp))
                        }
                        
                        Spacer(modifier = Modifier.height(32.dp))
                        Text("ACTIVE PROTECTION", fontFamily = LexendFont, color = Color.White, style = MaterialTheme.typography.headlineMedium.copy(fontSize = 24.sp, fontWeight = FontWeight.Black))
                        Text("Your medical baseline is encrypted and synchronized with the VAJRA sentinel network.", fontFamily = PlusJakartaSansFont, color = Color.White.copy(alpha = 0.7f), style = MaterialTheme.typography.bodyLarge.copy(fontSize = 13.sp, lineHeight = 18.sp))
                        
                        Spacer(modifier = Modifier.height(32.dp))
                        // Waveform Visual
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.Bottom, modifier = Modifier.height(36.dp)) {
                            listOf(0.4f, 0.9f, 0.6f, 1.0f, 0.7f, 0.5f, 0.8f, 0.4f, 0.6f, 0.9f, 0.5f).forEach { h ->
                                Box(modifier = Modifier.width(4.dp).fillMaxHeight(h).clip(CircleShape).background(Color.White.copy(alpha = 0.4f)))
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // --- EMERGENCY BROADCAST CARD ---
            SetupCard {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Emergency Broadcast", fontFamily = PlusJakartaSansFont, style = MaterialTheme.typography.titleMedium.copy(fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Primary))
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Share medical profile with first responders automatically upon SOS trigger.", fontFamily = PlusJakartaSansFont, style = MaterialTheme.typography.bodyLarge.copy(color = Color.Gray, fontSize = 13.sp, lineHeight = 18.sp))
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Switch(
                        checked = emergencyBroadcast, 
                        onCheckedChange = { emergencyBroadcast = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = Primary,
                            uncheckedTrackColor = Color(0xFFE2E2E8),
                            uncheckedThumbColor = Color.White
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(48.dp))

            // --- SAVE BUTTON ---
            Button(
                onClick = {
                    isLoading = true
                    val userId = FirebaseAuth.getInstance().currentUser?.uid
                    if (userId != null) {
                        val profileData = hashMapOf(
                            "bloodType" to selectedBloodType,
                            "emergencyBroadcast" to emergencyBroadcast,
                            "allergies" to allergies,
                            "conditions" to conditions,
                            "medications" to medications,
                            "setupComplete" to true,
                            "lastUpdated" to Timestamp.now()
                        )
                        AuthManager.db.collection("Users").document(userId).collection("medical").document("profile")
                            .set(profileData)
                            .addOnSuccessListener { isLoading = false; onBackClick() }
                            .addOnFailureListener { isLoading = false; Toast.makeText(context, "System Error: ${it.message}", Toast.LENGTH_SHORT).show() }
                    } else {
                        onBackClick()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(72.dp)
                    .shadow(16.dp, RoundedCornerShape(36.dp), spotColor = Primary.copy(alpha = 0.4f)),
                shape = RoundedCornerShape(36.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Primary),
                enabled = !isLoading
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (isLoading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                    } else {
                        Icon(Icons.Default.Lock, null, tint = Color.White, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Save & Secure Profile", fontFamily = PlusJakartaSansFont, style = MaterialTheme.typography.titleMedium.copy(fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White))
                    }
                }
            }

            Spacer(modifier = Modifier.height(140.dp))
        }
    }
}

@Composable
private fun SetupCard(content: @Composable ColumnScope.() -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().shadow(8.dp, RoundedCornerShape(32.dp), spotColor = Color.Black.copy(alpha = 0.1f)),
        color = Color.White,
        shape = RoundedCornerShape(32.dp),
        content = {
            Column(modifier = Modifier.padding(24.dp), content = content)
        }
    )
}

@Composable
private fun BloodTypeCircle(type: String, isSelected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Surface(
        modifier = modifier
            .aspectRatio(1.2f)
            .clickable { onClick() },
        shape = RoundedCornerShape(30.dp), // Higher radius for more circular appearance
        color = if (isSelected) Primary else SoftLilac.copy(alpha = 0.4f)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = type, 
                fontFamily = PlusJakartaSansFont,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Bold, 
                    fontSize = 14.sp, 
                    color = if (isSelected) Color.White else Primary.copy(alpha = 0.7f)
                )
            )
        }
    }
}

@Composable
private fun MedicalProfileTag(text: String, onRemove: () -> Unit) {
    Surface(
        color = Color(0xFFF1F5FF),
        shape = RoundedCornerShape(50.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            Text(text, fontFamily = PlusJakartaSansFont, style = MaterialTheme.typography.bodyLarge.copy(fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Primary))
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                Icons.Default.Close, 
                null, 
                tint = Primary.copy(alpha = 0.4f), 
                modifier = Modifier.size(14.dp).clickable { onRemove() }
            )
        }
    }
}

@Composable
private fun AddTagPill(onClick: () -> Unit) {
    val dashPath = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
    Box(
        modifier = Modifier
            .height(44.dp)
            .width(90.dp)
            .drawBehind {
                drawRoundRect(
                    color = Color.LightGray,
                    style = Stroke(width = 2f, pathEffect = dashPath),
                    cornerRadius = CornerRadius(22.dp.toPx(), 22.dp.toPx())
                )
            }
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Add, null, tint = Color.Gray, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Add", fontFamily = PlusJakartaSansFont, style = MaterialTheme.typography.labelMedium.copy(fontSize = 13.sp, color = Color.Gray, fontWeight = FontWeight.Bold))
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FlowRow(
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    content: @Composable () -> Unit
) {
    androidx.compose.foundation.layout.FlowRow(
        modifier = modifier,
        horizontalArrangement = horizontalArrangement,
        verticalArrangement = verticalArrangement,
        content = { content() }
    )
}
