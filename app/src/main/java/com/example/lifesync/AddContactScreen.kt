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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.lifesync.ui.theme.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddContactScreen(onBackClick: () -> Unit) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var selectedCountryCode by remember { mutableStateOf("+91") }
    var relationship by remember { mutableStateOf("Guardian") }
    var photoUri by remember { mutableStateOf<Uri?>(null) }
    var emergencyAlerts by remember { mutableStateOf(true) }
    var locationSharing by remember { mutableStateOf(true) }
    var isPrimary by remember { mutableStateOf(false) }

    var isLoading by remember { mutableStateOf(false) }
    var relExpanded by remember { mutableStateOf(false) }
    var countryExpanded by remember { mutableStateOf(false) }

    val countries = listOf(
        Pair("🇮🇳", "+91"), Pair("🇺🇸", "+1"), Pair("🇬🇧", "+44"),
        Pair("🇦🇺", "+61"), Pair("🇨🇦", "+1"), Pair("🇩🇪", "+49"), Pair("🇫🇷", "+33")
    )

    val relations = listOf("Wife", "Husband", "Mother", "Father", "Brother", "Sister", "Doctor", "Friend", "Colleague")
    val context = LocalContext.current
    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri -> photoUri = uri }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SurfaceBase)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBackClick) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color(0xFF001B3C))
            }
            Text("Add Emergency Contact", fontFamily = LexendFont, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF001B3C))
        }

        Spacer(modifier = Modifier.height(48.dp))

        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            Box(contentAlignment = Alignment.BottomEnd) {
                Surface(
                    modifier = Modifier.size(140.dp).clip(CircleShape).clickable { imagePicker.launch("image/*") },
                    color = Color(0xFFE0E7FF), shape = CircleShape
                ) {
                    if (photoUri != null) {
                        AsyncImage(model = photoUri, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                    } else {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Person, null, modifier = Modifier.size(64.dp), tint = Color(0xFF001B3C).copy(alpha = 0.3f))
                        }
                    }
                }
                Surface(
                    modifier = Modifier.size(40.dp).offset(x = (-8).dp, y = (-8).dp),
                    color = Color(0xFF001B3C), shape = CircleShape, border = BorderStroke(2.dp, Color.White)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.AddAPhoto, null, tint = Color.White, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(48.dp))

        Text("FULL NAME", fontFamily = PlusJakartaSansFont, fontSize = 11.sp, fontWeight = FontWeight.Black, color = Color(0xFF44474E), letterSpacing = 1.sp)
        Spacer(modifier = Modifier.height(8.dp))
        TextField(
            value = name, onValueChange = { name = it },
            modifier = Modifier.fillMaxWidth().height(56.dp).clip(RoundedCornerShape(16.dp)),
            colors = TextFieldDefaults.colors(focusedContainerColor = Color(0xFFF1F5FF), unfocusedContainerColor = Color(0xFFF1F5FF), focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent),
            placeholder = { Text("Enter legal name", fontFamily = PlusJakartaSansFont, color = Color(0xFF7089B3).copy(alpha = 0.5f)) }, singleLine = true,
            textStyle = MaterialTheme.typography.bodyLarge.copy(fontFamily = PlusJakartaSansFont)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text("RELATIONSHIP", fontFamily = PlusJakartaSansFont, fontSize = 11.sp, fontWeight = FontWeight.Black, color = Color(0xFF44474E), letterSpacing = 1.sp)
        Spacer(modifier = Modifier.height(8.dp))
        ExposedDropdownMenuBox(expanded = relExpanded, onExpandedChange = { relExpanded = it }) {
            TextField(
                value = relationship, onValueChange = {}, readOnly = true,
                modifier = Modifier.fillMaxWidth().height(56.dp).clip(RoundedCornerShape(16.dp)).menuAnchor(type = ExposedDropdownMenuAnchorType.PrimaryNotEditable),
                colors = TextFieldDefaults.colors(focusedContainerColor = Color(0xFFF1F5FF), unfocusedContainerColor = Color(0xFFF1F5FF), focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent),
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = relExpanded) },
                textStyle = MaterialTheme.typography.bodyLarge.copy(fontFamily = PlusJakartaSansFont, textAlign = TextAlign.Start, fontWeight = FontWeight.SemiBold)
            )
            ExposedDropdownMenu(expanded = relExpanded, onDismissRequest = { relExpanded = false }) {
                relations.forEach { rel -> DropdownMenuItem(text = { Text(rel, fontFamily = PlusJakartaSansFont, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Start) }, onClick = { relationship = rel; relExpanded = false }) }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text("PHONE NUMBER", fontFamily = PlusJakartaSansFont, fontSize = 11.sp, fontWeight = FontWeight.Black, color = Color(0xFF44474E), letterSpacing = 1.sp)
        Spacer(modifier = Modifier.height(8.dp))

        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            ExposedDropdownMenuBox(expanded = countryExpanded, onExpandedChange = { countryExpanded = it }, modifier = Modifier.width(135.dp)) {
                val flag = countries.find { it.second == selectedCountryCode }?.first ?: ""
                TextField(
                    value = "$flag $selectedCountryCode", onValueChange = {}, readOnly = true,
                    modifier = Modifier.menuAnchor(type = ExposedDropdownMenuAnchorType.PrimaryNotEditable).fillMaxWidth().height(64.dp).clip(RoundedCornerShape(20.dp)),
                    colors = TextFieldDefaults.colors(focusedContainerColor = Color(0xFFF1F5FF), unfocusedContainerColor = Color(0xFFF1F5FF), focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent),
                    textStyle = MaterialTheme.typography.bodyLarge.copy(fontFamily = PlusJakartaSansFont, fontWeight = FontWeight.Bold, color = Color(0xFF001B3C), textAlign = TextAlign.Start)
                )
                ExposedDropdownMenu(expanded = countryExpanded, onDismissRequest = { countryExpanded = false }) {
                    countries.forEach { (flagIcon, code) -> DropdownMenuItem(text = { Text("$flagIcon ($code)", fontFamily = PlusJakartaSansFont, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Start) }, onClick = { selectedCountryCode = code; countryExpanded = false }) }
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
            TextField(
                value = phone, onValueChange = { if (it.length <= 10 && it.all { char -> char.isDigit() }) phone = it },
                modifier = Modifier.weight(1f).height(64.dp).clip(RoundedCornerShape(20.dp)),
                colors = TextFieldDefaults.colors(focusedContainerColor = Color(0xFFF1F5FF), unfocusedContainerColor = Color(0xFFF1F5FF), focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent),
                placeholder = { Text("10-digit number", fontFamily = PlusJakartaSansFont, color = Color(0xFF7089B3).copy(alpha = 0.5f)) }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true,
                textStyle = MaterialTheme.typography.bodyLarge.copy(fontFamily = PlusJakartaSansFont)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        EditToggleCard(icon = Icons.Default.Notifications, title = "Emergency Alerts", subtitle = "Notify during SOS events", checked = emergencyAlerts, onCheckedChange = { emergencyAlerts = it })
        Spacer(modifier = Modifier.height(16.dp))
        EditToggleCard(icon = Icons.Default.LocationOn, title = "Live Location Sharing", subtitle = "Visible during active VAJRA sync", checked = locationSharing, onCheckedChange = { locationSharing = it })
        Spacer(modifier = Modifier.height(32.dp))

        Surface(
            modifier = Modifier.fillMaxWidth().height(80.dp).clickable { isPrimary = !isPrimary },
            color = if (isPrimary) Color(0xFF001B3C) else Color(0xFFF1F5FF),
            shape = RoundedCornerShape(24.dp)
        ) {
            Row(modifier = Modifier.padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(44.dp).clip(CircleShape).background(if (isPrimary) Color.White.copy(alpha = 0.2f) else Color.White), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.VerifiedUser, null, tint = if (isPrimary) Color.White else Color(0xFF001B3C), modifier = Modifier.size(20.dp))
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("Primary Guardian", fontFamily = PlusJakartaSansFont, fontWeight = FontWeight.Bold, color = if (isPrimary) Color.White else Color(0xFF001B3C), fontSize = 15.sp)
                    Text("First contact to be reached", fontFamily = PlusJakartaSansFont, fontSize = 12.sp, color = if (isPrimary) Color.White.copy(alpha = 0.7f) else Color(0xFF7089B3))
                }
                Checkbox(checked = isPrimary, onCheckedChange = { isPrimary = it }, colors = CheckboxDefaults.colors(checkedColor = Color.White, checkmarkColor = Color(0xFF001B3C), uncheckedColor = Color(0xFF001B3C).copy(alpha = 0.3f)))
            }
        }

        Spacer(modifier = Modifier.height(48.dp))

        Button(
            onClick = {
                if (name.isBlank() || phone.length != 10) {
                    Toast.makeText(context, "Please enter a valid name and 10-digit phone number", Toast.LENGTH_SHORT).show()
                    return@Button
                }
                isLoading = true
                val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return@Button
                val contactId = UUID.randomUUID().toString()
                val fullPhone = "$selectedCountryCode$phone"

                if (photoUri != null) {
                    val ref = FirebaseStorage.getInstance().reference.child("contacts/$contactId.jpg")
                    ref.putFile(photoUri!!).addOnSuccessListener {
                        ref.downloadUrl.addOnSuccessListener { uri ->
                            saveToFirestore(userId, contactId, name, relationship, fullPhone, uri.toString(), isPrimary, { isLoading = false; onBackClick() })
                        }
                    }
                } else {
                    saveToFirestore(userId, contactId, name, relationship, fullPhone, null, isPrimary, { isLoading = false; onBackClick() })
                }
            },
            modifier = Modifier.fillMaxWidth().height(64.dp), shape = RoundedCornerShape(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Primary), enabled = !isLoading
        ) {
            if (isLoading) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
            else Text("Add Contact", fontFamily = PlusJakartaSansFont, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        }
        Spacer(modifier = Modifier.height(140.dp))
    }
}

private fun saveToFirestore(userId: String, contactId: String, name: String, relation: String, phone: String, photo: String?, isPrimary: Boolean, onDone: () -> Unit) {
    val db = FirebaseFirestore.getInstance()
    val userContactsRef = db.collection("Users").document(userId).collection("contacts")

    val data = hashMapOf("name" to name, "relation" to relation, "phone" to phone, "photoUrl" to photo, "isPrimaryGuardian" to isPrimary)

    if (isPrimary) {
        userContactsRef.whereEqualTo("isPrimaryGuardian", true).get().addOnSuccessListener { snapshot ->
            val batch = db.batch()
            snapshot.documents.forEach { doc ->
                batch.update(doc.reference, "isPrimaryGuardian", false)
            }
            batch.set(userContactsRef.document(contactId), data)
            batch.commit().addOnSuccessListener { onDone() }
        }
    } else {
        userContactsRef.document(contactId).set(data).addOnSuccessListener { onDone() }
    }
}
