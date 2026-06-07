package com.example.lostfoundfict.ui.item

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.lostfoundfict.R
import com.example.lostfoundfict.databinding.FragmentItemDetailBinding
import com.example.lostfoundfict.viewmodel.ItemViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

class ItemDetailFragment : Fragment() {

    private var _binding: FragmentItemDetailBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ItemViewModel by activityViewModels()
    private var itemId: String = ""
    private lateinit var commentAdapter: CommentAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentItemDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Земи го itemId ПРВО
        itemId = arguments?.getString("itemId") ?: ""

        if (itemId.isEmpty()) {
            findNavController().popBackStack()
            return
        }

        // Потоа постави ги сите
        setupCommentRecyclerView()
        loadItem()
        loadComments()
        setupSendComment()

        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun loadItem() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.allItems.collectLatest { items ->
                val item = items.find { it.id == itemId } ?: return@collectLatest

                binding.tvDetailTitle.text = item.title
                binding.tvDetailDescription.text = item.description
                binding.tvDetailCategory.text = item.category
                binding.tvDetailLocation.text = if (item.locationName.isNotEmpty())
                    "📍 ${item.locationName}" else getString(R.string.no_location)
                binding.tvDetailUser.text = "👤 ${item.userName}"

                val dateFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
                val date = item.timestamp?.toDate()
                binding.tvDetailDate.text = if (date != null) dateFormat.format(date) else ""

                if (item.type == "lost") {
                    binding.tvDetailType.text = getString(R.string.type_lost)
                    binding.tvDetailType.setBackgroundColor(android.graphics.Color.parseColor("#E53935"))
                } else {
                    binding.tvDetailType.text = getString(R.string.type_found)
                    binding.tvDetailType.setBackgroundColor(android.graphics.Color.parseColor("#43A047"))
                }

                if (item.imageUrl.isNotEmpty()) {
                    binding.ivDetailImage.visibility = View.VISIBLE
                    Glide.with(this@ItemDetailFragment)
                        .load(item.imageUrl)
                        .centerCrop()
                        .into(binding.ivDetailImage)
                } else {
                    binding.ivDetailImage.visibility = View.GONE
                }

                val currentUser = FirebaseAuth.getInstance().currentUser
                if (currentUser?.uid == item.userId) {
                    binding.btnDelete.visibility = View.VISIBLE
                    binding.btnMarkResolved.visibility = View.VISIBLE
                }

                binding.btnDelete.setOnClickListener {
                    viewModel.deleteItem(itemId)
                    findNavController().popBackStack()
                }

                binding.btnMarkResolved.setOnClickListener {
                    viewModel.markAsResolved(itemId)
                    Toast.makeText(requireContext(), getString(R.string.marked_resolved), Toast.LENGTH_SHORT).show()
                    findNavController().popBackStack()
                }

                viewLifecycleOwner.lifecycleScope.launch {
                    viewModel.isItemSaved(itemId).collectLatest { isSaved ->
                        binding.btnBookmark.setImageResource(
                            if (isSaved) R.drawable.ic_saved else R.drawable.ic_save_outline
                        )
                        binding.btnBookmark.setOnClickListener {
                            viewModel.toggleSaveItem(item, isSaved)
                            val msg = if (isSaved) getString(R.string.removed_from_saved)
                            else getString(R.string.added_to_saved)
                            Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
    }

    private fun setupCommentRecyclerView() {
        commentAdapter = CommentAdapter()
        binding.rvComments.layoutManager = LinearLayoutManager(requireContext())
        binding.rvComments.adapter = commentAdapter
        binding.rvComments.isNestedScrollingEnabled = false
    }

    private fun loadComments() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.getComments(itemId).collectLatest { comments ->
                android.util.Log.d("COMMENTS", "Loaded ${comments.size} comments")
                commentAdapter.submitList(comments.toList())
                binding.tvNoComments.visibility =
                    if (comments.isEmpty()) View.VISIBLE else View.GONE
                binding.rvComments.visibility =
                    if (comments.isEmpty()) View.GONE else View.VISIBLE
            }
        }
    }

    private fun setupSendComment() {
        binding.btnSendComment.setOnClickListener {
            val text = binding.etComment.text.toString().trim()
            if (text.isEmpty()) {
                Toast.makeText(requireContext(),
                    getString(R.string.error_empty_comment),
                    Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            viewModel.addComment(itemId, text)
            binding.etComment.text?.clear()
            // Скролај до дното за да го видиш новиот коментар
            binding.rvComments.scrollToPosition(commentAdapter.itemCount - 1)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}