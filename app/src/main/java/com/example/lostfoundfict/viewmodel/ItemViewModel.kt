package com.example.lostfoundfict.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.lostfoundfict.data.model.Item
import com.example.lostfoundfict.data.repository.ItemRepository
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class ItemState {
    object Idle : ItemState()
    object Loading : ItemState()
    object Success : ItemState()
    data class Error(val message: String) : ItemState()
}

class ItemViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ItemRepository(application)

    // Сите огласи
    val allItems = repository.getAllItems()
    val lostItems = repository.getLostItems()
    val foundItems = repository.getFoundItems()

    // Зачувани огласи (Room)
    val savedItems = repository.getSavedItems()

    // Состојба за UI
    private val _itemState = MutableStateFlow<ItemState>(ItemState.Idle)
    val itemState: StateFlow<ItemState> = _itemState

    // Додај нов оглас
    fun addItem(
        title: String,
        description: String,
        type: String,
        category: String,
        imageUrl: String = "",
        locationName: String = ""
    ) {
        val currentUser = FirebaseAuth.getInstance().currentUser ?: return

        viewModelScope.launch {
            _itemState.value = ItemState.Loading

            val item = Item(
                title = title,
                description = description,
                type = type,
                category = category,
                imageUrl = imageUrl,
                locationName = locationName,
                userId = currentUser.uid,
                userName = if (currentUser.isAnonymous) "Guest"
                else currentUser.displayName ?: currentUser.email ?: "User",
                userEmail = currentUser.email ?: "",
                timestamp = Timestamp.now()
            )

            val result = repository.addItem(item)
            _itemState.value = if (result.isSuccess) {
                ItemState.Success
            } else {
                ItemState.Error(result.exceptionOrNull()?.message ?: "Грешка")
            }
        }
    }

    // Зачувај / Отзачувај оглас
    fun toggleSaveItem(item: Item, isSaved: Boolean) {
        viewModelScope.launch {
            if (isSaved) {
                repository.unsaveItem(item.id)
            } else {
                repository.saveItem(item)
            }
        }
    }

    fun isItemSaved(id: String) = repository.isItemSaved(id)

    fun markAsResolved(id: String) {
        viewModelScope.launch {
            repository.markAsResolved(id)
        }
    }

    fun deleteItem(id: String) {
        viewModelScope.launch {
            repository.deleteItem(id)
        }
    }

    fun resetState() {
        _itemState.value = ItemState.Idle
    }

    // --- Коментари ---

    fun getComments(itemId: String) = repository.getComments(itemId)

    fun addComment(itemId: String, text: String) {
        viewModelScope.launch {
            repository.addComment(itemId, text)
        }
    }
}