package com.example.lostfoundfict.ui.auth

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.example.lostfoundfict.R
import com.example.lostfoundfict.databinding.ActivityAuthBinding
import com.example.lostfoundfict.ui.MainActivity
import com.google.firebase.auth.FirebaseAuth

class AuthActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAuthBinding
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Ако веќе е логиран — директно на MainActivity
        if (FirebaseAuth.getInstance().currentUser != null) {
            goToMain()
            return
        }

        binding = ActivityAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.auth_nav_host) as NavHostFragment
        navController = navHostFragment.navController
    }

    fun goToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}