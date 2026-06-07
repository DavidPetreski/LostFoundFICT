package com.example.lostfoundfict.ui.saved

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.lostfoundfict.R
import com.example.lostfoundfict.data.model.Item
import com.example.lostfoundfict.databinding.FragmentSavedItemsBinding
import com.example.lostfoundfict.ui.item.ItemAdapter
import com.example.lostfoundfict.viewmodel.ItemViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class SavedItemsFragment : Fragment() {

    private var _binding: FragmentSavedItemsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ItemViewModel by activityViewModels()
    private lateinit var adapter: ItemAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentSavedItemsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        observeSavedItems()
    }

    private fun setupRecyclerView() {
        adapter = ItemAdapter(
            onItemClick = { item -> navigateToDetail(item) },
            onSaveClick = { item ->
                viewModel.toggleSaveItem(item, true)
            }
        )
        binding.recyclerViewSaved.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewSaved.adapter = adapter
    }

    private fun observeSavedItems() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.savedItems.collectLatest { savedList ->
                // Конвертирај SavedItemEntity во Item за адаптерот
                val items = savedList.map { entity ->
                    Item(
                        id = entity.firestoreId,
                        title = entity.title,
                        description = entity.description,
                        type = entity.type,
                        category = entity.category,
                        imageUrl = entity.imageUrl,
                        locationName = entity.locationName,
                        userName = entity.userName
                    )
                }
                adapter.submitList(items)
                val savedIds = savedList.map { it.firestoreId }.toSet()
                adapter.updateSavedIds(savedIds)
                binding.tvEmptySaved.visibility =
                    if (items.isEmpty()) View.VISIBLE else View.GONE
            }
        }
    }

    private fun navigateToDetail(item: Item) {
        val bundle = Bundle().apply { putString("itemId", item.id) }
        findNavController().navigate(R.id.itemDetailFragment, bundle)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}