package com.example.lifesync

data class Contact(
    val id: String = "",
    val name: String = "",
    val phone: String = "",
    val relation: String = "Friend",
    val isPrimaryGuardian: Boolean = false,
    val photoUrl: String? = null
)