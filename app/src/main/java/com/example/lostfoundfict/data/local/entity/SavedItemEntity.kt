package com.example.lostfoundfict.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "saved_items")
data class SavedItemEntity(
    @PrimaryKey
    val firestoreId: String,        // ID од Firestore
    val userId: String = "",
    val title: String,
    val description: String,
    val type: String,               // "lost" или "found"
    val category: String,
    val imageUrl: String,
    val locationName: String,
    val userName: String,
    val savedAt: Long = System.currentTimeMillis()
)