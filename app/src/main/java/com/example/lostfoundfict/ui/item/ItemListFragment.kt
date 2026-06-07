package com.example.lostfoundfict.ui.item

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
import com.example.lostfoundfict.databinding.FragmentItemListBinding
import com.example.lostfoundfict.viewmodel.ItemViewModel
import com.google.android.material.tabs.TabLayout
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ItemListFragment : Fragment() {

    private var _binding: FragmentItemListBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ItemViewModel by activityViewModels()
    private lateinit var adapter: ItemAdapter
    private var itemsJob: Job? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentItemListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupTabs()
        observeSavedItems()
        loadItems("all")
    }

    private fun setupRecyclerView() {
        adapter = ItemAdapter(
            onItemClick = { item -> navigateToDetail(item) },
            onSaveClick = { item -> toggleSave(item) }
        )
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter
    }

    private fun setupTabs() {
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText(getString(R.string.tab_all)))
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText(getString(R.string.tab_lost)))
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText(getString(R.string.tab_found)))

        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                when (tab.position) {
                    0 -> loadItems("all")
                    1 -> loadItems("lost")
                    2 -> loadItems("found")
                }
            }
            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })
    }

    private fun loadItems(filter: String) {
        itemsJob?.cancel()
        itemsJob = viewLifecycleOwner.lifecycleScope.launch {
            val flow = when (filter) {
                "lost" -> viewModel.lostItems
                "found" -> viewModel.foundItems
                else -> viewModel.allItems
            }
            flow.collectLatest { items ->
                showItems(items)
            }
        }
    }

    private fun showItems(items: List<Item>) {
        adapter.submitList(items)
        binding.tvEmpty.visibility = if (items.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun observeSavedItems() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.savedItems.collectLatest { savedList ->
                val savedIds = savedList.map { it.firestoreId }.toSet()
                adapter.updateSavedIds(savedIds)
            }
        }
    }

    private fun navigateToDetail(item: Item) {
        val bundle = Bundle().apply { putString("itemId", item.id) }
        findNavController().navigate(R.id.itemDetailFragment, bundle)
    }

    private fun toggleSave(item: Item) {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.isItemSaved(item.id).collectLatest { isSaved ->
                viewModel.toggleSaveItem(item, isSaved)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        itemsJob?.cancel()
        _binding = null
    }
}