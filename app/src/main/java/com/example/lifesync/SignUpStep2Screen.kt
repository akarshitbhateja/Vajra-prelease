package com.example.lifesync

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WaterDrop
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.example.lifesync.ui.theme.*

@Composable
fun SignUpStep2Screen(onCompleteClick: () -> Unit, onBackClick: () -> Unit, onNavClick: () -> Unit, onAvatarClick: () -> Unit) {
    var selectedBloodType by remember { mutableStateOf("A+") }
    val bloodTypes = listOf("A+", "A-", "B+", "B-", "O+", "O-", "AB+", "AB-")
    var emergencyBroadcast by remember { mutableStateOf(true) }
    
    var allergies by remember { mutableStateOf<List<String>>(emptyList()) }
    var conditions by remember { mutableStateOf<List<String>>(emptyList()) }
    var medications by remember { mutableStateOf<List<String>>(emptyList()) }
    
    var isAddingAllergy by remember { mutableStateOf(false) }
    var isAddingCondition by remember { mutableStateOf(false) }
    var isAddingMedication by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = SurfaceBase,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            Surface(
                modifier = Modifier.fillMaxWidth().navigationBarsPadding(),
                color = Color.White.copy(alpha = 0.95f)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Button(
                        onClick = onCompleteClick,
                        modifier = Modifier.weight(1f).height(64.dp),
                        shape = RoundedCornerShape(32.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE0E7FF))
                    ) {
                        Text("Skip for Now", color = Color(0xFF001B3C), fontWeight = FontWeight.Bold, fontSize = 15.sp, maxLines = 1)
                    }
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
                                    .addOnSuccessListener { isLoading = false; onCompleteClick() }
                                    .addOnFailureListener { isLoading = false; Toast.makeText(context, "System Error: ${it.message}", Toast.LENGTH_SHORT).show() }
                            } else {
                                onCompleteClick()
                            }
                        },
                        modifier = Modifier.weight(1.3f).height(64.dp),
                        shape = RoundedCornerShape(32.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00091B)),
                        enabled = !isLoading
                    ) {
                        if (isLoading) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                        else Text("Save Profile", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp, maxLines = 1)
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp)
        ) {
            VajraHeader(currentScreen = AppScreen.SignUp2, onNavClick = onNavClick, onAvatarClick = onAvatarClick)

            Spacer(modifier = Modifier.height(40.dp))

            Surface(color = Color(0xFFD6E3FF), shape = RoundedCornerShape(50.dp)) {
                Text("STEP 02 — CLINICAL PROFILE", fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp, color = Color(0xFF002045).copy(alpha = 0.7f), modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp))
            }

            Spacer(modifier = Modifier.height(24.dp))
            Text("The Ethereal Sentinel", fontSize = 38.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF001B3C), lineHeight = 44.sp)
            Spacer(modifier = Modifier.height(16.dp))
            Text("Your Digital Medical ID acts as a silent guardian. Configure your critical health data to ensure emergency responders have authoritative guidance when seconds matter.", fontSize = 15.sp, color = Color(0xFF44474E), lineHeight = 24.sp)
            Spacer(modifier = Modifier.height(48.dp))

            Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(32.dp), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(28.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text("Blood Type", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF001B3C))
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("ESSENTIAL IDENTIFICATION", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF44474E), letterSpacing = 0.5.sp)
                        }
                        Icon(Icons.Default.WaterDrop, contentDescription = null, tint = Color(0xFF001B3C), modifier = Modifier.size(28.dp))
                    }
                    Spacer(modifier = Modifier.height(28.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            bloodTypes.take(4).forEach { type -> BloodTypeChip(type, selectedBloodType == type, modifier = Modifier.weight(1f)) { selectedBloodType = type } }
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            bloodTypes.drop(4).forEach { type -> BloodTypeChip(type, selectedBloodType == type, modifier = Modifier.weight(1f)) { selectedBloodType = type } }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(32.dp), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(28.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column {
                            Text("Critical Allergies", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF001B3C))
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("HIGH RISK INDICATORS", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFFBA1A1A), letterSpacing = 0.5.sp)
                        }
                        if (!isAddingAllergy) {
                            Surface(color = Color(0xFFE0E7FF), shape = RoundedCornerShape(50.dp), modifier = Modifier.clickable { isAddingAllergy = true }) {
                                Text("Add Record", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF001B3C), modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp), maxLines = 1)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))

                    if (isAddingAllergy) {
                        MedicalAutoComplete(
                            placeholder = "Search allergies (e.g. Penicillin)",
                            searchType = MedicalSearchType.CONDITIONS,
                            onItemSelected = { 
                                if (!allergies.contains(it)) allergies = allergies + it
                                isAddingAllergy = false 
                            },
                            onDismiss = { isAddingAllergy = false }
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    if (allergies.isEmpty()) {
                        DashedContainer("NO ALLERGIES RECORDED", Icons.Default.Warning)
                    } else {
                        allergies.forEach { allergy ->
                            AllergyRow(allergy, isCritical = true) {
                                allergies = allergies.filter { it != allergy }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(32.dp), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(28.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column {
                            Text("Medical Conditions", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF001B3C))
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("EXISTING DIAGNOSES", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF44474E))
                        }
                        if (!isAddingCondition) {
                            Surface(color = Color(0xFFE0E7FF), shape = RoundedCornerShape(50.dp), modifier = Modifier.clickable { isAddingCondition = true }) {
                                Text("Add Record", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF001B3C), modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp), maxLines = 1)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))

                    if (isAddingCondition) {
                        MedicalAutoComplete(
                            placeholder = "Search conditions (e.g. Asthma)",
                            searchType = MedicalSearchType.CONDITIONS,
                            onItemSelected = { 
                                if (!conditions.contains(it)) conditions = conditions + it
                                isAddingCondition = false 
                            },
                            onDismiss = { isAddingCondition = false }
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    if (conditions.isEmpty()) {
                        DashedContainer("NO CONDITIONS RECORDED", Icons.Default.MedicalServices)
                    } else {
                        conditions.forEach { condition ->
                            AllergyRow(condition, isCritical = false) {
                                conditions = conditions.filter { it != condition }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(32.dp), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(28.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column {
                            Text("Current Prescriptions", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF001B3C))
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("ACTIVE MEDICATIONS", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF44474E))
                        }
                        if (!isAddingMedication) {
                            Surface(color = Color(0xFFE0E7FF), shape = RoundedCornerShape(50.dp), modifier = Modifier.clickable { isAddingMedication = true }) {
                                Text("Add Record", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF001B3C), modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp), maxLines = 1)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))

                    if (isAddingMedication) {
                        MedicalAutoComplete(
                            placeholder = "Search medications (e.g. Lisinopril)",
                            searchType = MedicalSearchType.MEDICATIONS,
                            onItemSelected = { 
                                if (!medications.contains(it)) medications = medications + it
                                isAddingMedication = false 
                            },
                            onDismiss = { isAddingMedication = false }
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    if (medications.isEmpty()) {
                        DashedContainer("NO MEDICATIONS RECORDED", Icons.Default.MedicalServices)
                    } else {
                        medications.forEach { med ->
                            AllergyRow(med, isCritical = false) {
                                medications = medications.filter { it != med }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Box(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(32.dp)).background(Brush.verticalGradient(listOf(Color(0xFF001B3C), Color(0xFF2D0005))))) {
                Column(modifier = Modifier.padding(32.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Surface(color = Color.White.copy(alpha = 0.15f), shape = RoundedCornerShape(50.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)) {
                                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFFFFB3AC)))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("LIVE SYNCING", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp, maxLines = 1)
                            }
                        }
                        Text("✱", color = Color(0xFFFFB3AC), fontSize = 28.sp)
                    }
                    Spacer(modifier = Modifier.height(40.dp))
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text("SENTINEL", color = Color.White, fontSize = 34.sp, fontWeight = FontWeight.Black)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("ACTIVE", color = Color(0xFFFFB3AC).copy(alpha = 0.6f), fontSize = 14.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 6.dp))
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Clinical details are encrypted and staged for immediate emergency retrieval.", color = Color.White.copy(alpha = 0.6f), fontSize = 14.sp, lineHeight = 20.sp)

                    Spacer(modifier = Modifier.height(32.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.Bottom, modifier = Modifier.height(48.dp)) {
                        listOf(0.4f, 0.8f, 1.2f, 0.7f, 0.9f, 0.5f, 0.8f).forEach { scale ->
                            Box(modifier = Modifier.width(4.dp).fillMaxHeight(scale / 1.2f).clip(RoundedCornerShape(50.dp)).background(Color(0xFFFFB3AC)))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Surface(color = Color(0xFFF0F3FF), shape = RoundedCornerShape(32.dp), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(32.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("Emergency Broadcast", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF001B3C), maxLines = 1)
                        Switch(checked = emergencyBroadcast, onCheckedChange = { emergencyBroadcast = it }, colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = Color(0xFF001B3C)))
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Permit verified first responders to scan your Digital ID securely from the lock screen.", color = Color(0xFF44474E), fontSize = 14.sp, lineHeight = 20.sp)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Box(modifier = Modifier.fillMaxWidth().height(180.dp).clip(RoundedCornerShape(32.dp))) {
                AsyncImage(model = "https://images.unsplash.com/photo-1559839734-2b71f153673e?q=80&w=1000", contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                Box(modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f)))))
                Column(modifier = Modifier.padding(28.dp).align(Alignment.BottomStart)) {
                    Text("VERIFICATION", color = Color(0xFFFFB3AC), fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp, maxLines = 1)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Consult a specialist to verify\nyour medical credentials.", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold, lineHeight = 22.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
                }
            }

            // Guaranteed space at bottom of content
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun BloodTypeChip(type: String, isSelected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(modifier = modifier.height(60.dp).clip(CircleShape).background(if (isSelected) Color(0xFF001B3C) else Color(0xFFF0F3FF)).clickable { onClick() }, contentAlignment = Alignment.Center) {
        Text(type, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = if (isSelected) Color.White else Color(0xFF001B3C))
    }
}

@Composable
fun AllergyRow(name: String, isCritical: Boolean, onRemove: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().height(64.dp).clip(RoundedCornerShape(50.dp)).background(if (isCritical) Color(0xFFFFF0F0) else Color(0xFFF0F3FF)), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(start = 24.dp)) {
            Icon(if (isCritical) Icons.Default.Warning else Icons.Default.MedicalServices, contentDescription = null, tint = if (isCritical) Color(0xFFBA1A1A) else Color(0xFF44474E), modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Text(name, color = if (isCritical) Color(0xFFBA1A1A) else Color(0xFF001B3C), fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
        Icon(Icons.Default.Close, contentDescription = "Remove", tint = if (isCritical) Color(0xFFBA1A1A) else Color(0xFF44474E), modifier = Modifier.padding(end = 24.dp).size(24.dp).clickable { onRemove() })
    }
}

@Composable
fun DashedContainer(text: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    val dashPath = PathEffect.dashPathEffect(floatArrayOf(15f, 15f), 0f)
    Box(modifier = Modifier.fillMaxWidth().height(96.dp).drawBehind { drawRoundRect(color = Color(0xFFC4C6CF).copy(alpha = 0.5f), style = Stroke(width = 4f, pathEffect = dashPath), cornerRadius = CornerRadius(48f)) }.background(Color(0xFFF0F3FF), RoundedCornerShape(32.dp)), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, contentDescription = null, tint = Color(0xFF44474E).copy(alpha = 0.6f), modifier = Modifier.size(28.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(text, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp, color = Color(0xFF44474E).copy(alpha = 0.6f), maxLines = 1)
        }
    }
}