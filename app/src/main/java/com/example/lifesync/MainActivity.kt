package com.example.lifesync

import android.Manifest
import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.em
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.util.Consumer
import coil.compose.AsyncImage
import com.example.lifesync.ui.theme.*
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority

enum class AppScreen { Login, SignUp1, SignUp2, Home, Contacts, MedicalProfile, Settings, SOS, MedicalID, AddContact, EditContact, EditProfile }

class MainActivity : ComponentActivity() {

    private var isSosActiveGlobal = false

    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        if (isSosActiveGlobal) {
            val bounceIntent = Intent(this, MainActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            }
            startActivity(bounceIntent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        if (!Settings.canDrawOverlays(this)) {
            val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName"))
            startActivity(intent)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
            val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
            keyguardManager.requestDismissKeyguard(this, null)
        }
        @Suppress("DEPRECATION")
        window.addFlags(
            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                    WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                    WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
        )

        setContent {
            VajraTheme {
                var currentScreen by remember {
                    mutableStateOf(if (AuthManager.isUserLoggedIn()) AppScreen.Home else AppScreen.Login)
                }
                var detectedGForce by remember { mutableDoubleStateOf(0.0) }
                var detectedLocation by remember { mutableStateOf("Locating...") }
                
                var detectedLat by remember { mutableDoubleStateOf(0.0) }
                var detectedLng by remember { mutableDoubleStateOf(0.0) }
                
                var editingContact by remember { mutableStateOf<Contact?>(null) }
                var showBackgroundLocationDialog by remember { mutableStateOf(false) }

                val context = LocalContext.current
                val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

                val onAvatarClick: () -> Unit = {
                    currentScreen = if (currentScreen == AppScreen.Settings) AppScreen.EditProfile else AppScreen.Settings
                }

                val onNavClick: () -> Unit = {
                    if (currentScreen == AppScreen.Home) {
                        // Optional: Open drawer
                    } else {
                        currentScreen = when (currentScreen) {
                            AppScreen.AddContact, AppScreen.EditContact -> AppScreen.Contacts
                            AppScreen.Contacts, AppScreen.MedicalProfile, AppScreen.Settings, AppScreen.MedicalID, AppScreen.EditProfile, AppScreen.SignUp2 -> AppScreen.Home
                            else -> AppScreen.Home
                        }
                    }
                }

                BackHandler(enabled = currentScreen != AppScreen.Home && currentScreen != AppScreen.Login) {
                    onNavClick()
                }

                val backgroundLocationLauncher = rememberLauncherForActivityResult(
                    ActivityResultContracts.RequestPermission()
                ) { isGranted ->
                    if (isGranted) {
                        Toast.makeText(context, "VAJRA is now protecting you 24/7!", Toast.LENGTH_SHORT).show()
                    }
                }

                val permissionLauncher = rememberLauncherForActivityResult(
                    ActivityResultContracts.RequestMultiplePermissions()
                ) { permissions ->
                    val fineLocationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
                    if (fineLocationGranted) {
                        startCrashService()
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            val backgroundGranted = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED
                            if (!backgroundGranted) {
                                showBackgroundLocationDialog = true
                            }
                        }
                    }
                }

                if (showBackgroundLocationDialog) {
                    AlertDialog(
                        onDismissRequest = { showBackgroundLocationDialog = false },
                        title = { Text("Enable 'Allow All The Time' Location", fontFamily = LexendFont) },
                        text = { Text("VAJRA needs 'Allow All The Time' location access to detect accidents even when your screen is off. Please select 'Allow all the time' in the next screen for maximum safety.", fontFamily = PlusJakartaSansFont) },
                        confirmButton = {
                            Button(onClick = {
                                showBackgroundLocationDialog = false
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                    backgroundLocationLauncher.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                                }
                            }) {
                                Text("GRANT ACCESS", fontFamily = PlusJakartaSansFont, fontWeight = FontWeight.Bold)
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showBackgroundLocationDialog = false }) {
                                Text("MAYBE LATER", fontFamily = PlusJakartaSansFont, color = Color.Gray)
                            }
                        }
                    )
                }

                LaunchedEffect(Unit) {
                    val permissionsToRequest = mutableListOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.SEND_SMS,
                        Manifest.permission.CALL_PHONE
                    )
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
                    }
                    permissionLauncher.launch(permissionsToRequest.toTypedArray())

                    if (AuthManager.isUserLoggedIn()) {
                        AuthManager.fetchUserProfile()
                        startCrashService()
                    }
                }

                LaunchedEffect(currentScreen) {
                    isSosActiveGlobal = (currentScreen == AppScreen.SOS)
                }

                LaunchedEffect(intent) {
                    if (intent.getBooleanExtra("CRASH_TRIGGERED", false)) {
                        detectedGForce = intent.getDoubleExtra("G_FORCE", 0.0)
                        detectedLocation = intent.getStringExtra("LOCATION_DATA") ?: "Locating..."
                        detectedLat = intent.getDoubleExtra("LATITUDE", 0.0)
                        detectedLng = intent.getDoubleExtra("LONGITUDE", 0.0)
                        currentScreen = AppScreen.SOS
                    }
                }

                DisposableEffect(Unit) {
                    val listener = Consumer<Intent> { newIntent ->
                        if (newIntent.getBooleanExtra("CRASH_TRIGGERED", false)) {
                            detectedGForce = newIntent.getDoubleExtra("G_FORCE", 0.0)
                            detectedLocation = newIntent.getStringExtra("LOCATION_DATA") ?: "Locating..."
                            detectedLat = newIntent.getDoubleExtra("LATITUDE", 0.0)
                            detectedLng = newIntent.getDoubleExtra("LONGITUDE", 0.0)
                            currentScreen = AppScreen.SOS
                        }
                    }
                    addOnNewIntentListener(listener)
                    onDispose { removeOnNewIntentListener(listener) }
                }

                Box(modifier = Modifier.fillMaxSize()) {
                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        containerColor = Color(0xFFF9F9FF),
                        contentWindowInsets = WindowInsets(0, 0, 0, 0)
                    ) { innerPadding ->
                        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
                            AnimatedContent(
                                targetState = currentScreen,
                                transitionSpec = { fadeIn(animationSpec = tween(400)) togetherWith fadeOut(animationSpec = tween(400)) },
                                label = "ScreenTransition"
                            ) { screen ->
                                when (screen) {
                                    AppScreen.Login -> LoginScreen(
                                        onLoginClick = { AuthManager.fetchUserProfile(); startCrashService(); currentScreen = AppScreen.Home },
                                        onSignUpClick = { currentScreen = AppScreen.SignUp1 }
                                    )
                                    AppScreen.SignUp1 -> SignUpStep1Screen(
                                        onNextClick = { currentScreen = AppScreen.SignUp2 },
                                        onBackClick = { currentScreen = AppScreen.Login }
                                    )
                                    AppScreen.SignUp2 -> SignUpStep2Screen(
                                        onCompleteClick = { currentScreen = AppScreen.Home },
                                        onBackClick = { currentScreen = AppScreen.SignUp1 },
                                        onNavClick = onNavClick,
                                        onAvatarClick = onAvatarClick
                                    )
                                    AppScreen.Home -> HomeDashboardScreen(
                                        onContactsClick = { currentScreen = AppScreen.Contacts },
                                        onProfileClick = { currentScreen = AppScreen.MedicalProfile },
                                        onSettingsClick = { currentScreen = AppScreen.Settings },
                                        onSosClick = { 
                                            detectedGForce = 0.0
                                            detectedLocation = "Locating..."
                                            detectedLat = 0.0
                                            detectedLng = 0.0
                                            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                                                fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                                                    .addOnSuccessListener { loc ->
                                                        if (loc != null) {
                                                            detectedLocation = "${loc.latitude}, ${loc.longitude}"
                                                            detectedLat = loc.latitude
                                                            detectedLng = loc.longitude
                                                        }
                                                    }
                                            }
                                            currentScreen = AppScreen.SOS 
                                        },
                                        onMedicalIdClick = { currentScreen = AppScreen.MedicalID },
                                        onNavClick = onNavClick,
                                        onAvatarClick = onAvatarClick
                                    )
                                    AppScreen.Contacts -> ContactsScreen(
                                        onAddContactClick = { currentScreen = AppScreen.AddContact },
                                        onEditContactClick = { contact -> editingContact = contact; currentScreen = AppScreen.EditContact },
                                        onNavClick = onNavClick,
                                        onAvatarClick = onAvatarClick
                                    )
                                    AppScreen.MedicalProfile -> MedicalProfileScreen(
                                        onBackClick = { currentScreen = AppScreen.Home },
                                        onNavClick = onNavClick,
                                        onAvatarClick = onAvatarClick
                                    )
                                    AppScreen.Settings -> SettingsScreen(
                                        onBackClick = { currentScreen = AppScreen.Home },
                                        onLogout = { currentScreen = AppScreen.Login },
                                        onEditProfileClick = { currentScreen = AppScreen.EditProfile },
                                        onMedicalIdClick = { currentScreen = AppScreen.MedicalID },
                                        onNavClick = onNavClick,
                                        onAvatarClick = onAvatarClick
                                    )
                                    AppScreen.SOS -> SosScreen(
                                        gForce = detectedGForce,
                                        locationData = detectedLocation,
                                        onCancel = { currentScreen = AppScreen.MedicalID }, 
                                        onTimerFinished = {
                                            AuthManager.dispatchEmergencyAlert(
                                                gForce = detectedGForce,
                                                lat = detectedLat,
                                                lng = detectedLng,
                                                locationData = detectedLocation,
                                                onSuccess = {
                                                    Toast.makeText(context, "Emergency protocols dispatched to nearest hospital.", Toast.LENGTH_LONG).show()
                                                    currentScreen = AppScreen.MedicalID 
                                                },
                                                onFailure = { error ->
                                                    Toast.makeText(context, "Dispatch failed: $error", Toast.LENGTH_LONG).show()
                                                    currentScreen = AppScreen.MedicalID 
                                                }
                                            )
                                        }
                                    )
                                    AppScreen.MedicalID -> MedicalIdScreen(
                                        onBackClick = { currentScreen = AppScreen.Home }
                                    )
                                    AppScreen.AddContact -> AddContactScreen(
                                        onBackClick = { currentScreen = AppScreen.Contacts }
                                    )
                                    AppScreen.EditContact -> {
                                        editingContact?.let {
                                            EditContactScreen(
                                                contact = it,
                                                onBackClick = { currentScreen = AppScreen.Contacts }
                                            )
                                        }
                                    }
                                    AppScreen.EditProfile -> EditProfileScreen(
                                        onBackClick = { currentScreen = AppScreen.Settings },
                                        onMedicalIdClick = { currentScreen = AppScreen.MedicalID }
                                    )
                                }
                            }

                            // Floating Footer Navigation Bar
                            val allowedScreens = listOf(
                                AppScreen.Home, AppScreen.Contacts, AppScreen.MedicalProfile, 
                                AppScreen.Settings, AppScreen.AddContact, AppScreen.EditContact, 
                                AppScreen.EditProfile
                            )
                            if (currentScreen in allowedScreens) {
                                Box(modifier = Modifier.align(Alignment.BottomCenter)) {
                                    BottomNavBar(currentScreen = currentScreen) { screen ->
                                        currentScreen = screen
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun startCrashService() {
        val serviceIntent = Intent(this, CrashService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent)
        } else {
            startService(serviceIntent)
        }
    }
}

@Composable
fun VajraHeader(currentScreen: AppScreen, onNavClick: () -> Unit, onAvatarClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        IconButton(
            onClick = onNavClick,
            modifier = Modifier.align(Alignment.CenterStart)
        ) {
            val icon = if (currentScreen == AppScreen.Home) Icons.Default.Menu else Icons.AutoMirrored.Filled.ArrowBack
            Icon(icon, "Navigation", tint = Primary, modifier = Modifier.size(28.dp))
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
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { onAvatarClick() }
        ) {
            UserProfileAvatar(modifier = Modifier.size(44.dp).shadow(4.dp, CircleShape))
        }
    }
}

@Composable
fun HomeDashboardScreen(
    onContactsClick: () -> Unit,
    onProfileClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onSosClick: () -> Unit,
    onMedicalIdClick: () -> Unit,
    onNavClick: () -> Unit,
    onAvatarClick: () -> Unit
) {
    val scrollState = rememberScrollState()
    
    Box(modifier = Modifier.fillMaxSize().background(Color(0xFFF9F9FF)).statusBarsPadding()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            VajraHeader(currentScreen = AppScreen.Home, onNavClick = onNavClick, onAvatarClick = onAvatarClick)

            Spacer(modifier = Modifier.height(40.dp))

            // Protection Active Section
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Surface(
                    modifier = Modifier.size(84.dp), 
                    shape = CircleShape, 
                    color = Color.White,
                    shadowElevation = 10.dp
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Shield, null, tint = Primary, modifier = Modifier.size(36.dp))
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    "SYSTEM DIAGNOSTIC", 
                    fontFamily = PlusJakartaSansFont,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.1.em
                    ),
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "PROTECTION\nACTIVE", 
                    fontFamily = LexendFont,
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontSize = 38.sp, 
                        fontWeight = FontWeight.Black,
                        lineHeight = 42.sp
                    ),
                    color = Primary, 
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(60.dp))

            // SOS Button with Animated Ripples
            Box(contentAlignment = Alignment.Center) {
                val infiniteTransition = rememberInfiniteTransition(label = "ripple")
                
                repeat(3) { i ->
                    val progress by infiniteTransition.animateFloat(
                        initialValue = 0f,
                        targetValue = 1f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(2000, easing = LinearEasing, delayMillis = i * 600),
                            repeatMode = RepeatMode.Restart
                        ),
                        label = "ripple_anim"
                    )
                    
                    Box(
                        modifier = Modifier
                            .graphicsLayer {
                                scaleX = 1f + (progress * 0.4f)
                                scaleY = 1f + (progress * 0.4f)
                                alpha = 1f - progress
                            }
                            .size(190.dp)
                            .border(2.dp, Color.Red.copy(alpha = 0.5f), CircleShape)
                    )
                }
                
                Surface(
                    modifier = Modifier
                        .size(190.dp)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { onSosClick() },
                    shape = CircleShape,
                    color = Color.Transparent
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = listOf(Color(0xFFE53935), Color(0xFF38073B))
                                )
                            )
                            .border(4.dp, Color.White.copy(alpha = 0.2f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                "SOS",
                                fontFamily = LexendFont,
                                color = Color.White,
                                style = MaterialTheme.typography.displayLarge.copy(
                                    fontSize = 64.sp,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = (-0.02).em
                                )
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                "MANUAL SOS", 
                                fontFamily = LexendFont,
                                color = Color.White, 
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Black,
                                    fontSize = 15.sp,
                                    letterSpacing = 1.sp
                                )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(64.dp))

            // Grid Cards
            Row(
                modifier = Modifier.fillMaxWidth(), 
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                DashboardSmallCard(
                    icon = Icons.Default.ContactPage,
                    label = "Emergency\nContacts",
                    onClick = onContactsClick,
                    modifier = Modifier.weight(1f)
                )
                DashboardSmallCard(
                    icon = Icons.Default.MedicalServices,
                    label = "Medical\nProfile",
                    onClick = onProfileClick,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(140.dp)) 
        }
    }
}

@Composable
fun DashboardSmallCard(icon: ImageVector, label: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(160.dp),
        color = SoftLilac.copy(alpha = 0.5f),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Surface(
                modifier = Modifier.size(48.dp),
                shape = CircleShape,
                color = Color.White
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, null, tint = Primary, modifier = Modifier.size(24.dp))
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                label, 
                fontFamily = PlusJakartaSansFont,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    lineHeight = 18.sp
                ),
                color = Primary, 
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun BottomNavBar(currentScreen: AppScreen, onNavigate: (AppScreen) -> Unit) {
    val items = listOf(
        Icons.Default.Home to AppScreen.Home,
        Icons.Default.ContactPage to AppScreen.Contacts,
        Icons.Default.MedicalServices to AppScreen.MedicalProfile,
        Icons.Default.Settings to AppScreen.Settings
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(bottom = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier
                .width(280.dp)
                .height(68.dp),
            shape = RoundedCornerShape(34.dp),
            color = Color.White,
            shadowElevation = 12.dp
        ) {
            Row(
                modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                items.forEach { (icon, screen) ->
                    val isSelected = when (currentScreen) {
                        AppScreen.Home -> screen == AppScreen.Home
                        AppScreen.Contacts, AppScreen.AddContact, AppScreen.EditContact -> screen == AppScreen.Contacts
                        AppScreen.MedicalProfile, AppScreen.MedicalID, AppScreen.EditProfile -> screen == AppScreen.MedicalProfile
                        AppScreen.Settings -> screen == AppScreen.Settings
                        else -> false
                    }

                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) { onNavigate(screen) }
                    ) {
                        if (isSelected) {
                            Surface(
                                modifier = Modifier.size(44.dp),
                                shape = CircleShape,
                                color = Primary
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(icon, null, tint = Color.White, modifier = Modifier.size(24.dp))
                                }
                            }
                        } else {
                            Icon(icon, null, tint = Color.Gray.copy(alpha = 0.4f), modifier = Modifier.size(24.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun UserProfileAvatar(modifier: Modifier = Modifier) {
    val photoUrl = AuthManager.currentUserProfilePic
    val initials = AuthManager.currentUserInitials

    Box(
        // FIXED: Now uses Vajra Soft Lilac (#ECD4EA) instead of the old blue
        modifier = modifier.clip(CircleShape).background(Color(0xFFECD4EA)).border(1.dp, Color.White, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        if (!photoUrl.isNullOrEmpty()) {
            AsyncImage(
                model = photoUrl,
                contentDescription = "User Profile",
                modifier = Modifier.fillMaxSize().clip(CircleShape),
                contentScale = ContentScale.Crop
            )
        } else {
            // FIXED: Text is now Vajra Deep Purple (#38073B)
            Text(text = initials, fontFamily = PlusJakartaSansFont, fontWeight = FontWeight.Bold, color = Color(0xFF38073B), fontSize = 14.sp)
        }
    }
}
