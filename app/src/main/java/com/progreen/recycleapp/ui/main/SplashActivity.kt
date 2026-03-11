package com.progreen.recycleapp.ui.main

import android.content.Intent
import android.os.Bundle
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import com.progreen.recycleapp.R
import com.progreen.recycleapp.databinding.ActivitySplashBinding
import com.progreen.recycleapp.ui.auth.AuthActivity
import com.progreen.recycleapp.ui.common.Injection

class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val anim = AnimationUtils.loadAnimation(this, R.anim.splash_logo_in)
        binding.logoContainer.startAnimation(anim)

        binding.root.postDelayed({
            val authRepository = Injection.authRepository(this)
            val next = if (authRepository.isLoggedIn()) MainActivity::class.java else AuthActivity::class.java
            startActivity(Intent(this, next))
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
            finish()
        }, 1400)
    }
}
