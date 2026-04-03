package com.example.lifesync

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import com.example.lifesync.ui.theme.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun ContactsScreen(onAddContactClick: () -> Unit, onEditContactClick: (Contact) -> Unit, onNavClick: () -> Unit, onAvatarClick: () -> Unit) {
    var contacts by remember { mutableStateOf<List<Contact>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    val context = LocalContext.current
    val db = FirebaseFirestore.getInstance()
    val userId = FirebaseAuth.getInstance().currentUser?.uid

    // Design System Colors from DESIGN.md
    val surfaceBase = Color(0xFFF9F9FF)
    val primaryColor = Color(0xFF38073B)
    val primaryContainer = Color(0xFF511F52)
    val softLilac = Color(0xFFECD4EA)
    val ghostBorder = Color(0xFFD2C2CD).copy(alpha = 0.2f)

    // Permission handling for direct call
    var pendingPhoneToCall by remember { mutableStateOf<String?>(null) }
    val callPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted && pendingPhoneToCall != null) {
            makeDirectCall(context, pendingPhoneToCall!!)
            pendingPhoneToCall = null
        }
    }

    LaunchedEffect(userId) {
        if (userId != null) {
            db.collection("Users").document(userId).collection("contacts")
                .addSnapshotListener { snapshot, _ ->

                    var list = snapshot?.documents?.map { doc ->
                        Contact(
                            id = doc.id,
                            name = doc.getString("name") ?: "",
                            phone = doc.getString("phone") ?: "",
                            relation = doc.getString("relation") ?: "Friend",
                            isPrimaryGuardian = doc.getBoolean("isPrimaryGuardian") ?: false,
                            photoUrl = doc.getString("photoUrl")
                        )
                    } ?: emptyList()

                    // --- SELF-HEALING PRIMARY LOGIC ---
                    if (list.isNotEmpty()) {
                        val primaryCount = list.count { it.isPrimaryGuardian }

                        if (primaryCount == 0) {
                            val firstId = list.first().id
                            db.collection("Users").document(userId).collection("contacts").document(firstId)
                                .update("isPrimaryGuardian", true)

                            list = list.mapIndexed { index, contact ->
                                if (index == 0) contact.copy(isPrimaryGuardian = true) else contact
                            }
                        } else if (primaryCount > 1) {
                            val batch = db.batch()
                            val contactsToDemote = list.filter { it.isPrimaryGuardian }.drop(1)
                            contactsToDemote.forEach { contact ->
                                val ref = db.collection("Users").document(userId).collection("contacts").document(contact.id)
                                batch.update(ref, "isPrimaryGuardian", false)
                            }
                            batch.commit()

                            val idsToDemote = contactsToDemote.map { it.id }
                            list = list.map { contact ->
                                if (contact.id in idsToDemote) contact.copy(isPrimaryGuardian = false) else contact
                            }
                        }
                    }

                    contacts = list
                    isLoading = false
                }
        } else {
            isLoading = false
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(surfaceBase).statusBarsPadding()) {
        Column(modifier = Modifier.fillMaxSize()) {
            VajraHeader(currentScreen = AppScreen.Contacts, onNavClick = onNavClick, onAvatarClick = onAvatarClick)

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 24.dp)
            ) {
                item {
                    Text(
                        text = "Your Safety Network",
                        fontFamily = LexendFont,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = primaryColor,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Designated individuals who will be notified during an emergency trigger.",
                        fontFamily = PlusJakartaSansFont,
                        fontSize = 14.sp,
                        color = Color(0xFF49454F),
                        lineHeight = 20.sp,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(32.dp))

                    val interactionSource = remember { MutableInteractionSource() }
                    val isPressed by interactionSource.collectIsPressedAsState()
                    val scale by animateFloatAsState(if (isPressed) 0.98f else 1f, label = "button_scale")

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(64.dp)
                            .scale(scale)
                            .shadow(12.dp, CircleShape, spotColor = primaryColor.copy(alpha = 0.2f))
                            .background(Brush.linearGradient(listOf(primaryColor, primaryContainer)), CircleShape)
                            .clickable(interactionSource = interactionSource, indication = null) { onAddContactClick() },
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Add, null, tint = Color.White, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Add Emergency Contact", fontFamily = PlusJakartaSansFont, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(48.dp))
                }

                if (isLoading) {
                    item { CircularProgressIndicator(color = primaryColor) }
                } else {
                    val sortedContacts = contacts.sortedByDescending { it.isPrimaryGuardian }
                    items(sortedContacts, key = { it.id }) { contact ->
                        ContactCard(
                            contact = contact,
                            onClick = { onEditContactClick(contact) },
                            onCall = {
                                if (ContextCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
                                    makeDirectCall(context, contact.phone)
                                } else {
                                    pendingPhoneToCall = contact.phone
                                    callPermissionLauncher.launch(Manifest.permission.CALL_PHONE)
                                }
                            },
                            primaryColor = primaryColor,
                            softColor = softLilac,
                            ghostBorder = ghostBorder
                        )
                        Spacer(modifier = Modifier.height(28.dp))
                    }
                }
                item { Spacer(modifier = Modifier.height(140.dp)) }
            }
        }
    }
}

@Composable
fun ContactCard(contact: Contact, onClick: () -> Unit, onCall: () -> Unit, primaryColor: Color, softColor: Color, ghostBorder: Color) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else if (isHovered) 1.02f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "card_scale"
    )

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .clickable(interactionSource = interactionSource, indication = null) { onClick() },
        color = Color.White,
        shape = RoundedCornerShape(32.dp),
        shadowElevation = if (isHovered) 16.dp else 4.dp,
        border = BorderStroke(1.dp, ghostBorder)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Box(
                modifier = Modifier.wrapContentSize(),
                contentAlignment = Alignment.BottomCenter
            ) {
                Surface(
                    modifier = Modifier.size(90.dp),
                    shape = RoundedCornerShape(28.dp),
                    color = Color.White,
                    border = BorderStroke(3.dp, Color.White),
                    shadowElevation = 2.dp
                ) {
                    Box(modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(25.dp))) {
                        if (!contact.photoUrl.isNullOrEmpty()) {
                            AsyncImage(
                                model = contact.photoUrl,
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            // FIXED: Solid Soft Lilac background without transparency
                            Box(modifier = Modifier.fillMaxSize().background(softColor), contentAlignment = Alignment.Center) {
                                Text(contact.name.take(1).uppercase(), fontFamily = PlusJakartaSansFont, fontSize = 36.sp, fontWeight = FontWeight.Bold, color = primaryColor)
                            }
                        }
                    }
                }

                if (contact.isPrimaryGuardian) {
                    Surface(
                        modifier = Modifier.offset(y = 12.dp),
                        color = primaryColor,
                        shape = CircleShape,
                        shadowElevation = 6.dp,
                        border = BorderStroke(2.dp, Color.White)
                    ) {
                        Text(
                            "PRIMARY",
                            fontFamily = PlusJakartaSansFont,
                            color = Color.White,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                            letterSpacing = 1.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            Text(
                text = contact.name,
                fontFamily = LexendFont,
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                color = primaryColor
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    color = softColor.copy(alpha = 0.4f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = contact.relation.uppercase(),
                        fontFamily = PlusJakartaSansFont,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = primaryColor.copy(alpha = 0.8f),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = contact.phone,
                    fontFamily = PlusJakartaSansFont,
                    fontSize = 14.sp,
                    color = Color.Gray,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .clickable { onCall() },
                color = Color.Transparent,
                shape = CircleShape,
                border = BorderStroke(1.dp, ghostBorder.copy(alpha = 0.4f))
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(Icons.Default.Call, null, tint = primaryColor, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        "QUICK CALL",
                        fontFamily = PlusJakartaSansFont,
                        color = primaryColor,
                        fontWeight = FontWeight.Black,
                        fontSize = 13.sp,
                        letterSpacing = 1.sp
                    )
                }
            }
        }
    }
}

private fun makeDirectCall(context: Context, phone: String) {
    try {
        val intent = Intent(Intent.ACTION_CALL).apply { data = Uri.parse("tel:$phone") }
        context.startActivity(intent)
    } catch (e: Exception) {
        val dialIntent = Intent(Intent.ACTION_DIAL).apply { data = Uri.parse("tel:$phone") }
        context.startActivity(dialIntent)
    }
}
