package com.example.lostfoundfict.ui.item

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.lostfoundfict.data.model.Comment
import com.example.lostfoundfict.databinding.ItemCommentBinding
import java.text.SimpleDateFormat
import java.util.Locale

class CommentAdapter : ListAdapter<Comment, CommentAdapter.CommentViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        val binding = ItemCommentBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return CommentViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class CommentViewHolder(private val binding: ItemCommentBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(comment: Comment) {
            binding.tvCommentUser.text = comment.userName
            binding.tvCommentText.text = comment.text
            binding.tvCommentAvatar.text = comment.userName
                .firstOrNull()?.uppercaseChar()?.toString() ?: "?"

            val dateFormat = SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault())
            val date = comment.timestamp?.toDate()
            binding.tvCommentTime.text = if (date != null) dateFormat.format(date) else ""
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<Comment>() {
        override fun areItemsTheSame(oldItem: Comment, newItem: Comment) =
            oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Comment, newItem: Comment) =
            oldItem == newItem
    }
}