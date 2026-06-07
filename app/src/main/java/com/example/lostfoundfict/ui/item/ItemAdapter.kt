package com.example.lostfoundfict.ui.item

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.lostfoundfict.R
import com.example.lostfoundfict.data.model.Item
import com.example.lostfoundfict.databinding.ItemCardBinding
import java.text.SimpleDateFormat
import java.util.Locale

class ItemAdapter(
    private val onItemClick: (Item) -> Unit,
    private val onSaveClick: (Item) -> Unit
) : ListAdapter<Item, ItemAdapter.ItemViewHolder>(DiffCallback()) {

    // Листа на зачувани ID-а за да знаеме кои се bookmark-ирани
    private val savedIds = mutableSetOf<String>()

    fun updateSavedIds(ids: Set<String>) {
        savedIds.clear()
        savedIds.addAll(ids)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val binding = ItemCardBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ItemViewHolder(private val binding: ItemCardBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Item) {
            binding.tvTitle.text = item.title
            binding.tvDescription.text = item.description
            binding.tvLocation.text = if (item.locationName.isNotEmpty())
                "📍 ${item.locationName}" else ""

            // Датум
            val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
            val date = item.timestamp?.toDate()
            val dateStr = if (date != null) dateFormat.format(date) else ""
            binding.tvUserDate.text = "${item.userName} • $dateStr"

            // Lost/Found badge боја
            if (item.type == "lost") {
                binding.tvType.text = "LOST"
                binding.tvType.setBackgroundColor(Color.parseColor("#E53935"))
            } else {
                binding.tvType.text = "FOUND"
                binding.tvType.setBackgroundColor(Color.parseColor("#43A047"))
            }

            // Слика со Glide
            if (item.imageUrl.isNotEmpty()) {
                Glide.with(binding.ivItemImage.context)
                    .load(item.imageUrl)
                    .centerCrop()
                    .placeholder(R.mipmap.ic_launcher)
                    .into(binding.ivItemImage)
            }

            // Bookmark икона
            val isSaved = savedIds.contains(item.id)
            binding.btnSave.setImageResource(
                if (isSaved) R.drawable.ic_saved else R.drawable.ic_save_outline
            )

            // Клик на картичката
            binding.root.setOnClickListener { onItemClick(item) }

            // Клик на bookmark
            binding.btnSave.setOnClickListener { onSaveClick(item) }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<Item>() {
        override fun areItemsTheSame(oldItem: Item, newItem: Item) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Item, newItem: Item) = oldItem == newItem
    }
}