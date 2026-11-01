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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditContactScreen(contact: Contact, onBackClick: () -> Unit) {
    var name by remember { mutableStateOf(contact.name) }

    val initialCode = if (contact.phone.startsWith("+")) {
        if (contact.phone.length > 10) contact.phone.dropLast(10) else "+91"
    } else "+91"
    val initialNumber = if (contact.phone.length >= 10) contact.phone.takeLast(10) else contact.phone

    var phone by remember { mutableStateOf(initialNumber) }
    var selectedCountryCode by remember { mutableStateOf(initialCode) }

    var relationship by remember { mutableStateOf(contact.relation) }
    var photoUri by remember { mutableStateOf<Uri?>(null) }
    var emergencyAlerts by remember { mutableStateOf(true) }
    var locationSharing by remember { mutableStateOf(true) }
    var isPrimary by remember { mutableStateOf(contact.isPrimaryGuardian) }

    var isLoading by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var relExpanded by remember { mutableStateOf(false) }
    var countryExpanded by remember { mutableStateOf(false) }

    val countries = listOf(
        Pair("🇮🇳", "+91"), Pair("🇺🇸", "+1"), Pair("🇬🇧", "+44"),
        Pair("🇦🇺", "+61"), Pair("🇨🇦", "+1")
    )

    val relations = listOf("Wife", "Husband", "Mother", "Father", "Brother", "Sister", "Doctor", "Friend", "Colleague")
    val context = LocalContext.current
    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri -> photoUri = uri }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Contact", fontFamily = LexendFont) },
            text = { Text("Are you sure you want to delete this contact?", fontFamily = PlusJakartaSansFont) },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    isLoading = true
                    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return@TextButton
                    FirebaseFirestore.getInstance().collection("Users").document(userId)
                        .collection("contacts").document(contact.id).delete()
                        .addOnSuccessListener {
                            if (!contact.photoUrl.isNullOrEmpty()) {
                                try { FirebaseStorage.getInstance().getReferenceFromUrl(contact.photoUrl).delete() } catch (e: Exception) {}
                            }
                            isLoading = false
                            onBackClick()
                        }
                }) {
                    Text("DELETE", color = Color.Red, fontWeight = FontWeight.Bold, fontFamily = PlusJakartaSansFont)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("CANCEL", color = Primary, fontFamily = PlusJakartaSansFont)
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF9F9FF))
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
    ) {
        Box(modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp, horizontal = 24.dp)) {
            IconButton(onClick = onBackClick, modifier = Modifier.align(Alignment.CenterStart)) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Primary)
            }
            Text("Edit Contact", fontFamily = LexendFont, style = MaterialTheme.typography.titleLarge.copy(fontSize = 20.sp, fontWeight = FontWeight.Bold), color = Primary, modifier = Modifier.align(Alignment.Center))
            Box(modifier = Modifier.align(Alignment.CenterEnd)) {
                UserProfileAvatar(modifier = Modifier.size(44.dp).shadow(4.dp, CircleShape))
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            Box(contentAlignment = Alignment.BottomEnd) {
                Surface(
                    modifier = Modifier.size(150.dp).clip(CircleShape).clickable { imagePicker.launch("image/*") },
                    color = Color(0xFFF1F5FF), shape = CircleShape, shadowElevation = 8.dp
                ) {
                    if (photoUri != null) {
                        AsyncImage(model = photoUri, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                    } else if (!contact.photoUrl.isNullOrEmpty()) {
                        AsyncImage(model = contact.photoUrl, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                    } else {
                        Box(contentAlignment = Alignment.Center) {
                            Text(name.take(1).uppercase(), fontFamily = PlusJakartaSansFont, fontSize = 48.sp, fontWeight = FontWeight.Bold, color = Primary)
                        }
                    }
                }
                Surface(
                    modifier = Modifier.size(44.dp).offset(x = (-4).dp, y = (-4).dp),
                    color = Primary, shape = CircleShape, border = BorderStroke(3.dp, Color.White)
                ) {
                    Box(contentAlignment = Alignment.Center) { Icon(Icons.Default.Edit, null, tint = Color.White, modifier = Modifier.size(20.dp)) }
                }
            }
        }

        Spacer(modifier = Modifier.height(40.dp))

        Column(modifier = Modifier.padding(horizontal = 24.dp)) {
            Text("CONTACT NAME", fontFamily = PlusJakartaSansFont, style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold), color = Primary, letterSpacing = 1.sp)
            Spacer(modifier = Modifier.height(8.dp))
            TextField(
                value = name, onValueChange = { name = it },
                modifier = Modifier.fillMaxWidth().height(64.dp).clip(RoundedCornerShape(20.dp)),
                colors = TextFieldDefaults.colors(focusedContainerColor = SoftLilac.copy(alpha = 0.3f), unfocusedContainerColor = SoftLilac.copy(alpha = 0.3f), focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent),
                singleLine = true, textStyle = MaterialTheme.typography.bodyLarge.copy(fontFamily = PlusJakartaSansFont, fontWeight = FontWeight.SemiBold)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text("PHONE NUMBER", fontFamily = PlusJakartaSansFont, style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold), color = Primary, letterSpacing = 1.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                ExposedDropdownMenuBox(expanded = countryExpanded, onExpandedChange = { countryExpanded = it }, modifier = Modifier.width(140.dp)) {
                    val flag = countries.find { it.second == selectedCountryCode }?.first ?: ""
                    TextField(
                        value = selectedCountryCode, onValueChange = {}, readOnly = true,
                        modifier = Modifier.menuAnchor(type = ExposedDropdownMenuAnchorType.PrimaryNotEditable).fillMaxWidth().height(64.dp).clip(RoundedCornerShape(20.dp)),
                        colors = TextFieldDefaults.colors(focusedContainerColor = SoftLilac.copy(alpha = 0.3f), unfocusedContainerColor = SoftLilac.copy(alpha = 0.3f), focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent),
                        leadingIcon = { Box(modifier = Modifier.padding(start = 12.dp), contentAlignment = Alignment.Center) { Text(flag, fontSize = 18.sp) } },
                        trailingIcon = { Icon(Icons.Default.ArrowDropDown, null, tint = Primary) },
                        textStyle = MaterialTheme.typography.bodyLarge.copy(fontFamily = PlusJakartaSansFont, fontWeight = FontWeight.Bold, textAlign = TextAlign.Start), singleLine = true
                    )
                    ExposedDropdownMenu(expanded = countryExpanded, onDismissRequest = { countryExpanded = false }) {
                        countries.forEach { (flagIcon, code) -> DropdownMenuItem(text = { Text("$flagIcon $code", fontFamily = PlusJakartaSansFont, textAlign = TextAlign.Start) }, onClick = { selectedCountryCode = code; countryExpanded = false }) }
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                TextField(
                    value = phone, onValueChange = { if (it.length <= 10 && it.all { c -> c.isDigit() }) phone = it },
                    modifier = Modifier.weight(1f).height(64.dp).clip(RoundedCornerShape(20.dp)),
                    colors = TextFieldDefaults.colors(focusedContainerColor = SoftLilac.copy(alpha = 0.3f), unfocusedContainerColor = SoftLilac.copy(alpha = 0.3f), focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent),
                    leadingIcon = { Icon(Icons.Default.Phone, null, tint = Primary.copy(alpha = 0.5f), modifier = Modifier.size(20.dp)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true, textStyle = MaterialTheme.typography.bodyLarge.copy(fontFamily = PlusJakartaSansFont, fontWeight = FontWeight.SemiBold)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text("RELATION", fontFamily = PlusJakartaSansFont, style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold), color = Primary, letterSpacing = 1.sp)
            Spacer(modifier = Modifier.height(8.dp))
            ExposedDropdownMenuBox(expanded = relExpanded, onExpandedChange = { relExpanded = it }) {
                TextField(
                    value = relationship, onValueChange = {}, readOnly = true,
                    modifier = Modifier.fillMaxWidth().height(64.dp).clip(RoundedCornerShape(20.dp)).menuAnchor(type = ExposedDropdownMenuAnchorType.PrimaryNotEditable),
                    colors = TextFieldDefaults.colors(focusedContainerColor = SoftLilac.copy(alpha = 0.3f), unfocusedContainerColor = SoftLilac.copy(alpha = 0.3f), focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent),
                    trailingIcon = { Icon(Icons.Default.UnfoldMore, null, tint = Primary) }, textStyle = MaterialTheme.typography.bodyLarge.copy(fontFamily = PlusJakartaSansFont, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Start), singleLine = true
                )
                ExposedDropdownMenu(expanded = relExpanded, onDismissRequest = { relExpanded = false }) {
                    relations.forEach { rel -> DropdownMenuItem(text = { Text(rel, fontFamily = PlusJakartaSansFont, textAlign = TextAlign.Start) }, onClick = { relationship = rel; relExpanded = false }) }
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            EditToggleCard(title = "Set as Primary Guardian", subtitle = "Sends immediate SOS alerts first", checked = isPrimary, onCheckedChange = { isPrimary = it })
            Spacer(modifier = Modifier.height(16.dp))
            EditToggleCard(title = "Emergency Alerts", subtitle = "Notify during SOS events", checked = emergencyAlerts, onCheckedChange = { emergencyAlerts = it })
            Spacer(modifier = Modifier.height(16.dp))
            EditToggleCard(title = "Live Location Sharing", subtitle = "Visible during active Sync", checked = locationSharing, onCheckedChange = { locationSharing = it })

            Spacer(modifier = Modifier.height(48.dp))

            Row(modifier = Modifier.fillMaxWidth().padding(bottom = 40.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Button(
                    onClick = { showDeleteDialog = true }, modifier = Modifier.weight(1f).height(64.dp), shape = RoundedCornerShape(32.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFEBEE), contentColor = Color(0xFFD32F2F))
                ) {
                    Icon(Icons.Default.Delete, null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Delete", fontFamily = PlusJakartaSansFont, fontWeight = FontWeight.Bold)
                }
                Button(
                    onClick = {
                        if (name.isBlank() || phone.length != 10) {
                            Toast.makeText(context, "Invalid input", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        isLoading = true
                        val userId = AuthManager.auth.currentUser?.uid ?: return@Button
                        val fullPhone = "$selectedCountryCode$phone"
                        if (photoUri != null) {
                            val ref = FirebaseStorage.getInstance().reference.child("contacts/${contact.id}.jpg")
                            ref.putFile(photoUri!!).addOnSuccessListener { ref.downloadUrl.addOnSuccessListener { uri ->
                                updateContactInFirestore(userId, contact.id, name, relationship, fullPhone, uri.toString(), isPrimary, { isLoading = false; onBackClick() })
                            }}
                        } else {
                            updateContactInFirestore(userId, contact.id, name, relationship, fullPhone, contact.photoUrl, isPrimary, { isLoading = false; onBackClick() })
                        }
                    },
                    modifier = Modifier.weight(1.5f).height(64.dp), shape = RoundedCornerShape(32.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Primary), enabled = !isLoading
                ) {
                    if (isLoading) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    else {
                        Icon(Icons.Default.Save, null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Save Changes", fontFamily = PlusJakartaSansFont, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(140.dp))
    }
}

@Composable
fun EditToggleCard(title: String, subtitle: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit, icon: ImageVector? = null) {
    Surface(
        modifier = Modifier.fillMaxWidth().height(90.dp),
        color = Color.White, shape = RoundedCornerShape(32.dp), shadowElevation = 2.dp
    ) {
        Row(modifier = Modifier.padding(horizontal = 24.dp), verticalAlignment = Alignment.CenterVertically) {
            if (icon != null) {
                Icon(icon, null, tint = Primary, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(16.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontFamily = PlusJakartaSansFont, fontWeight = FontWeight.Bold, color = Primary, fontSize = 16.sp)
                Text(subtitle, fontFamily = PlusJakartaSansFont, fontSize = 12.sp, color = Color.Gray)
            }
            Switch(
                checked = checked, onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = Primary, uncheckedTrackColor = Color(0xFFF1F5FF))
            )
        }
    }
}

private fun updateContactInFirestore(userId: String, contactId: String, name: String, relation: String, phone: String, photo: String?, isPrimary: Boolean, onDone: () -> Unit) {
    val db = FirebaseFirestore.getInstance()
    val userContactsRef = db.collection("Users").document(userId).collection("contacts")
    val data = hashMapOf("name" to name, "relation" to relation, "phone" to phone, "photoUrl" to photo, "isPrimaryGuardian" to isPrimary)

    if (isPrimary) {
        userContactsRef.whereEqualTo("isPrimaryGuardian", true).get().addOnSuccessListener { snapshot ->
            val batch = db.batch()
            snapshot.documents.forEach { doc ->
                if (doc.id != contactId) {
                    batch.update(doc.reference, "isPrimaryGuardian", false)
                }
            }
            batch.set(userContactsRef.document(contactId), data)
            batch.commit().addOnSuccessListener { onDone() }
        }
    } else {
        userContactsRef.document(contactId).set(data).addOnSuccessListener { onDone() }
    }
}
