package com.example.lostfoundfict.ui.profile

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.example.lostfoundfict.R
import com.example.lostfoundfict.databinding.FragmentProfileBinding
import com.example.lostfoundfict.ui.auth.AuthActivity
import com.example.lostfoundfict.viewmodel.ItemViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ItemViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUserInfo()
        setupStats()
        setupNightMode()
        setupSignOut()
    }

    private fun setupUserInfo() {
        val user = FirebaseAuth.getInstance().currentUser
        if (user?.isAnonymous == true) {
            binding.tvProfileName.text = getString(R.string.guest)
            binding.tvProfileEmail.text = getString(R.string.guest_desc)
            binding.tvAvatar.text = "👤"
        } else {
            val name = user?.displayName?.takeIf { it.isNotEmpty() }
                ?: user?.email?.split("@")?.get(0)?.takeIf { it.isNotEmpty() }
                ?: "U"
            binding.tvProfileName.text = name
            binding.tvProfileEmail.text = user?.email ?: ""
            binding.tvAvatar.text = name.first().uppercaseChar().toString()
        }
    }

    private fun setupStats() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.allItems.collectLatest { items ->
                val myItems = items.filter { it.userId == userId }
                binding.tvMyItemsCount.text = myItems.size.toString()
                binding.tvMyLostCount.text = myItems.count { it.type == "lost" }.toString()
                binding.tvMyFoundCount.text = myItems.count { it.type == "found" }.toString()
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.savedItems.collectLatest { saved ->
                binding.tvMySavedCount.text = saved.size.toString()
            }
        }
    }

    private fun setupNightMode() {
        val prefs = requireContext().getSharedPreferences("settings", 0)
        val isNightMode = prefs.getBoolean("night_mode", false)

        // Постави го зачуваниот режим
        binding.switchNightMode.isChecked = isNightMode

        binding.switchNightMode.setOnCheckedChangeListener { _, isChecked ->
            // Зачувај ја преференцата
            prefs.edit().putBoolean("night_mode", isChecked).apply()

            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
        }
    }

    private fun setupSignOut() {
        binding.btnSignOut.setOnClickListener {
            FirebaseAuth.getInstance().signOut()

            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build()

            val googleClient = GoogleSignIn.getClient(requireActivity(), gso)
            googleClient.signOut().addOnCompleteListener {
                val intent = Intent(requireActivity(), AuthActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}