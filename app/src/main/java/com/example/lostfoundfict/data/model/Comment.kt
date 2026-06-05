package com.example.lostfoundfict.data.model

import com.google.firebase.Timestamp

data class Comment(
    val id: String = "",
    val itemId: String = "",
    val userId: String = "",
    val userName: String = "",
    val text: String = "",
    val timestamp: Timestamp? = null
)