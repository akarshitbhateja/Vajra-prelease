import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    id("com.google.gms.google-services")
}

// 1. Read keys from local.properties
val localProperties = Properties()
val localPropertiesFile = rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
    localProperties.load(localPropertiesFile.inputStream())
}

android {
    namespace = "com.example.lifesync"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.lifesync"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // 2. Inject Secrets Securely into BuildConfig
        buildConfigField("String", "TWILIO_ACCOUNT_SID", "\"${localProperties.getProperty("TWILIO_ACCOUNT_SID") ?: ""}\"")
        buildConfigField("String", "TWILIO_AUTH_TOKEN", "\"${localProperties.getProperty("TWILIO_AUTH_TOKEN") ?: ""}\"")
        buildConfigField("String", "TWILIO_FROM_NUMBER", "\"${localProperties.getProperty("TWILIO_FROM_NUMBER") ?: ""}\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
        buildConfig = true // REQUIRED to generate BuildConfig class
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.androidx.google.fonts)

    // --- GOOGLE PLAY SERVICES ---
    implementation(libs.play.services.location)
    implementation(libs.play.services.auth)

    // --- COIL ---
    implementation(libs.coil.compose)

    // --- FIREBASE DEPENDENCIES ---
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.storage)
    
    // --- ZXING FOR QR CODES ---
    implementation(libs.zxing.core)

    // --- OKHTTP ---
    implementation(libs.okhttp)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}
