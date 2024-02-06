package com.practice.trello.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.practice.trello.R
import com.practice.trello.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private var binding: ActivityMainBinding? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.Theme_Trello)
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding?.root)

    }

}