package com.progreen.recycling.ui.splash

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import com.progreen.recycling.R
import com.progreen.recycling.data.repository.AppRepository
import com.progreen.recycling.databinding.ActivitySplashBinding
import com.progreen.recycling.ui.auth.LoginActivity
import com.progreen.recycling.ui.main.MainActivity

class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.logoView.startAnimation(AnimationUtils.loadAnimation(this, R.anim.scale_pop))
        binding.titleView.startAnimation(AnimationUtils.loadAnimation(this, R.anim.fade_in))

        Handler(Looper.getMainLooper()).postDelayed({
            val repository = AppRepository.getInstance(this)
            val destination = if (repository.isLoggedIn()) MainActivity::class.java else LoginActivity::class.java
            startActivity(Intent(this, destination))
            overridePendingTransition(R.anim.activity_enter, R.anim.activity_exit)
            finish()
        }, 1600)
    }
}
