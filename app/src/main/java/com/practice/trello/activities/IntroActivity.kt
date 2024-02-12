package com.practice.trello.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.practice.trello.databinding.ActivityIntroBinding
import com.practice.trello.firebase.FireStoreClass

class IntroActivity : AppCompatActivity() {
    private lateinit var binding: ActivityIntroBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityIntroBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.introBtnSignIn.setOnClickListener {
            val intent = Intent(this, SignInActivity::class.java)
            startActivity(intent)
        }

        binding.introBtnSignUp.setOnClickListener {
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
        }
        binding.skipButton.setOnClickListener {
            val currentUserId = FireStoreClass().getCurrentUserId()
            if (currentUserId.isNotBlank())
                startActivity(Intent(this, MainActivity::class.java))
        }
    }
}