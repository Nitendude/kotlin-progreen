package com.progreen.recyclingapp.ui.splash

import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.progreen.recyclingapp.R
import com.progreen.recyclingapp.data.local.PrefsManager
import com.progreen.recyclingapp.databinding.ActivitySplashBinding
import com.progreen.recyclingapp.ui.auth.LoginActivity
import com.progreen.recyclingapp.ui.main.MainActivity

class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        playAnimation()

        Handler(Looper.getMainLooper()).postDelayed({
            val prefs = PrefsManager(this)
            val intent = if (prefs.isLoggedIn()) {
                Intent(this, MainActivity::class.java)
            } else {
                Intent(this, LoginActivity::class.java)
            }
            startActivity(intent)
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
            finish()
        }, 1800)
    }

    private fun playAnimation() {
        val logoAnim = ObjectAnimator.ofPropertyValuesHolder(
            binding.logoView,
            PropertyValuesHolder.ofFloat("scaleX", 0.7f, 1f),
            PropertyValuesHolder.ofFloat("scaleY", 0.7f, 1f),
            PropertyValuesHolder.ofFloat("alpha", 0.4f, 1f)
        )
        logoAnim.duration = 900
        logoAnim.start()

        binding.titleView.alpha = 0f
        binding.subtitleView.alpha = 0f
        binding.titleView.animate().alpha(1f).setDuration(700).setStartDelay(300).start()
        binding.subtitleView.animate().alpha(1f).setDuration(700).setStartDelay(500).start()
    }
}
