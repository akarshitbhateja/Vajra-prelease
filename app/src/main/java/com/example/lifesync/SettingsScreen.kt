package com.example.lifesync

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.automirrored.filled.Shortcut
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.lifesync.ui.theme.*

@Composable
fun SettingsScreen(
    onBackClick: () -> Unit,
    onLogout: () -> Unit,
    onEditProfileClick: () -> Unit,
    onMedicalIdClick: () -> Unit,
    onNavClick: () -> Unit,
    onAvatarClick: () -> Unit
) {
    val scrollState = rememberScrollState()
    
    // States for interactive elements
    var biometricLock by remember { mutableStateOf(true) }
    var silentSos by remember { mutableStateOf(false) }
    var alertVolume by remember { mutableFloatStateOf(AuthManager.alertVolume.coerceIn(0.3f, 1f)) }
    var darkMode by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFFF9F9FF)).statusBarsPadding()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp)
        ) {
            // Editorial Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                IconButton(
                    onClick = onNavClick,
                    modifier = Modifier.align(Alignment.CenterStart)
                ) {
                    Icon(Icons.Default.Menu, "Menu", tint = Primary, modifier = Modifier.size(28.dp))
                }

                Text(
                    "VAJRA",
                    fontFamily = LexendFont,
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 4.sp
                    ),
                    color = Primary
                )

                Box(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .clickable { onAvatarClick() }
                ) {
                    UserProfileAvatar(modifier = Modifier.size(44.dp).shadow(4.dp, CircleShape))
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))

            // 1. ELEVATED PROFILE CARD
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                color = SoftLilac.copy(alpha = 0.5f),
                shape = RoundedCornerShape(48.dp)
            ) {
                Column(
                    modifier = Modifier.padding(vertical = 32.dp, horizontal = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(contentAlignment = Alignment.BottomEnd) {
                        Surface(
                            modifier = Modifier
                                .size(110.dp)
                                .border(4.dp, Color.White, CircleShape),
                            shape = CircleShape,
                            color = Color.White
                        ) {
                            val photoUrl = AuthManager.currentUserProfilePic
                            if (!photoUrl.isNullOrEmpty()) {
                                AsyncImage(
                                    model = photoUrl,
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Box(
                                    modifier = Modifier.fillMaxSize().background(Primary.copy(alpha = 0.1f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        AuthManager.currentUserInitials,
                                        fontFamily = PlusJakartaSansFont,
                                        fontSize = 32.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Primary
                                    )
                                }
                            }
                        }
                        // Badge icon
                        Surface(
                            modifier = Modifier.size(28.dp).offset(x = 2.dp, y = 2.dp),
                            shape = CircleShape,
                            color = Primary,
                            border = BorderStroke(2.dp, Color.White)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Schedule, null, tint = Color.White, modifier = Modifier.size(14.dp))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))
                    Text(
                        AuthManager.currentUserFullName,
                        fontFamily = LexendFont,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        color = Primary
                    )
                    Text(
                        AuthManager.currentUserEmail,
                        fontFamily = PlusJakartaSansFont,
                        fontSize = 13.sp,
                        color = Primary.copy(alpha = 0.6f),
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.height(28.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Surface(
                            onClick = onEditProfileClick,
                            modifier = Modifier.weight(1f).height(56.dp),
                            shape = RoundedCornerShape(28.dp),
                            color = Color.White.copy(alpha = 0.7f)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxSize(),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Edit, null, tint = Primary, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Edit Profile", fontFamily = PlusJakartaSansFont, color = Primary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        Surface(
                            onClick = onMedicalIdClick,
                            modifier = Modifier.weight(1f).height(56.dp),
                            shape = RoundedCornerShape(28.dp),
                            color = Color.White.copy(alpha = 0.7f)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxSize().padding(horizontal = 4.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.QrCode, null, tint = Primary, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Emergency QR", fontFamily = PlusJakartaSansFont, color = Primary, fontSize = 13.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // SECTION: Identity Verification
            SettingsHeaderLabel("Identity Verification")
            SettingsItemRow(
                icon = Icons.Default.Fingerprint,
                title = "Identity Verification Status",
                subtitle = "Level 3: Biometric Enrolled",
                iconBgColor = Color(0xFFFFD1DF) // Soft pinkish circle as in image
            ) {
                Icon(Icons.Default.ChevronRight, null, tint = Color.LightGray, modifier = Modifier.size(24.dp))
            }

            // SECTION: Security & Access
            SettingsHeaderLabel("Security & Access")
            SettingsItemRow(icon = Icons.Default.Lock, title = "Biometric Lock") {
                Switch(
                    checked = biometricLock,
                    onCheckedChange = { biometricLock = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = Primary,
                        uncheckedTrackColor = Color(0xFFE2E2E8),
                        uncheckedThumbColor = Color.White
                    )
                )
            }
            SettingsItemRow(icon = Icons.Default.Dialpad, title = "Emergency Bypass PIN") {
                Icon(Icons.Default.ChevronRight, null, tint = Color.LightGray, modifier = Modifier.size(24.dp))
            }

            // SECTION: Emergency Protocols
            SettingsHeaderLabel("Emergency Protocols")
            SettingsItemRow(icon = Icons.Default.Schedule, title = "Response Timer") {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("5 min", fontFamily = PlusJakartaSansFont, color = Primary.copy(alpha = 0.4f), fontSize = 14.sp, fontWeight = FontWeight.Medium)
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(Icons.Default.ChevronRight, null, tint = Color.LightGray, modifier = Modifier.size(24.dp))
                }
            }
            SettingsItemRow(icon = Icons.Default.VisibilityOff, title = "Silent SOS Mode") {
                Switch(
                    checked = silentSos,
                    onCheckedChange = { silentSos = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = Primary,
                        uncheckedTrackColor = Color(0xFFE2E2E8),
                        uncheckedThumbColor = Color.White
                    )
                )
            }

            // SECTION: Audio Preferences
            SettingsHeaderLabel("Audio Preferences")
            Surface(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                color = Color(0xFFF3F3F9),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.AutoMirrored.Filled.VolumeUp, null, tint = Primary, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(16.dp))
                        Text("Master Alert Volume", fontFamily = PlusJakartaSansFont, fontWeight = FontWeight.Bold, color = Primary, fontSize = 15.sp)
                        Spacer(modifier = Modifier.weight(1f))
                        Text("${(alertVolume * 100).toInt()}%", fontFamily = PlusJakartaSansFont, fontWeight = FontWeight.Bold, color = Primary, fontSize = 14.sp)
                    }
                    Slider(
                        value = alertVolume,
                        onValueChange = { 
                            alertVolume = it
                            AuthManager.alertVolume = it
                            AuthManager.saveUserPreferences()
                        },
                        valueRange = 0.3f..1f,
                        colors = SliderDefaults.colors(
                            thumbColor = Primary,
                            activeTrackColor = Primary,
                            inactiveTrackColor = Primary.copy(alpha = 0.1f)
                        ),
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }

            // SECTION: Safety Network
            SettingsHeaderLabel("Safety Network")
            SettingsActionCard(
                icon = Icons.Default.Groups,
                title = "Guardian Circle",
                subtitle = "4 trusted contacts",
                actionLabel = "Manage"
            )
            SettingsActionCard(
                icon = Icons.AutoMirrored.Filled.Shortcut,
                title = "Hospital Network",
                subtitle = "Preferred providers",
                actionLabel = "Configure"
            )

            // SECTION: App Preferences
            SettingsHeaderLabel("App Preferences")
            SettingsItemRow(icon = Icons.Default.DarkMode, title = "Dark Mode") {
                Switch(
                    checked = darkMode,
                    onCheckedChange = { darkMode = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = Primary,
                        uncheckedTrackColor = Color(0xFFE2E2E8),
                        uncheckedThumbColor = Color.White
                    )
                )
            }
            SettingsItemRow(icon = Icons.Default.Notifications, title = "Notifications") {
                Icon(Icons.Default.ChevronRight, null, tint = Color.LightGray, modifier = Modifier.size(24.dp))
            }
            SettingsItemRow(icon = Icons.Default.Language, title = "Language") {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("English", fontFamily = PlusJakartaSansFont, color = Primary.copy(alpha = 0.4f), fontSize = 14.sp, fontWeight = FontWeight.Medium)
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(Icons.Default.ChevronRight, null, tint = Color.LightGray, modifier = Modifier.size(24.dp))
                }
            }

            Spacer(modifier = Modifier.height(48.dp))

            // SECURE LOGOUT BUTTON
            OutlinedButton(
                onClick = {
                    AuthManager.logout()
                    onLogout()
                },
                modifier = Modifier.fillMaxWidth().height(60.dp),
                shape = RoundedCornerShape(30.dp),
                border = BorderStroke(1.2.dp, Color(0xFFD32F2F).copy(alpha = 0.6f)),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFD32F2F))
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.AutoMirrored.Filled.Logout, null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Secure Logout", fontFamily = PlusJakartaSansFont, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }

            Spacer(modifier = Modifier.height(140.dp))
        }
    }
}

@Composable
fun SettingsHeaderLabel(text: String) {
    Text(
        text = text,
        fontFamily = PlusJakartaSansFont,
        fontSize = 15.sp,
        fontWeight = FontWeight.Bold,
        color = Primary,
        modifier = Modifier.padding(top = 24.dp, bottom = 12.dp)
    )
}

@Composable
fun SettingsItemRow(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    iconBgColor: Color = Color.Transparent,
    trailing: @Composable () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        color = Color(0xFFF3F3F9),
        shape = RoundedCornerShape(24.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(if (iconBgColor == Color.Transparent) Color(0xFFECECF4) else iconBgColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = if (iconBgColor == Color.Transparent) Primary else Color.White, modifier = Modifier.size(22.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontFamily = PlusJakartaSansFont, fontWeight = FontWeight.Bold, color = Primary, fontSize = 15.sp)
                if (subtitle != null) {
                    Text(subtitle, fontFamily = PlusJakartaSansFont, color = Primary.copy(alpha = 0.5f), fontSize = 12.sp)
                }
            }
            trailing()
        }
    }
}

@Composable
fun SettingsActionCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    actionLabel: String
) {
    Surface(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        color = Color(0xFFF3F3F9),
        shape = RoundedCornerShape(28.dp)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(40.dp).clip(CircleShape).background(Primary.copy(alpha = 0.05f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, null, tint = Primary, modifier = Modifier.size(22.dp))
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(title, fontFamily = PlusJakartaSansFont, fontWeight = FontWeight.Bold, color = Primary, fontSize = 16.sp)
                    Text(subtitle, fontFamily = PlusJakartaSansFont, color = Primary.copy(alpha = 0.5f), fontSize = 13.sp)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "$actionLabel \u2192",
                fontFamily = PlusJakartaSansFont,
                color = Primary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Black,
                modifier = Modifier.clickable { /* Action */ }
            )
        }
    }
}
