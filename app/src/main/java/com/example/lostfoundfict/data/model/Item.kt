package com.example.lostfoundfict.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.GeoPoint

data class Item(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val type: String = "lost",          // "lost" или "found"
    val category: String = "",
    val imageUrl: String = "",
    val location: GeoPoint? = null,
    val locationName: String = "",
    val userId: String = "",
    val userName: String = "",
    val userEmail: String = "",
    val timestamp: Timestamp? = null,
    val isResolved: Boolean = false
)