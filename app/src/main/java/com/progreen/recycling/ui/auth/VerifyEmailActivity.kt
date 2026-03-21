package com.progreen.recycling.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import com.progreen.recycling.R
import com.progreen.recycling.data.repository.AppRepository
import com.progreen.recycling.databinding.ActivityVerifyEmailBinding
import com.progreen.recycling.ui.main.MainActivity
import com.progreen.recycling.util.toast

class VerifyEmailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityVerifyEmailBinding
    private val repository by lazy { AppRepository.getInstance(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVerifyEmailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val email = intent.getStringExtra(EXTRA_EMAIL)
            ?: repository.getPendingVerificationEmail()
            ?: ""

        binding.verifyCard.startAnimation(AnimationUtils.loadAnimation(this, R.anim.slide_up_fade))
        binding.verifyEmailLabel.text = email

        binding.verifyButton.setOnClickListener {
            val otp = binding.otpInput.text?.toString()?.trim().orEmpty()
            if (email.isBlank() || otp.length < 4) {
                toast("Enter the OTP sent to your email")
                return@setOnClickListener
            }

            if (repository.verifyOtp(email, otp)) {
                toast("Email verified")
                startActivity(Intent(this, MainActivity::class.java))
                finishAffinity()
                overridePendingTransition(R.anim.activity_enter, R.anim.activity_exit)
            } else {
                toast(repository.getLastErrorMessage() ?: "Verification failed")
            }
        }

        binding.resendOtpButton.setOnClickListener {
            if (email.isBlank()) {
                toast("Missing email address")
                return@setOnClickListener
            }

            if (repository.resendOtp(email)) {
                toast("OTP resent")
            } else {
                toast(repository.getLastErrorMessage() ?: "Could not resend OTP")
            }
        }

        binding.backToLoginCta.setOnClickListener {
            finish()
            overridePendingTransition(R.anim.activity_pop_enter, R.anim.activity_pop_exit)
        }
    }

    companion object {
        const val EXTRA_EMAIL = "extra_email"
    }
}
