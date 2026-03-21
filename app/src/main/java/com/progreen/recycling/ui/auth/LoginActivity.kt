package com.progreen.recycling.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import com.progreen.recycling.R
import com.progreen.recycling.data.repository.AppRepository
import com.progreen.recycling.databinding.ActivityLoginBinding
import com.progreen.recycling.ui.main.MainActivity
import com.progreen.recycling.util.toast

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val repository by lazy { AppRepository.getInstance(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.loginLogo.startAnimation(AnimationUtils.loadAnimation(this, R.anim.slide_up_fade))
        binding.loginCard.startAnimation(AnimationUtils.loadAnimation(this, R.anim.fade_in))

        binding.loginButton.setOnClickListener {
            val email = binding.emailInput.text?.toString()?.trim().orEmpty()
            val password = binding.passwordInput.text?.toString()?.trim().orEmpty()

            if (email.isBlank() || password.isBlank()) {
                toast("Email and password are required")
                return@setOnClickListener
            }

            if (repository.login(email, password)) {
                startActivity(Intent(this, MainActivity::class.java))
                overridePendingTransition(R.anim.activity_enter, R.anim.activity_exit)
                finish()
            } else {
                toast(repository.getLastErrorMessage() ?: "Invalid credentials")
            }
        }

        binding.registerCta.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
            overridePendingTransition(R.anim.activity_enter, R.anim.activity_exit)
        }

        binding.verifyCta.setOnClickListener {
            val pendingEmail = repository.getPendingVerificationEmail()
            if (pendingEmail.isNullOrBlank()) {
                toast("No pending verification email found")
            } else {
                startActivity(
                    Intent(this, VerifyEmailActivity::class.java)
                        .putExtra(VerifyEmailActivity.EXTRA_EMAIL, pendingEmail)
                )
                overridePendingTransition(R.anim.activity_enter, R.anim.activity_exit)
            }
        }
    }
}
