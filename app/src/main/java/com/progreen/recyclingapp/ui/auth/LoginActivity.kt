package com.progreen.recyclingapp.ui.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.progreen.recyclingapp.R
import com.progreen.recyclingapp.data.local.PrefsManager
import com.progreen.recyclingapp.databinding.ActivityLoginBinding
import com.progreen.recyclingapp.ui.main.MainActivity

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var prefsManager: PrefsManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        prefsManager = PrefsManager(this)

        animateLoginScreen()

        binding.loginButton.setOnClickListener {
            val email = binding.emailInput.text.toString().trim()
            val password = binding.passwordInput.text.toString().trim()

            if (email.isBlank() || password.isBlank()) {
                Toast.makeText(this, "Enter email and password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (prefsManager.login(email, password)) {
                startActivity(Intent(this, MainActivity::class.java))
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                finish()
            } else {
                Toast.makeText(
                    this,
                    "Invalid credentials. Use registered account or demo@recycle.com / 123456",
                    Toast.LENGTH_LONG
                ).show()
            }
        }

        binding.registerCta.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }
    }

    private fun animateLoginScreen() {
        binding.loginCard.alpha = 0f
        binding.loginCard.translationY = 80f
        binding.loginCard.animate().alpha(1f).translationY(0f).setDuration(600).start()

        binding.loginLogo.scaleX = 0.8f
        binding.loginLogo.scaleY = 0.8f
        binding.loginLogo.animate().scaleX(1f).scaleY(1f).setDuration(450).start()
    }
}
