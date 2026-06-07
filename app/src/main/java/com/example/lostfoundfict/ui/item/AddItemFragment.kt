package com.example.lostfoundfict.ui.item

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.lostfoundfict.R
import com.example.lostfoundfict.databinding.FragmentAddItemBinding
import com.example.lostfoundfict.util.AnalyticsHelper
import com.example.lostfoundfict.viewmodel.ItemState
import com.example.lostfoundfict.viewmodel.ItemViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AddItemFragment : Fragment() {

    private var _binding: FragmentAddItemBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ItemViewModel by activityViewModels()

    private var photoUri: Uri? = null
    private var imageUrl: String = ""

    // Launcher за камера
    private val cameraLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            photoUri?.let { uri ->
                binding.ivPreview.visibility = View.VISIBLE
                Glide.with(this).load(uri).centerCrop().into(binding.ivPreview)
                imageUrl = uri.toString()
            }
        }
    }

    // Launcher за дозвола за камера
    private val cameraPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) openCamera()
        else Toast.makeText(requireContext(), getString(R.string.camera_permission_denied), Toast.LENGTH_SHORT).show()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentAddItemBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupCategoryDropdown()
        setupObservers()
        setupCameraButton()
        setupSubmitButton()
        setupTypeToggle()
    }

    private fun setupTypeToggle() {
        binding.radioGroup.setOnCheckedChangeListener { _, checkedId ->
            if (checkedId == R.id.rbFound) {
                binding.btnCamera.visibility = View.VISIBLE
                binding.tvCameraHint.visibility = View.VISIBLE
            } else {
                binding.btnCamera.visibility = View.GONE
                binding.tvCameraHint.visibility = View.GONE
                binding.ivPreview.visibility = View.GONE
            }
        }
    }

    private fun setupCameraButton() {
        binding.btnCamera.setOnClickListener {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
                openCamera()
            } else {
                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }

    private fun openCamera() {
        val photoFile = createImageFile()
        photoUri = FileProvider.getUriForFile(
            requireContext(),
            "${requireContext().packageName}.provider",
            photoFile
        )
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
            putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
        }
        cameraLauncher.launch(intent)
    }

    private fun createImageFile(): File {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile("JPEG_${timestamp}_", ".jpg", storageDir)
    }

    private fun setupCategoryDropdown() {
        val categories = resources.getStringArray(R.array.item_categories)
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, categories)
        binding.actvCategory.setAdapter(adapter)
    }

    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.itemState.collectLatest { state ->
                when (state) {
                    is ItemState.Loading -> {
                        binding.progressBar.visibility = View.VISIBLE
                        binding.btnSubmit.isEnabled = false
                    }
                    is ItemState.Success -> {
                        binding.progressBar.visibility = View.GONE
                        binding.btnSubmit.isEnabled = true
                        // Додадено точно како што побара Claude:
                        val type = if (binding.rbLost.isChecked) "lost" else "found"
                        val category = binding.actvCategory.text.toString()
                        AnalyticsHelper.logItemAdded(type, category)
                        Toast.makeText(requireContext(), getString(R.string.item_added_success), Toast.LENGTH_SHORT).show()
                        clearForm()
                        viewModel.resetState()
                        findNavController().navigate(R.id.itemListFragment)
                    }
                    is ItemState.Error -> {
                        binding.progressBar.visibility = View.GONE
                        binding.btnSubmit.isEnabled = true
                        Toast.makeText(requireContext(), state.message, Toast.LENGTH_LONG).show()
                        viewModel.resetState()
                    }
                    else -> {
                        binding.progressBar.visibility = View.GONE
                        binding.btnSubmit.isEnabled = true
                    }
                }
            }
        }
    }

    private fun setupSubmitButton() {
        binding.btnSubmit.setOnClickListener {
            val title = binding.etTitle.text.toString().trim()
            val description = binding.etDescription.text.toString().trim()
            val category = binding.actvCategory.text.toString().trim()
            val location = binding.etLocation.text.toString().trim()
            val type = if (binding.rbLost.isChecked) "lost" else "found"

            if (title.isEmpty() || description.isEmpty() || category.isEmpty()) {
                Toast.makeText(requireContext(), getString(R.string.error_empty_fields), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            viewModel.addItem(
                title = title,
                description = description,
                type = type,
                category = category,
                imageUrl = imageUrl,
                locationName = location
            )
        }
    }

    private fun clearForm() {
        binding.etTitle.text?.clear()
        binding.etDescription.text?.clear()
        binding.actvCategory.text?.clear()
        binding.etLocation.text?.clear()
        binding.rbLost.isChecked = true
        binding.ivPreview.visibility = View.GONE
        imageUrl = ""
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}