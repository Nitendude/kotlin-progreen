package com.progreen.recycling.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import com.progreen.recycling.R
import com.progreen.recycling.data.model.User
import com.progreen.recycling.data.repository.AppRepository
import com.progreen.recycling.databinding.ActivityRegisterBinding
import com.progreen.recycling.util.toast

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private val repository by lazy { AppRepository.getInstance(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.registerCard.startAnimation(AnimationUtils.loadAnimation(this, R.anim.slide_up_fade))

        binding.createAccountButton.setOnClickListener {
            val name = binding.nameInput.text?.toString()?.trim().orEmpty()
            val email = binding.registerEmailInput.text?.toString()?.trim().orEmpty()
            val password = binding.registerPasswordInput.text?.toString()?.trim().orEmpty()

            if (name.isBlank() || email.isBlank() || password.isBlank()) {
                toast("All fields are required")
                return@setOnClickListener
            }

            if (repository.register(User(name = name, email = email, password = password))) {
                toast("OTP sent to your email")
                startActivity(
                    Intent(this, VerifyEmailActivity::class.java)
                        .putExtra(VerifyEmailActivity.EXTRA_EMAIL, email)
                )
                finish()
                overridePendingTransition(R.anim.activity_enter, R.anim.activity_exit)
            } else {
                toast(repository.getLastErrorMessage() ?: "Could not create account")
            }
        }

        binding.loginCta.setOnClickListener {
            finish()
            overridePendingTransition(R.anim.activity_pop_enter, R.anim.activity_pop_exit)
        }

        binding.applyLguButton.setOnClickListener {
            startRoleApplication("LGU")
        }

        binding.applyCompanyButton.setOnClickListener {
            startRoleApplication("COMPANY")
        }
    }

    private fun startRoleApplication(applicationType: String) {
        startActivity(
            Intent(this, RoleApplicationActivity::class.java)
                .putExtra(RoleApplicationActivity.EXTRA_APPLICATION_TYPE, applicationType)
        )
        overridePendingTransition(R.anim.activity_enter, R.anim.activity_exit)
    }
}
