package com.practice.trello.activities

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.WindowManager
import android.widget.Toast
import androidx.core.widget.doOnTextChanged
import com.google.firebase.auth.FirebaseAuth
import com.practice.trello.R
import com.practice.trello.databinding.ActivitySignInBinding
import com.practice.trello.firebase.FireStoreClass
import com.practice.trello.models.User

class SignInActivity : BaseActivity() {
    private var binding: ActivitySignInBinding? = null
    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignInBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        auth = FirebaseAuth.getInstance()

        setupActionbar()

        binding?.signInBtnSignIn?.setOnClickListener { signInRegisteredUser() }

        binding?.signInEtEmail?.doOnTextChanged { _, _, _, _ -> removeError() }
        binding?.signInEtPassword?.doOnTextChanged { _, _, _, _ -> removeError() }
    }

    private fun removeError() {
        binding?.signInTilEmail?.setErrorMessage("")
        binding?.signInTilPassword?.setErrorMessage("")
    }

    private fun setupActionbar() {
        setSupportActionBar(binding?.signInToolBar)
        if (supportActionBar != null) {
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
            supportActionBar!!.setHomeAsUpIndicator(R.drawable.baseline_arrow_back)
        }
        binding?.signInToolBar?.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun validateForm(email: String, password: String): Boolean {
        return when {
            email.isBlank() -> {
                binding?.signInTilEmail?.setErrorMessage("Please enter an email.")
                false
            }

            !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                binding?.signInTilEmail?.setErrorMessage("Enter valid email.")
                false
            }

            password.isBlank() -> {
                binding?.signInTilPassword?.setErrorMessage("Please enter a password.")
                false
            }

            password.length < 4 -> {
                binding?.signInTilPassword?.setErrorMessage("Password must be 5 or more character long.")
                false
            }

            else -> true
        }
    }

    private fun signInRegisteredUser() {
        val email: String = binding?.signInEtEmail?.text.toString().trim { it <= ' ' }
        val password: String = binding?.signInEtPassword?.text.toString().trim { it <= ' ' }

        if (validateForm(email, password)) {
            showProgressDialog(resources.getString(R.string.progress_please_wait))
            auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this) { task ->
                hideProgressDialog()
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    FireStoreClass().signInUser(this)
                } else {
                    Toast.makeText(
                        this,
                        "Registration failed",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    fun signInSuccess(user: User) {
        hideProgressDialog()
        Toast.makeText(this, "Welcome back to Trello", Toast.LENGTH_SHORT).show()
        startActivity(Intent(this, MainActivity::class.java))
    }
}