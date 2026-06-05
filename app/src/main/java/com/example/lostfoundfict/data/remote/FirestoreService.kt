package com.example.lostfoundfict.data.remote

import com.example.lostfoundfict.data.model.Item
import com.example.lostfoundfict.data.model.Comment
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirestoreService {

    private val db: FirebaseFirestore = Firebase.firestore
    private val itemsCollection = db.collection("items")

    // Земи ги сите огласи во реално време (Flow) — Безбедно од краш при Sign Out
    fun getAllItems(): Flow<List<Item>> = callbackFlow {
        val listener = itemsCollection
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    android.util.Log.e("FIRESTORE", "Error: ${error.message}")
                    // НЕ го затвораме flow-от за да спречиме фатален краш, само логираме
                    return@addSnapshotListener
                }

                val items = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Item::class.java)?.copy(id = doc.id)
                } ?: emptyList()
                trySend(items)
            }
        awaitClose { listener.remove() }
    }

    // Земи само Lost огласи — Безбедно од краш при Sign Out
    fun getLostItems(): Flow<List<Item>> = callbackFlow {
        val listener = itemsCollection
            .whereEqualTo("type", "lost")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    android.util.Log.e("FIRESTORE", "Error: ${error.message}")
                    return@addSnapshotListener
                }
                val items = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Item::class.java)?.copy(id = doc.id)
                } ?: emptyList()
                trySend(items)
            }
        awaitClose { listener.remove() }
    }

    // Земи само Found огласи — Безбедно од краш при Sign Out
    fun getFoundItems(): Flow<List<Item>> = callbackFlow {
        val listener = itemsCollection
            .whereEqualTo("type", "found")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    android.util.Log.e("FIRESTORE", "Error: ${error.message}")
                    return@addSnapshotListener
                }
                val items = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Item::class.java)?.copy(id = doc.id)
                } ?: emptyList()
                trySend(items)
            }
        awaitClose { listener.remove() }
    }

    // Додај нов оглас
    suspend fun addItem(item: Item): Result<String> {
        return try {
            val docRef = itemsCollection.add(item).await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Земи еден оглас по ID
    suspend fun getItemById(id: String): Result<Item> {
        return try {
            val doc = itemsCollection.document(id).get().await()
            val item = doc.toObject(Item::class.java)?.copy(id = doc.id)
            if (item != null) Result.success(item)
            else Result.failure(Exception("Item not found"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Означи оглас како решен
    suspend fun markAsResolved(id: String): Result<Unit> {
        return try {
            itemsCollection.document(id).update("isResolved", true).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Избриши оглас
    suspend fun deleteItem(id: String): Result<Unit> {
        return try {
            itemsCollection.document(id).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // --- Коментари ---
    fun getComments(itemId: String): Flow<List<Comment>> = callbackFlow {
        val listener = db.collection("items")
            .document(itemId)
            .collection("comments")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    android.util.Log.e("FIRESTORE", "Comments error: ${error.message}")
                    return@addSnapshotListener
                }
                val comments = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Comment::class.java)?.copy(id = doc.id)
                } ?: emptyList()
                trySend(comments)
            }
        awaitClose { listener.remove() }
    }

    suspend fun addComment(itemId: String, comment: Comment): Result<Unit> {
        return try {
            db.collection("items")
                .document(itemId)
                .collection("comments")
                .add(comment)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

}