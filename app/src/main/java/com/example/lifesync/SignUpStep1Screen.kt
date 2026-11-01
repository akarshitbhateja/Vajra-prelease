package com.example.lifesync

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.lifesync.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUpStep1Screen(onNextClick: () -> Unit, onBackClick: () -> Unit) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var profileImageUri by remember { mutableStateOf<Uri?>(null) }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? -> profileImageUri = uri }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SurfaceBase)
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 28.dp)
    ) {
        Box(modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp)) {
            IconButton(onClick = onBackClick, modifier = Modifier.align(Alignment.CenterStart)) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = PrimaryContainer, modifier = Modifier.size(28.dp))
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Surface(color = SurfaceContainerHighest, shape = RoundedCornerShape(50.dp)) {
            Text("STEP 01 — IDENTITY SETUP", fontFamily = PlusJakartaSansFont, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp, color = OnPrimaryContainer, modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp))
        }

        Spacer(modifier = Modifier.height(20.dp))
        Text("Create Account", fontFamily = LexendFont, fontSize = 38.sp, fontWeight = FontWeight.ExtraBold, color = PrimaryContainer)
        Spacer(modifier = Modifier.height(12.dp))
        Text("Establish your secure identity within the LifeSync emergency network.", fontFamily = PlusJakartaSansFont, fontSize = 15.sp, color = OnSurfaceVariant, lineHeight = 24.sp)

        Spacer(modifier = Modifier.height(40.dp))

        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            Box(
                modifier = Modifier
                    .size(130.dp)
                    .clip(CircleShape)
                    .background(SurfaceContainerLow)
                    .border(3.dp, Brush.linearGradient(listOf(PrimaryContainer, Color(0xFF7089B3))), CircleShape)
                    .clickable { imagePickerLauncher.launch("image/*") },
                contentAlignment = Alignment.Center
            ) {
                if (profileImageUri != null) {
                    AsyncImage(
                        model = profileImageUri,
                        contentDescription = "Profile Picture",
                        modifier = Modifier.fillMaxSize().clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.AddAPhoto, contentDescription = null, tint = OnSurfaceVariant, modifier = Modifier.size(36.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Add Photo", fontFamily = PlusJakartaSansFont, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = OnSurfaceVariant)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(48.dp))

        SignUpField(label = "FULL NAME", value = name, onValueChange = { name = it }, icon = Icons.Default.Person, placeholder = "Enter your legal name")
        Spacer(modifier = Modifier.height(24.dp))
        SignUpField(label = "EMAIL ADDRESS", value = email, onValueChange = { email = it }, icon = Icons.Default.Email, placeholder = "name@example.com")
        Spacer(modifier = Modifier.height(24.dp))
        SignUpField(label = "PASSWORD", value = password, onValueChange = { password = it }, icon = Icons.Default.Lock, placeholder = "Minimum 6 characters", isPassword = true, passwordVisible = passwordVisible, onToggleVisibility = { passwordVisible = !passwordVisible })
        Spacer(modifier = Modifier.height(24.dp))
        SignUpField(label = "CONFIRM PASSWORD", value = confirmPassword, onValueChange = { confirmPassword = it }, icon = Icons.Default.Lock, placeholder = "Re-enter your password", isPassword = true, passwordVisible = confirmPasswordVisible, onToggleVisibility = { confirmPasswordVisible = !confirmPasswordVisible })

        Spacer(modifier = Modifier.height(56.dp))

        Button(
            onClick = {
                if (name.isBlank() || email.isBlank() || password.isBlank()) {
                    Toast.makeText(context, "Kindly fill in all mandatory fields.", Toast.LENGTH_SHORT).show()
                    return@Button
                }
                if (password != confirmPassword) {
                    Toast.makeText(context, "Passwords do not match. Kindly verify.", Toast.LENGTH_SHORT).show()
                    return@Button
                }
                if (password.length < 6) {
                    Toast.makeText(context, "Password requires a minimum of 6 characters.", Toast.LENGTH_SHORT).show()
                    return@Button
                }

                isLoading = true
                AuthManager.auth.createUserWithEmailAndPassword(email.trim(), password.trim())
                    .addOnSuccessListener { authResult ->
                        val userId = authResult.user?.uid ?: ""
                        if (profileImageUri != null) {
                            val ref = AuthManager.storage.reference.child("profile_pics/$userId.jpg")
                            ref.putFile(profileImageUri!!)
                                .addOnSuccessListener {
                                    ref.downloadUrl.addOnSuccessListener { downloadUri ->
                                        saveUserData(userId, name, email, downloadUri.toString(), onNextClick) { isLoading = false; Toast.makeText(context, it, Toast.LENGTH_SHORT).show() }
                                    }
                                }
                                .addOnFailureListener {
                                    saveUserData(userId, name, email, null, onNextClick) { isLoading = false; Toast.makeText(context, "Image upload failed. Proceeding without photo.", Toast.LENGTH_SHORT).show() }
                                }
                        } else {
                            saveUserData(userId, name, email, null, onNextClick) { isLoading = false; Toast.makeText(context, it, Toast.LENGTH_SHORT).show() }
                        }
                    }
                    .addOnFailureListener { exception ->
                        isLoading = false
                        Toast.makeText(context, "Registration failed: ${exception.message}", Toast.LENGTH_LONG).show()
                    }
            },
            modifier = Modifier.fillMaxWidth().height(64.dp),
            shape = RoundedCornerShape(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Primary),
            enabled = !isLoading
        ) {
            if (isLoading) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp), strokeWidth = 2.dp) else Text("Proceed to Step 2", fontFamily = PlusJakartaSansFont, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }
        Spacer(modifier = Modifier.height(48.dp))
    }
}

private fun saveUserData(userId: String, name: String, email: String, photoUrl: String?, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
    val userMap = hashMapOf("fullName" to name.trim(), "email" to email.trim(), "profilePic" to photoUrl, "createdAt" to com.google.firebase.Timestamp.now())
    AuthManager.db.collection("Users").document(userId).set(userMap)
        .addOnSuccessListener { AuthManager.fetchUserProfile(); onSuccess() }
        .addOnFailureListener { onFailure("System error saving profile: ${it.message}") }
}

@Composable
fun SignUpField(label: String, value: String, onValueChange: (String) -> Unit, icon: androidx.compose.ui.graphics.vector.ImageVector, placeholder: String, isPassword: Boolean = false, passwordVisible: Boolean = false, onToggleVisibility: () -> Unit = {}) {
    Column {
        Text(label, fontFamily = PlusJakartaSansFont, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp, color = OnSurfaceVariant)
        Spacer(modifier = Modifier.height(10.dp))
        TextField(
            value = value, onValueChange = onValueChange, modifier = Modifier.fillMaxWidth().height(60.dp).clip(RoundedCornerShape(20.dp)),
            colors = TextFieldDefaults.colors(focusedContainerColor = SurfaceContainerLow, unfocusedContainerColor = SurfaceContainerLow, focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent, focusedTextColor = OnSurface, unfocusedTextColor = OnSurface),
            leadingIcon = { Icon(icon, contentDescription = null, tint = Primary, modifier = Modifier.size(22.dp)) },
            trailingIcon = if (isPassword) { { IconButton(onClick = onToggleVisibility) { Icon(if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff, null, tint = OnSurfaceVariant) } } } else null,
            visualTransformation = if (isPassword && !passwordVisible) PasswordVisualTransformation() else VisualTransformation.None,
            placeholder = { Text(placeholder, fontFamily = PlusJakartaSansFont, color = OnSurfaceVariant.copy(alpha = 0.5f), fontSize = 15.sp) },
            singleLine = true,
            textStyle = MaterialTheme.typography.bodyLarge.copy(fontFamily = PlusJakartaSansFont)
        )
    }
}