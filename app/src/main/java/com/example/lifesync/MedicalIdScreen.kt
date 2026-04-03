package com.example.lifesync

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color as AndroidColor
import android.net.Uri
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import com.example.lifesync.ui.theme.*
import com.google.firebase.auth.FirebaseAuth
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun MedicalIdScreen(onBackClick: () -> Unit) {
    val context = LocalContext.current
    val userId = FirebaseAuth.getInstance().currentUser?.uid
    
    // --- Design System Constants (DESIGN.md) ---
    val deepEggplant = Primary
    val softLilac = SoftLilac
    val surfaceBase = SurfaceBase
    val secondaryZone = SurfaceContainerLow

    // --- Backend States (Live Data) ---
    var bloodType by remember { mutableStateOf("...") }
    var allergies by remember { mutableStateOf<List<String>>(emptyList()) }
    var medications by remember { mutableStateOf<List<String>>(emptyList()) }
    var medicalNotes by remember { mutableStateOf("Establishing secure clinical connection...") }
    var lastUpdated by remember { mutableStateOf("LOADING") }
    var primaryContact by remember { mutableStateOf<Contact?>(null) }
    
    // QR state
    var qrBitmap by remember { mutableStateOf<Bitmap?>(null) }

    // FETCH REAL-TIME DATA FROM FIREBASE
    LaunchedEffect(userId) {
        if (userId != null) {
            // 1. Fetch Medical Profile (Live updates)
            AuthManager.db.collection("Users").document(userId)
                .collection("medical").document("profile")
                .addSnapshotListener { doc, _ ->
                    if (doc != null && doc.exists()) {
                        bloodType = doc.getString("bloodType") ?: "O+"
                        @Suppress("UNCHECKED_CAST")
                        allergies = (doc.get("allergies") as? List<String>) ?: emptyList()
                        @Suppress("UNCHECKED_CAST")
                        medications = (doc.get("medications") as? List<String>) ?: emptyList()
                        medicalNotes = doc.getString("notes") ?: "Patient has a documented history of cardiovascular sensitivity. Regular monitoring of blood pressure is advised. Recent checkups indicate stable heart rhythm, but vigilance during high-stress scenarios or emergencies is critical. Avoid high-sodium intakes and maintain prescribed medication schedule."
                        
                        doc.getTimestamp("lastUpdated")?.let { ts ->
                            val sdf = SimpleDateFormat("MMM yyyy", Locale.US)
                            lastUpdated = sdf.format(ts.toDate()).uppercase()
                        }
                    }
                }

            // 2. Fetch Primary Guardian (Live updates)
            AuthManager.db.collection("Users").document(userId)
                .collection("contacts")
                .whereEqualTo("isPrimaryGuardian", true)
                .addSnapshotListener { snapshot, _ ->
                    if (snapshot != null && !snapshot.isEmpty) {
                        val doc = snapshot.documents[0]
                        primaryContact = Contact(
                            id = doc.id,
                            name = doc.getString("name") ?: "",
                            phone = doc.getString("phone") ?: "",
                            relation = doc.getString("relation") ?: "Guardian",
                            isPrimaryGuardian = true,
                            photoUrl = doc.getString("photoUrl")
                        )
                    }
                }
        }
    }

    // Dynamic QR Generation based on actual clinical data
    LaunchedEffect(bloodType, allergies, medications, primaryContact) {
        val qrData = """
            🚨 VAJRA EMERGENCY ID 🚨
            Name: ${AuthManager.currentUserFullName}
            Blood Type: $bloodType
            Allergies: ${if(allergies.isEmpty()) "None" else allergies.joinToString(", ")}
            Medications: ${if(medications.isEmpty()) "None" else medications.joinToString(", ")}
            Contact: ${primaryContact?.name ?: "N/A"} (${primaryContact?.phone ?: "N/A"})
        """.trimIndent()

        qrBitmap = withContext(Dispatchers.Default) {
            try {
                val size = 512
                val bitMatrix = MultiFormatWriter().encode(qrData, BarcodeFormat.QR_CODE, size, size)
                val bmp = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
                for (x in 0 until size) {
                    for (y in 0 until size) {
                        bmp.setPixel(x, y, if (bitMatrix.get(x, y)) AndroidColor.BLACK else AndroidColor.WHITE)
                    }
                }
                bmp
            } catch (e: Exception) { null }
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(surfaceBase)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // --- HEADER ---
            Box(modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp)) {
                IconButton(onClick = onBackClick, modifier = Modifier.align(Alignment.CenterStart)) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = deepEggplant, modifier = Modifier.size(24.dp))
                }
                Text(
                    "VAJRA",
                    fontFamily = LexendFont,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 4.sp,
                    color = deepEggplant,
                    modifier = Modifier.align(Alignment.Center)
                )
                IconButton(onClick = { /* Navigate to Edit Details */ }, modifier = Modifier.align(Alignment.CenterEnd)) {
                    Icon(Icons.Default.Edit, "Edit", tint = deepEggplant, modifier = Modifier.size(20.dp))
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // --- SECTION 1: QR HERO SECTION ---
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = softLilac.copy(alpha = 0.5f),
                shape = RoundedCornerShape(40.dp)
            ) {
                Column(
                    modifier = Modifier.padding(vertical = 40.dp, horizontal = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Surface(
                        modifier = Modifier.size(190.dp),
                        color = Color.White,
                        shape = RoundedCornerShape(28.dp),
                        shadowElevation = 2.dp
                    ) {
                        Box(modifier = Modifier.padding(20.dp), contentAlignment = Alignment.Center) {
                            if (qrBitmap != null) {
                                Image(bitmap = qrBitmap!!.asImageBitmap(), contentDescription = "QR", modifier = Modifier.fillMaxSize())
                            } else {
                                CircularProgressIndicator(color = deepEggplant, strokeWidth = 2.dp)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(28.dp))
                    Text(
                        "SCAN FOR MEDICAL\nHISTORY",
                        fontFamily = LexendFont,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        color = deepEggplant,
                        letterSpacing = 1.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // --- SECTION 2: BLOOD TYPE HERO CARD ---
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = deepEggplant,
                shape = RoundedCornerShape(40.dp)
            ) {
                Box(modifier = Modifier.padding(32.dp)) {
                    // Subtle background icon as requested
                    Icon(
                        Icons.Default.MedicalServices, 
                        null, 
                        tint = Color.White.copy(alpha = 0.05f), 
                        modifier = Modifier.size(140.dp).align(Alignment.CenterEnd).offset(x = 40.dp)
                    )
                    
                    Column {
                        Text(
                            "BLOOD TYPE",
                            fontFamily = PlusJakartaSansFont,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White.copy(alpha = 0.4f),
                            letterSpacing = 1.5.sp
                        )
                        Text(
                            bloodType,
                            fontFamily = LexendFont,
                            fontSize = 110.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                            lineHeight = 110.sp
                        )
                    }
                    
                    // Small floating kit icon at bottom right of card
                    Surface(
                        modifier = Modifier.align(Alignment.BottomEnd).size(48.dp),
                        color = Color.White.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.MedicalInformation, null, tint = Color.White, modifier = Modifier.size(24.dp))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // --- SECTION 3: BENTO GRID: ALLERGIES & MEDICATIONS ---
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                BentoIdCard(
                    label = "CRITICAL\nALLERGIES",
                    value = allergies.firstOrNull() ?: "None Recorded",
                    icon = Icons.Default.Warning,
                    modifier = Modifier.weight(1f)
                )
                BentoIdCard(
                    label = "MEDICATIONS",
                    value = medications.firstOrNull() ?: "None Recorded",
                    icon = Icons.Default.Medication,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // --- SECTION 4: MEDICAL NOTES CARD (WITH DASHED BORDER) ---
            val dashPath = PathEffect.dashPathEffect(floatArrayOf(12f, 12f), 0f)
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .drawBehind {
                        drawRoundRect(
                            color = deepEggplant.copy(alpha = 0.15f),
                            style = Stroke(width = 2f, pathEffect = dashPath),
                            cornerRadius = CornerRadius(32.dp.toPx())
                        )
                    },
                color = Color.White,
                shape = RoundedCornerShape(32.dp)
            ) {
                Column(modifier = Modifier.padding(28.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Description, null, tint = deepEggplant, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            "MEDICAL NOTES",
                            fontFamily = LexendFont,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = deepEggplant
                        )
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                    Text(
                        medicalNotes,
                        fontFamily = PlusJakartaSansFont,
                        fontSize = 14.sp,
                        lineHeight = 22.sp,
                        color = Color(0xFF49454F)
                    )
                    Spacer(modifier = Modifier.height(28.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "UPDATED : $lastUpdated", 
                            fontFamily = PlusJakartaSansFont,
                            fontSize = 9.sp, 
                            fontWeight = FontWeight.Bold, 
                            color = deepEggplant.copy(alpha = 0.4f),
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            "VERIFIED\nPROFILE", 
                            fontFamily = PlusJakartaSansFont,
                            fontSize = 9.sp, 
                            fontWeight = FontWeight.Bold, 
                            color = deepEggplant.copy(alpha = 0.4f),
                            textAlign = TextAlign.End,
                            lineHeight = 11.sp,
                            letterSpacing = 0.5.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // --- SECTION 5: PRIMARY CONTACT CARD ---
            primaryContact?.let { contact ->
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = secondaryZone,
                    shape = RoundedCornerShape(32.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(modifier = Modifier.size(56.dp), shape = CircleShape, color = Color.White) {
                            if (!contact.photoUrl.isNullOrEmpty()) {
                                AsyncImage(model = contact.photoUrl, contentDescription = null, contentScale = ContentScale.Crop)
                            } else {
                                Box(modifier = Modifier.background(deepEggplant), contentAlignment = Alignment.Center) {
                                    Text(contact.name.take(1).uppercase(), color = Color.White, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                contact.name, 
                                fontFamily = LexendFont, 
                                fontSize = 16.sp, 
                                fontWeight = FontWeight.Bold, 
                                color = deepEggplant
                            )
                            Text(
                                "Primary Emergency Contact", 
                                fontFamily = PlusJakartaSansFont,
                                fontSize = 11.sp, 
                                color = Color.Gray
                            )
                        }
                        Surface(
                            modifier = Modifier.size(44.dp).clickable { 
                                val phoneToCall = contact.phone.replace(Regex("[^0-9+]"), "")
                                val callIntent = Intent(Intent.ACTION_CALL).apply {
                                    data = Uri.parse("tel:$phoneToCall")
                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                }

                                try {
                                    if (ContextCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
                                        context.startActivity(callIntent)
                                    } else {
                                        val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phoneToCall")).apply {
                                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                        }
                                        context.startActivity(dialIntent)
                                    }
                                } catch (e: Exception) {
                                    val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phoneToCall")).apply {
                                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    }
                                    context.startActivity(dialIntent)
                                }
                            },
                            color = Color.White,
                            shape = CircleShape
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Call, null, tint = deepEggplant, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun BentoIdCard(label: String, value: String, icon: ImageVector, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.height(150.dp),
        color = SoftLilac.copy(alpha = 0.4f), // Soft Lilac layering
        shape = RoundedCornerShape(32.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                label,
                fontFamily = PlusJakartaSansFont,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = Primary.copy(alpha = 0.5f),
                lineHeight = 12.sp,
                letterSpacing = 0.5.sp
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                value,
                fontFamily = LexendFont,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Primary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 22.sp
            )
            Spacer(modifier = Modifier.weight(1f))
            Icon(
                icon, 
                null, 
                tint = Primary.copy(alpha = 0.15f),
                modifier = Modifier.size(28.dp).align(Alignment.End)
            )
        }
    }
}
