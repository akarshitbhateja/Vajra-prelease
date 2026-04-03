package com.example.lifesync

import android.content.Context
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

object AuthManager {
    val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    val db: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }
    val storage: FirebaseStorage by lazy { FirebaseStorage.getInstance() }

    // Global Reactive State for User Profile
    var currentUserFullName by mutableStateOf("User")
    var currentUserInitials by mutableStateOf("U")
    var currentUserProfilePic by mutableStateOf<String?>(null)
    var currentUserEmail by mutableStateOf("")
    var currentUserPhone by mutableStateOf("")
    var currentUserAddress by mutableStateOf("")
    
    // Primary Guardian Cache
    var primaryGuardianPhone by mutableStateOf<String?>(null)

    // User Preferences
    var alertVolume by mutableFloatStateOf(0.5f) // Default 50%

    fun isUserLoggedIn(): Boolean {
        return auth.currentUser != null
    }

    fun logout() {
        auth.signOut()
        currentUserFullName = "User"
        currentUserInitials = "U"
        currentUserProfilePic = null
        currentUserEmail = ""
        currentUserPhone = ""
        currentUserAddress = ""
        primaryGuardianPhone = null
        alertVolume = 0.5f
    }

    fun fetchUserProfile() {
        val uid = auth.currentUser?.uid ?: return

        db.collection("Users").document(uid).addSnapshotListener { doc, error ->
            if (error != null || doc == null || !doc.exists()) {
                val user = auth.currentUser
                if (user != null && doc?.exists() == false) {
                     updateBasicProfileFromAuth(user)
                }
                return@addSnapshotListener
            }

            val name = doc.getString("fullName") ?: "User"
            currentUserFullName = name
            currentUserEmail = doc.getString("email") ?: ""
            currentUserPhone = doc.getString("phone") ?: ""
            currentUserAddress = doc.getString("address") ?: ""

            val parts = name.trim().split(Regex("\\s+"))
            currentUserInitials = if (parts.size > 1) {
                "${parts[0].take(1)}${parts[parts.size - 1].take(1)}".uppercase()
            } else {
                if (parts.isNotEmpty() && parts[0].isNotEmpty()) parts[0].take(1).uppercase() else "U"
            }

            currentUserProfilePic = doc.getString("profilePic")
            
            // Load alert volume if saved
            val savedVolume = doc.getDouble("alertVolume")?.toFloat()
            if (savedVolume != null) {
                alertVolume = savedVolume
            }
        }

        // Fetch Primary Guardian Phone
        db.collection("Users").document(uid).collection("contacts")
            .whereEqualTo("isPrimaryGuardian", true)
            .addSnapshotListener { snapshot, error ->
                if (error == null && snapshot != null && !snapshot.isEmpty) {
                    primaryGuardianPhone = snapshot.documents[0].getString("phone")
                    Log.d("VAJRA", "Primary Guardian Loaded: $primaryGuardianPhone")
                }
            }
        
        // Also cleanup old logs whenever we fetch profile
        cleanupExpiredLogs()
    }

    private fun updateBasicProfileFromAuth(user: FirebaseUser) {
        currentUserFullName = user.displayName ?: "User"
        currentUserEmail = user.email ?: ""
        currentUserProfilePic = user.photoUrl?.toString()
        
        val name = currentUserFullName
        val parts = name.trim().split(Regex("\\s+"))
        currentUserInitials = if (parts.size > 1) {
            "${parts[0].take(1)}${parts[parts.size - 1].take(1)}".uppercase()
        } else {
            if (parts.isNotEmpty() && parts[0].isNotEmpty()) parts[0].take(1).uppercase() else "U"
        }
        
        val userMap = hashMapOf(
            "fullName" to currentUserFullName,
            "email" to currentUserEmail,
            "profilePic" to currentUserProfilePic,
            "createdAt" to Timestamp.now()
        )
        db.collection("Users").document(user.uid).set(userMap, com.google.firebase.firestore.SetOptions.merge())
    }

    fun saveUserPreferences() {
        val uid = auth.currentUser?.uid ?: return
        val prefs = hashMapOf("alertVolume" to alertVolume)
        db.collection("Users").document(uid).set(prefs, com.google.firebase.firestore.SetOptions.merge())
    }

    /**
     * Deletes emergency logs that are older than 1 hour.
     * This acts as a manual fallback for TTL since it's not enabled.
     */
    fun cleanupExpiredLogs() {
        val uid = auth.currentUser?.uid ?: return
        val currentTime = Timestamp.now()

        db.collection("EmergencyLogs")
            .whereEqualTo("userId", uid)
            .whereLessThan("expiresAt", currentTime)
            .get()
            .addOnSuccessListener { snapshot ->
                if (!snapshot.isEmpty) {
                    val batch = db.batch()
                    snapshot.documents.forEach { doc ->
                        batch.delete(doc.reference)
                    }
                    batch.commit().addOnSuccessListener {
                        Log.d("AuthManager", "Successfully cleaned up ${snapshot.size()} expired logs.")
                    }
                }
            }
            .addOnFailureListener {
                Log.e("AuthManager", "Cleanup failed: ${it.message}")
            }
    }

    fun dispatchEmergencyAlert(
        gForce: Double,
        lat: Double,
        lng: Double,
        locationData: String,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        val uid = auth.currentUser?.uid ?: return

        db.collection("Users").document(uid).collection("medical").document("profile").get()
            .addOnSuccessListener { doc ->
                val bloodType = doc.getString("bloodType") ?: "Unspecified"

                val emergencyData = hashMapOf(
                    "userId" to uid,
                    "patientName" to currentUserFullName,
                    "bloodType" to bloodType,
                    "impactGForce" to gForce,
                    "lat" to lat,
                    "lng" to lng,
                    "locationCoordinates" to locationData,
                    "timestamp" to Timestamp.now(),
                    "status" to "ACTIVE"
                )

                db.collection("Emergencies").add(emergencyData)
                    .addOnSuccessListener { onSuccess() }
                    .addOnFailureListener { onFailure(it.message ?: "System error during dispatch.") }
            }
            .addOnFailureListener {
                val emergencyData = hashMapOf(
                    "userId" to uid,
                    "patientName" to currentUserFullName,
                    "bloodType" to "Unspecified",
                    "impactGForce" to gForce,
                    "lat" to lat,
                    "lng" to lng,
                    "locationCoordinates" to locationData,
                    "timestamp" to Timestamp.now(),
                    "status" to "ACTIVE"
                )

                db.collection("Emergencies").add(emergencyData)
                    .addOnSuccessListener { onSuccess() }
                    .addOnFailureListener { onFailure(it.message ?: "System error during dispatch.") }
            }
    }
}
