package com.example.lostfoundfict.data.repository

import android.content.Context
import com.example.lostfoundfict.data.local.AppDatabase
import com.example.lostfoundfict.data.local.entity.SavedItemEntity
import com.example.lostfoundfict.data.model.Item
import com.example.lostfoundfict.data.model.Comment
import com.example.lostfoundfict.data.remote.FirestoreService
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.Flow

class ItemRepository(context: Context) {

    private val firestoreService = FirestoreService()
    private val savedItemDao = AppDatabase.getDatabase(context).savedItemDao()

    private val currentUserId: String
        get() = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    // --- Firestore операции ---

    fun getAllItems(): Flow<List<Item>> = firestoreService.getAllItems()

    fun getLostItems(): Flow<List<Item>> = firestoreService.getLostItems()

    fun getFoundItems(): Flow<List<Item>> = firestoreService.getFoundItems()

    suspend fun addItem(item: Item): Result<String> = firestoreService.addItem(item)

    suspend fun getItemById(id: String): Result<Item> = firestoreService.getItemById(id)

    suspend fun markAsResolved(id: String): Result<Unit> = firestoreService.markAsResolved(id)

    suspend fun deleteItem(id: String): Result<Unit> = firestoreService.deleteItem(id)

    // --- Room операции ---

    fun getSavedItems(): Flow<List<SavedItemEntity>> =
        savedItemDao.getAllSavedItems(currentUserId)

    suspend fun saveItem(item: Item) {
        savedItemDao.saveItem(
            SavedItemEntity(
                firestoreId = item.id,
                userId = currentUserId,
                title = item.title,
                description = item.description,
                type = item.type,
                category = item.category,
                imageUrl = item.imageUrl,
                locationName = item.locationName,
                userName = item.userName
            )
        )
    }

    suspend fun unsaveItem(id: String) = savedItemDao.deleteById(id, currentUserId)

    fun isItemSaved(id: String): Flow<Boolean> = savedItemDao.isItemSaved(id, currentUserId)

    // --- Коментари ---

    fun getComments(itemId: String): Flow<List<Comment>> =
        firestoreService.getComments(itemId)

    suspend fun addComment(itemId: String, text: String): Result<Unit> {
        val user = FirebaseAuth.getInstance().currentUser
        val comment = Comment(
            itemId = itemId,
            userId = user?.uid ?: "",
            userName = if (user?.isAnonymous == true) "Guest"
            else user?.displayName ?: user?.email?.split("@")?.get(0) ?: "User",
            text = text,
            timestamp = com.google.firebase.Timestamp.now()
        )
        return firestoreService.addComment(itemId, comment)
    }
}