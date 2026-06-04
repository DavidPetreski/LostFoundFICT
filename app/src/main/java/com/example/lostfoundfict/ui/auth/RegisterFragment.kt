package com.example.lostfoundfict.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.lostfoundfict.R
import com.example.lostfoundfict.databinding.FragmentRegisterBinding
import com.example.lostfoundfict.viewmodel.AuthState
import com.example.lostfoundfict.viewmodel.AuthViewModel

class RegisterFragment : Fragment() {

    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AuthViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.authState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is AuthState.Loading -> binding.progressBar.visibility = View.VISIBLE
                is AuthState.Success -> (requireActivity() as AuthActivity).goToMain()
                is AuthState.Error -> {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(requireContext(), state.message, Toast.LENGTH_LONG).show()
                }
                else -> binding.progressBar.visibility = View.GONE
            }
        }

        binding.btnRegister.setOnClickListener {
            val name = binding.etName.text.toString().trim()
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()
            val confirm = binding.etConfirmPassword.text.toString().trim()

            when {
                name.isEmpty() || email.isEmpty() || password.isEmpty() ->
                    Toast.makeText(requireContext(), getString(R.string.error_empty_fields), Toast.LENGTH_SHORT).show()
                password != confirm ->
                    Toast.makeText(requireContext(), getString(R.string.error_password_mismatch), Toast.LENGTH_SHORT).show()
                password.length < 6 ->
                    Toast.makeText(requireContext(), getString(R.string.error_password_short), Toast.LENGTH_SHORT).show()
                else -> viewModel.registerWithEmail(email, password)
            }
        }

        binding.tvLogin.setOnClickListener {
            findNavController().navigate(R.id.action_register_to_login)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}