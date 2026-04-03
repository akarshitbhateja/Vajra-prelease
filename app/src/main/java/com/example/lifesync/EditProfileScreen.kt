package com.example.lifesync

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.lifesync.ui.theme.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(onBackClick: () -> Unit, onMedicalIdClick: () -> Unit) {
    val scrollState = rememberScrollState()
    val context = LocalContext.current
    val userId = FirebaseAuth.getInstance().currentUser?.uid
    
    var name by remember { mutableStateOf(AuthManager.currentUserFullName) }
    var email by remember { mutableStateOf(AuthManager.currentUserEmail) }
    
    // Phone parsing
    val initialPhone = AuthManager.currentUserPhone
    val initialCode = if (initialPhone.startsWith("+")) {
        if (initialPhone.length > 10) initialPhone.dropLast(10) else "+91"
    } else "+91"
    val initialNumber = if (initialPhone.length >= 10) initialPhone.takeLast(10) else initialPhone

    var phone by remember { mutableStateOf(initialNumber) }
    var selectedCountryCode by remember { mutableStateOf(initialCode) }
    var address by remember { mutableStateOf(AuthManager.currentUserAddress) }
    
    var photoUri by remember { mutableStateOf<Uri?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var countryExpanded by remember { mutableStateOf(false) }

    val countries = listOf(
        Pair("🇮🇳 India", "+91"), Pair("🇺🇸 USA", "+1"), Pair("🇬🇧 UK", "+44"),
        Pair("🇦🇺 Australia", "+61"), Pair("🇨🇦 Canada", "+1"), Pair("🇩🇪 Germany", "+49"), Pair("🇫🇷 France", "+33")
    )

    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri -> photoUri = uri }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SurfaceBase)
            .statusBarsPadding()
            .verticalScroll(scrollState)
            .padding(horizontal = 24.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        
        // TOP BAR
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color(0xFF001B3C))
            }
            Text("Edit Profile", fontFamily = LexendFont, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF001B3C))
            IconButton(onClick = {
                if (userId != null) {
                    isLoading = true
                    val fullPhone = "$selectedCountryCode$phone"
                    
                    if (photoUri != null) {
                        val ref = FirebaseStorage.getInstance().reference.child("profile_pics/$userId.jpg")
                        ref.putFile(photoUri!!).addOnSuccessListener {
                            ref.downloadUrl.addOnSuccessListener { uri ->
                                saveProfile(userId, name, email, fullPhone, address, uri.toString()) {
                                    isLoading = false
                                    onBackClick()
                                }
                            }
                        }.addOnFailureListener {
                            isLoading = false
                            Toast.makeText(context, "Upload failed", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        saveProfile(userId, name, email, fullPhone, address, AuthManager.currentUserProfilePic) {
                            isLoading = false
                            onBackClick()
                        }
                    }
                }
            }, enabled = !isLoading) {
                if (isLoading) CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                else Icon(Icons.Default.Check, "Save", tint = Color(0xFF001B3C))
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // PROFILE IMAGE SECTION
        Box(modifier = Modifier.align(Alignment.CenterHorizontally)) {
            Box(modifier = Modifier.size(120.dp).clip(CircleShape).border(4.dp, Color.White, CircleShape).clickable { imagePicker.launch("image/*") }) {
                if (photoUri != null) {
                    AsyncImage(model = photoUri, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                } else if (!AuthManager.currentUserProfilePic.isNullOrEmpty()) {
                    AsyncImage(model = AuthManager.currentUserProfilePic, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                } else {
                    Box(modifier = Modifier.fillMaxSize().background(Color(0xFFE0E7FF)), contentAlignment = Alignment.Center) {
                        Text(AuthManager.currentUserInitials, fontFamily = PlusJakartaSansFont, fontSize = 40.sp, fontWeight = FontWeight.Bold, color = Color(0xFF001B3C))
                    }
                }
            }
            Surface(
                modifier = Modifier.align(Alignment.BottomEnd).size(32.dp),
                shape = CircleShape, color = Color(0xFF001B3C), border = BorderStroke(2.dp, Color.White)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Edit, null, tint = Color.White, modifier = Modifier.size(14.dp))
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        Surface(
            modifier = Modifier.align(Alignment.CenterHorizontally).clickable { imagePicker.launch("image/*") },
            color = Color(0xFFD6E3FF).copy(alpha = 0.5f), shape = RoundedCornerShape(50.dp)
        ) {
            Text("CHANGE PHOTO", fontFamily = PlusJakartaSansFont, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF001B3C))
        }

        Spacer(modifier = Modifier.height(40.dp))

        // PERSONAL INFORMATION
        SectionHeader("PERSONAL INFORMATION")
        EditProfileField(label = "Full Name", value = name, onValueChange = { name = it })
        EditProfileField(label = "Email Address", value = email, onValueChange = { email = it })
        
        // Phone Number with Country Code
        Column(modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp)) {
            Text("Phone Number", fontFamily = PlusJakartaSansFont, fontSize = 12.sp, color = Color(0xFF7089B3), fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                ExposedDropdownMenuBox(expanded = countryExpanded, onExpandedChange = { countryExpanded = it }, modifier = Modifier.width(90.dp)) {
                    TextField(
                        value = selectedCountryCode, onValueChange = {}, readOnly = true,
                        modifier = Modifier.menuAnchor(type = ExposedDropdownMenuAnchorType.PrimaryNotEditable),
                        colors = TextFieldDefaults.colors(focusedContainerColor = Color(0xFFF1F5FF).copy(alpha = 0.5f), unfocusedContainerColor = Color(0xFFF1F5FF).copy(alpha = 0.5f), focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent),
                        textStyle = MaterialTheme.typography.bodyMedium.copy(fontFamily = PlusJakartaSansFont, fontWeight = FontWeight.Bold)
                    )
                    ExposedDropdownMenu(expanded = countryExpanded, onDismissRequest = { countryExpanded = false }) {
                        countries.forEach { (c, code) ->
                            DropdownMenuItem(text = { Text("$c ($code)", fontFamily = PlusJakartaSansFont) }, onClick = { selectedCountryCode = code; countryExpanded = false })
                        }
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
                TextField(
                    value = phone, onValueChange = { if (it.length <= 10 && it.all { c -> c.isDigit() }) phone = it },
                    modifier = Modifier.weight(1f).height(56.dp),
                    colors = TextFieldDefaults.colors(focusedContainerColor = Color(0xFFF1F5FF).copy(alpha = 0.5f), unfocusedContainerColor = Color(0xFFF1F5FF).copy(alpha = 0.5f), focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    placeholder = { Text("10-digit number", fontFamily = PlusJakartaSansFont, fontSize = 14.sp) },
                    shape = RoundedCornerShape(16.dp),
                    textStyle = MaterialTheme.typography.bodyLarge.copy(fontFamily = PlusJakartaSansFont)
                )
            }
        }
        
        EditProfileField(label = "Home Address", value = address, onValueChange = { address = it })

        Spacer(modifier = Modifier.height(32.dp))

        // DIGITAL MEDICAL ID CARD
        Surface(modifier = Modifier.fillMaxWidth(), color = Color(0xFFF1F5FF), shape = RoundedCornerShape(24.dp)) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column {
                        Text("Digital Medical ID", fontFamily = LexendFont, fontSize = 18.sp, fontWeight = FontWeight.Black, color = Color(0xFF001B3C))
                        Text("Summary for emergency responders", fontFamily = PlusJakartaSansFont, fontSize = 12.sp, color = Color(0xFF7089B3))
                    }
                    Surface(onClick = onMedicalIdClick, color = Color(0xFFD6E3FF), shape = RoundedCornerShape(12.dp)) {
                        Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Edit, null, modifier = Modifier.size(12.dp), tint = Color(0xFF001B3C))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Edit Details", fontFamily = PlusJakartaSansFont, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF001B3C))
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    MedicalSummaryItem("BLOOD TYPE", "O Positive", Color.Red, modifier = Modifier.weight(1f))
                    MedicalSummaryItem("ALLERGIES", "Penicillin, Shellfish", Color.Black, modifier = Modifier.weight(1f))
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // SECTION HEADER AND TOGGLES
        SectionHeader("EMERGENCY SETTINGS")
        EmergencySettingToggle(title = "Automatic SOS", subtitle = "Crash detection alert services", icon = Icons.Default.Emergency, checked = true, iconColor = Color.Red)
        EmergencySettingToggle(title = "Share Location", subtitle = "Broadcast to trusted contacts", icon = Icons.Default.LocationOn, checked = true, iconColor = Color(0xFF001B3C))
        EmergencySettingToggle(title = "Responder Access", subtitle = "Share Medical ID with first responders", icon = Icons.Default.MedicalServices, checked = false, iconColor = Color(0xFF7089B3))

        Spacer(modifier = Modifier.height(32.dp))

        SectionHeader("DOCUMENT CENTER")
        DocumentItem(title = "Insurance ID", subtitle = "Uploaded Oct 12, 2023", icon = Icons.Default.Shield, actionText = "Update File")
        Spacer(modifier = Modifier.height(16.dp))
        DocumentItem(title = "Advance Directive", subtitle = "Not uploaded yet", icon = Icons.Default.Description, actionText = "Add Document", isPrimaryAction = true)

        Spacer(modifier = Modifier.height(48.dp))
        
        Button(
            onClick = {
                if (userId != null) {
                    isLoading = true
                    saveProfile(userId, name, email, "$selectedCountryCode$phone", address, AuthManager.currentUserProfilePic) {
                        isLoading = false
                        onBackClick()
                    }
                }
            },
            modifier = Modifier.fillMaxWidth().height(64.dp),
            shape = RoundedCornerShape(20.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00091B)),
            enabled = !isLoading
        ) {
            Text("Save Profile", fontFamily = PlusJakartaSansFont, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
        
        Spacer(modifier = Modifier.height(140.dp))
    }
}

private fun saveProfile(uid: String, name: String, email: String, phone: String, address: String, photo: String?, onDone: () -> Unit) {
    val data = hashMapOf(
        "fullName" to name,
        "email" to email,
        "phone" to phone,
        "address" to address,
        "profilePic" to photo
    )
    FirebaseFirestore.getInstance().collection("Users").document(uid).update(data as Map<String, Any>)
        .addOnSuccessListener { onDone() }
}

@Composable
fun SectionHeader(title: String) {
    Text(
        text = title,
        fontFamily = PlusJakartaSansFont,
        fontSize = 11.sp,
        fontWeight = FontWeight.Black,
        color = Color(0xFF7089B3),
        letterSpacing = 1.sp,
        modifier = Modifier.padding(bottom = 16.dp)
    )
}

@Composable
fun EditProfileField(label: String, value: String, onValueChange: (String) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp)) {
        Text(label, fontFamily = PlusJakartaSansFont, fontSize = 12.sp, color = Color(0xFF7089B3), fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        TextField(
            value = value, onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = TextFieldDefaults.colors(focusedContainerColor = Color(0xFFF1F5FF).copy(alpha = 0.5f), unfocusedContainerColor = Color(0xFFF1F5FF).copy(alpha = 0.5f), focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent),
            shape = RoundedCornerShape(16.dp),
            singleLine = true,
            textStyle = MaterialTheme.typography.bodyLarge.copy(fontFamily = PlusJakartaSansFont)
        )
    }
}

@Composable
fun MedicalSummaryItem(label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        color = Color.White,
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(label, fontFamily = PlusJakartaSansFont, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF7089B3))
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, fontFamily = PlusJakartaSansFont, fontSize = 14.sp, fontWeight = FontWeight.Black, color = color)
        }
    }
}

@Composable
fun EmergencySettingToggle(title: String, subtitle: String, icon: ImageVector, checked: Boolean, iconColor: Color) {
    var isChecked by remember { mutableStateOf(checked) }
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
        Surface(modifier = Modifier.size(44.dp), shape = CircleShape, color = iconColor.copy(alpha = 0.1f)) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, null, tint = iconColor, modifier = Modifier.size(20.dp))
            }
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontFamily = PlusJakartaSansFont, fontWeight = FontWeight.Bold, color = Color(0xFF001B3C), fontSize = 15.sp)
            Text(subtitle, fontFamily = PlusJakartaSansFont, fontSize = 11.sp, color = Color(0xFF7089B3))
        }
        Switch(
            checked = isChecked, 
            onCheckedChange = { isChecked = it },
            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = Color(0xFF001B3C))
        )
    }
}

@Composable
fun DocumentItem(title: String, subtitle: String, icon: ImageVector, actionText: String, isPrimaryAction: Boolean = false) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White,
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, Color(0xFFF1F5FF))
    ) {
        Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, null, tint = Color(0xFF001B3C), modifier = Modifier.size(32.dp))
            Spacer(modifier = Modifier.height(12.dp))
            Text(title, fontFamily = PlusJakartaSansFont, fontWeight = FontWeight.Black, color = Color(0xFF001B3C), fontSize = 15.sp)
            Text(subtitle, fontFamily = PlusJakartaSansFont, fontSize = 12.sp, color = Color(0xFF7089B3))
            Spacer(modifier = Modifier.height(16.dp))
            if (isPrimaryAction) {
                Button(
                    onClick = { },
                    modifier = Modifier.height(36.dp),
                    shape = RoundedCornerShape(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00091B))
                ) {
                    Text(actionText, fontFamily = PlusJakartaSansFont, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            } else {
                Text(
                    text = actionText,
                    fontFamily = PlusJakartaSansFont,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF001B3C),
                    textDecoration = androidx.compose.ui.text.style.TextDecoration.Underline,
                    modifier = Modifier.clickable { }
                )
            }
        }
    }
}
