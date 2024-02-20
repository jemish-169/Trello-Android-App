package com.practice.trello.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import com.practice.trello.R
import com.practice.trello.databinding.ActivitySplashBinding
import com.practice.trello.firebase.FireStoreClass

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySplashBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.Base_Theme_Trello)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)
        Handler().postDelayed({

            val currentUserId = FireStoreClass().getCurrentUserId()
            if (currentUserId.isNotBlank() || currentUserId.isNotEmpty())
                startActivity(Intent(this, MainActivity::class.java))
            else
                startActivity(Intent(this, IntroActivity::class.java))
            finish()
        }, 2500)
    }
}