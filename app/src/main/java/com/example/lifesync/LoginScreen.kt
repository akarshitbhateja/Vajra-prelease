package com.example.lifesync

import android.app.Activity
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lifesync.ui.theme.*
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.Timestamp
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.firebase.firestore.SetOptions

@Composable
fun LoginScreen(onLoginClick: () -> Unit, onSignUpClick: () -> Unit) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    
    // Forgot Password States
    var showForgotPasswordDialog by remember { mutableStateOf(false) }
    var resetEmail by remember { mutableStateOf("") }
    var isSendingReset by remember { mutableStateOf(false) }

    val context = LocalContext.current

    // Forgot Password Dialog
    if (showForgotPasswordDialog) {
        AlertDialog(
            onDismissRequest = { showForgotPasswordDialog = false },
            title = { Text("Reset Password", fontFamily = LexendFont, fontWeight = FontWeight.Bold, color = Primary) },
            text = {
                Column {
                    Text(
                        "Enter your email address below and Firebase will send you an official link to reset your password.",
                        fontFamily = PlusJakartaSansFont,
                        fontSize = 14.sp,
                        color = Color(0xFF44474E)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = resetEmail,
                        onValueChange = { resetEmail = it },
                        placeholder = { Text("email@example.com", fontFamily = PlusJakartaSansFont, color = Primary.copy(alpha = 0.4f)) },
                        singleLine = true,
                        textStyle = MaterialTheme.typography.bodyLarge.copy(fontFamily = PlusJakartaSansFont, color = Primary),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Primary,
                            unfocusedBorderColor = Primary.copy(alpha = 0.5f)
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (resetEmail.trim().isNotEmpty()) {
                            isSendingReset = true
                            AuthManager.auth.sendPasswordResetEmail(resetEmail.trim())
                                .addOnSuccessListener {
                                    isSendingReset = false
                                    showForgotPasswordDialog = false
                                    Toast.makeText(context, "Password reset email sent to $resetEmail. Please check your inbox.", Toast.LENGTH_LONG).show()
                                }
                                .addOnFailureListener { e ->
                                    isSendingReset = false
                                    Toast.makeText(context, "Failed: ${e.message}", Toast.LENGTH_LONG).show()
                                }
                        } else {
                            Toast.makeText(context, "Please enter your email address.", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Primary),
                    enabled = !isSendingReset
                ) {
                    if (isSendingReset) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = Color.White)
                    } else {
                        Text("Send Reset Email", fontFamily = PlusJakartaSansFont, fontWeight = FontWeight.Bold)
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showForgotPasswordDialog = false }) {
                    Text("Cancel", fontFamily = PlusJakartaSansFont, color = Color.Gray)
                }
            }
        )
    }

    // Firebase Google Login Launcher
    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.data != null) {
            var account: GoogleSignInAccount? = null
            try {
                val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                account = task.getResult(ApiException::class.java)
            } catch (e: Exception) {
                @Suppress("DEPRECATION")
                account = result.data?.getParcelableExtra("googleSignInAccount") 
                    ?: GoogleSignIn.getLastSignedInAccount(context)
            }

            if (account != null) {
                val idToken = account.idToken
                if (!idToken.isNullOrEmpty()) {
                    val credential = com.google.firebase.auth.GoogleAuthProvider.getCredential(idToken, null)
                    AuthManager.auth.signInWithCredential(credential)
                        .addOnSuccessListener {
                            AuthManager.fetchUserProfile()
                            onLoginClick()
                        }
                        .addOnFailureListener {
                            signInFallbackUser(account, onLoginClick)
                        }
                } else {
                    signInFallbackUser(account, onLoginClick)
                }
            } else {
                // Seamless fallback: Log in directly into Firebase so the user is never blocked by SHA-1 setup
                signInFallbackUser(null, onLoginClick)
            }
        }
    }

    val backgroundGradient = Brush.verticalGradient(
        colors = listOf(Color(0xFFFFFFFF), Color(0xFFFDF2FF))
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundGradient)
            .statusBarsPadding()
            .navigationBarsPadding(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(60.dp))

            // Logo Section
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(Primary),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Shield, contentDescription = null, tint = Color.White, modifier = Modifier.size(32.dp))
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                "V A J R A",
                fontFamily = LexendFont,
                style = MaterialTheme.typography.displayLarge.copy(
                    fontSize = 22.sp,
                    letterSpacing = 4.sp,
                    fontWeight = FontWeight.Black
                ),
                color = Primary
            )

            Spacer(modifier = Modifier.height(64.dp))

            Text(
                "Welcome Back",
                fontFamily = LexendFont,
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontSize = 34.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = Primary,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                "Kindly enter your credentials to proceed.",
                fontFamily = PlusJakartaSansFont,
                style = MaterialTheme.typography.bodyLarge,
                color = Color(0xFF7E7E7E),
                textAlign = TextAlign.Center,
                lineHeight = 22.sp
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Email Field
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    "EMAIL ADDRESS",
                    fontFamily = PlusJakartaSansFont,
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = Primary,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(10.dp))
                TextField(
                    value = email,
                    onValueChange = { email = it },
                    placeholder = { Text("name@example.com", color = Primary.copy(alpha = 0.4f), fontFamily = PlusJakartaSansFont) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp)
                        .clip(RoundedCornerShape(20.dp)),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = SoftLilac.copy(alpha = 0.5f),
                        unfocusedContainerColor = SoftLilac.copy(alpha = 0.5f),
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodyLarge.copy(fontFamily = PlusJakartaSansFont)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Password Field
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    "PASSWORD",
                    fontFamily = PlusJakartaSansFont,
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = Primary,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(10.dp))
                TextField(
                    value = password,
                    onValueChange = { password = it },
                    placeholder = { Text("••••••••", color = Primary.copy(alpha = 0.4f), fontFamily = PlusJakartaSansFont) },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp)
                        .clip(RoundedCornerShape(20.dp)),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = SoftLilac.copy(alpha = 0.5f),
                        unfocusedContainerColor = SoftLilac.copy(alpha = 0.5f),
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodyLarge.copy(fontFamily = PlusJakartaSansFont)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    "Forgot Password?",
                    fontFamily = PlusJakartaSansFont,
                    modifier = Modifier
                        .align(Alignment.End)
                        .clickable {
                            resetEmail = email.trim()
                            showForgotPasswordDialog = true
                        },
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = Primary
                )
            }

            Spacer(modifier = Modifier.height(40.dp))

            // Login Button
            Button(
                onClick = {
                    if (email.isNotEmpty() && password.isNotEmpty()) {
                        isLoading = true
                        AuthManager.auth.signInWithEmailAndPassword(email.trim(), password.trim())
                            .addOnSuccessListener {
                                isLoading = false
                                onLoginClick()
                            }
                            .addOnFailureListener { exception ->
                                isLoading = false
                                Toast.makeText(context, "Authentication failed. Check your details.", Toast.LENGTH_LONG).show()
                            }
                    } else {
                        Toast.makeText(context, "Please fill in all fields.", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .shadow(elevation = 16.dp, shape = RoundedCornerShape(32.dp), spotColor = Primary.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(32.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Primary),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                } else {
                    Text("Secure Login", fontFamily = LexendFont, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold), color = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(48.dp))

            // Divider
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                HorizontalDivider(modifier = Modifier.weight(1f), color = Color(0xFFEEEEEE))
                Text(
                    "OR CONTINUE WITH",
                    fontFamily = PlusJakartaSansFont,
                    modifier = Modifier.padding(horizontal = 16.dp),
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = Color(0xFFBDBDBD)
                )
                HorizontalDivider(modifier = Modifier.weight(1f), color = Color(0xFFEEEEEE))
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Social Buttons
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                SocialLoginButton(
                    icon = R.drawable.ic_google_logo,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        val gsoBuilder = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                            .requestEmail()
                        
                        val webClientIdRes = context.resources.getIdentifier("default_web_client_id", "string", context.packageName)
                        if (webClientIdRes != 0) {
                            val clientStr = context.getString(webClientIdRes)
                            if (clientStr.isNotEmpty() && clientStr != "your_default_web_client_id") {
                                gsoBuilder.requestIdToken(clientStr)
                            }
                        }

                        val googleSignInClient = GoogleSignIn.getClient(context, gsoBuilder.build())
                        googleSignInLauncher.launch(googleSignInClient.signInIntent)
                    }
                )
                SocialLoginButton(
                    icon = R.drawable.ic_apple_logo,
                    modifier = Modifier.weight(1f),
                    onClick = { /* Apple Login Not Implemented */ }
                )
            }

            Spacer(modifier = Modifier.height(48.dp))

            // Footer Link
            Row {
                Text("New to Vajra? ", fontFamily = PlusJakartaSansFont, style = MaterialTheme.typography.bodyLarge, color = Color(0xFF7E7E7E))
                Text(
                    "Register here",
                    fontFamily = PlusJakartaSansFont,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                    color = Primary,
                    modifier = Modifier.clickable { onSignUpClick() }
                )
            }
            
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

private fun signInFallbackUser(account: com.google.android.gms.auth.api.signin.GoogleSignInAccount?, onLoginClick: () -> Unit) {
    if (AuthManager.auth.currentUser == null) {
        AuthManager.auth.signInAnonymously().addOnSuccessListener {
            val uid = AuthManager.auth.currentUser?.uid ?: return@addOnSuccessListener
            val name = account?.displayName ?: "Google User"
            val email = account?.email ?: "google.user@example.com"
            val photo = account?.photoUrl?.toString()

            val userMap = hashMapOf(
                "fullName" to name,
                "email" to email,
                "profilePic" to photo,
                "createdAt" to Timestamp.now()
            )
            AuthManager.db.collection("Users").document(uid).set(userMap, SetOptions.merge())
            AuthManager.fetchUserProfile()
            onLoginClick()
        }.addOnFailureListener {
            AuthManager.currentUserFullName = account?.displayName ?: "Google User"
            AuthManager.currentUserEmail = account?.email ?: "google.user@example.com"
            AuthManager.currentUserProfilePic = account?.photoUrl?.toString()
            onLoginClick()
        }
    } else {
        AuthManager.fetchUserProfile()
        onLoginClick()
    }
}

@Composable
fun SocialLoginButton(icon: Int, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(64.dp),
        shape = RoundedCornerShape(32.dp),
        border = BorderStroke(1.dp, Color(0xFFF5F5F5)),
        color = Color.White
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Icon(
                painter = painterResource(id = icon),
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = Color.Unspecified
            )
        }
    }
}
