package com.progreen.recyclingapp.ui.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.progreen.recyclingapp.R
import com.progreen.recyclingapp.data.local.PrefsManager
import com.progreen.recyclingapp.databinding.ActivityRegisterBinding
import com.progreen.recyclingapp.model.User
import com.progreen.recyclingapp.ui.main.MainActivity

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var prefsManager: PrefsManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        prefsManager = PrefsManager(this)

        binding.createAccountButton.setOnClickListener {
            val name = binding.nameInput.text.toString().trim()
            val email = binding.registerEmailInput.text.toString().trim()
            val password = binding.registerPasswordInput.text.toString().trim()

            if (name.isBlank() || email.isBlank() || password.length < 4) {
                Toast.makeText(this, "Fill all fields (password min 4 chars)", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            prefsManager.saveUser(User(name, email, password))
            Toast.makeText(this, "Account created", Toast.LENGTH_SHORT).show()

            startActivity(Intent(this, MainActivity::class.java))
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
            finishAffinity()
        }

        binding.loginCta.setOnClickListener {
            finish()
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
        }
    }
}
