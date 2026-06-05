package com.example.lostfoundfict.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.lostfoundfict.R
import com.example.lostfoundfict.databinding.FragmentHomeBinding
import com.example.lostfoundfict.util.AnalyticsHelper
import com.example.lostfoundfict.viewmodel.ItemViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ItemViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Додадено точно како што побара Claude:
        AnalyticsHelper.logScreenView("HomeFragment")
        setupUserGreeting()
        setupStats()
        setupQuickActions()
        setupRecentItems()
    }

    private fun setupUserGreeting() {
        val user = FirebaseAuth.getInstance().currentUser
        val name = when {
            user?.isAnonymous == true -> getString(R.string.guest)
            user?.displayName?.isNotEmpty() == true -> user.displayName!!.split(" ")[0]
            user?.email?.isNotEmpty() == true -> user.email!!.split("@")[0]
            else -> getString(R.string.guest)
        }
        binding.tvGreeting.text = getString(R.string.greeting, name)
        binding.tvSubGreeting.text = getString(R.string.greeting_sub)
    }

    private fun setupStats() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.allItems.collectLatest { items ->
                binding.tvStatTotal.text = items.size.toString()
                binding.tvStatLost.text = items.count { it.type == "lost" }.toString()
                binding.tvStatFound.text = items.count { it.type == "found" }.toString()
                binding.tvStatResolved.text = items.count { it.isResolved }.toString()
            }
        }
    }

    private fun setupQuickActions() {
        binding.cardReportLost.setOnClickListener {
            findNavController().navigate(R.id.addItemFragment)
        }
        binding.cardReportFound.setOnClickListener {
            findNavController().navigate(R.id.addItemFragment)
        }
        binding.cardViewAll.setOnClickListener {
            findNavController().navigate(R.id.itemListFragment)
        }
    }

    private fun setupRecentItems() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.allItems.collectLatest { items ->
                val recent = items.take(3)

                if (recent.isEmpty()) {
                    binding.tvNoRecent.visibility = View.VISIBLE
                    binding.layoutRecentItems.visibility = View.GONE
                    return@collectLatest
                }

                binding.tvNoRecent.visibility = View.GONE
                binding.layoutRecentItems.visibility = View.VISIBLE

                val recentViews = listOf(binding.cardRecent1, binding.cardRecent2, binding.cardRecent3)
                val recentTitles = listOf(binding.tvRecent1Title, binding.tvRecent2Title, binding.tvRecent3Title)
                val recentTypes = listOf(binding.tvRecent1Type, binding.tvRecent2Type, binding.tvRecent3Type)
                val recentLocations = listOf(binding.tvRecent1Location, binding.tvRecent2Location, binding.tvRecent3Location)

                recentViews.forEachIndexed { index, card ->
                    if (index < recent.size) {
                        card.visibility = View.VISIBLE
                        val item = recent[index]
                        recentTitles[index].text = item.title
                        recentLocations[index].text = if (item.locationName.isNotEmpty())
                            "📍 ${item.locationName}" else "📍 ФИКТ"

                        if (item.type == "lost") {
                            recentTypes[index].text = getString(R.string.type_lost)
                            recentTypes[index].setBackgroundResource(R.drawable.badge_lost)
                        } else {
                            recentTypes[index].text = getString(R.string.type_found)
                            recentTypes[index].setBackgroundResource(R.drawable.badge_found)
                        }

                        card.setOnClickListener {
                            val bundle = Bundle().apply { putString("itemId", item.id) }
                            findNavController().navigate(R.id.itemDetailFragment, bundle)
                        }
                    } else {
                        card.visibility = View.GONE
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}