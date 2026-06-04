package com.example.lostfoundfict.ui.auth

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.lostfoundfict.R
import com.example.lostfoundfict.databinding.FragmentLoginBinding
import com.example.lostfoundfict.util.AnalyticsHelper
import com.example.lostfoundfict.viewmodel.AuthState
import com.example.lostfoundfict.viewmodel.AuthViewModel
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AuthViewModel by activityViewModels()

    // Facebook
    private lateinit var callbackManager: CallbackManager

    // Google Sign-In launcher
    private val googleSignInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.result
                viewModel.signInWithGoogle(account)
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Google грешка: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        callbackManager = CallbackManager.Factory.create()
        setupObservers()
        setupClickListeners()
        setupFacebook()
    }

    private fun setupObservers() {
        viewModel.authState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is AuthState.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.btnLogin.isEnabled = false
                }
                is AuthState.Success -> {
                    binding.progressBar.visibility = View.GONE
                    // Додадено точно како што побара Claude:
                    AnalyticsHelper.logLogin("email")
                    (requireActivity() as AuthActivity).goToMain()
                }
                is AuthState.Error -> {
                    binding.progressBar.visibility = View.GONE
                    binding.btnLogin.isEnabled = true
                    Toast.makeText(requireContext(), state.message, Toast.LENGTH_LONG).show()
                }
                else -> {
                    binding.progressBar.visibility = View.GONE
                    binding.btnLogin.isEnabled = true
                }
            }
        }
    }

    private fun setupClickListeners() {
        // Email/Password Login
        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()
            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(requireContext(), getString(R.string.error_empty_fields), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            viewModel.loginWithEmail(email, password)
        }

        // Anonymous
        binding.btnAnonymous.setOnClickListener {
            viewModel.loginAnonymously()
        }

        // Google
        binding.btnGoogle.setOnClickListener {
            signInWithGoogle()
        }

        // Оди на Register
        binding.tvRegister.setOnClickListener {
            findNavController().navigate(R.id.action_login_to_register)
        }
    }

    private fun signInWithGoogle() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        val client = GoogleSignIn.getClient(requireActivity(), gso)
        googleSignInLauncher.launch(client.signInIntent)
    }

    private fun setupFacebook() {
        binding.btnFacebook.setPermissions("email", "public_profile")
        binding.btnFacebook.registerCallback(callbackManager, object : FacebookCallback<LoginResult> {
            override fun onSuccess(result: LoginResult) {
                viewModel.signInWithFacebook(result.accessToken.token)
            }
            override fun onCancel() {}
            override fun onError(error: FacebookException) {
                Toast.makeText(requireContext(), "Facebook: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}