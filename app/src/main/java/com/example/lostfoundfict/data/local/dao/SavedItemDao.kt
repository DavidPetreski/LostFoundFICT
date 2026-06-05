package com.example.lostfoundfict.data.local.dao

import androidx.room.*
import com.example.lostfoundfict.data.local.entity.SavedItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SavedItemDao {

    // Земи ги зачуваните огласи САМО за тековниот корисник — Flow значи автоматски се ажурира UI
    @Query("SELECT * FROM saved_items WHERE userId = :userId ORDER BY savedAt DESC")
    fun getAllSavedItems(userId: String): Flow<List<SavedItemEntity>>

    // Зачувај оглас
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveItem(item: SavedItemEntity)

    // Избриши оглас
    @Delete
    suspend fun deleteItem(item: SavedItemEntity)

    // Провери дали е зачуван за конкретниот корисник (за bookmark икона)
    @Query("SELECT EXISTS(SELECT 1 FROM saved_items WHERE firestoreId = :id AND userId = :userId)")
    fun isItemSaved(id: String, userId: String): Flow<Boolean>

    // Избриши по ID за конкретниот корисник
    @Query("DELETE FROM saved_items WHERE firestoreId = :id AND userId = :userId")
    suspend fun deleteById(id: String, userId: String)
}