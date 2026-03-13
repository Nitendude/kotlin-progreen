package com.progreen.recycling.ui.main

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.progreen.recycling.R
import com.progreen.recycling.data.model.UserRole
import com.progreen.recycling.data.repository.AppRepository
import com.progreen.recycling.databinding.ActivityMainBinding
import com.progreen.recycling.ui.categories.CategoriesFragment
import com.progreen.recycling.ui.history.HistoryFragment
import com.progreen.recycling.ui.home.HomeFragment
import com.progreen.recycling.ui.lgu.LguDashboardFragment
import com.progreen.recycling.ui.profile.ProfileFragment
import com.progreen.recycling.ui.rewards.RewardsFragment
import com.progreen.recycling.ui.submit.SubmitFragment

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (savedInstanceState == null) {
            openFragment(createHomeFragment())
        }

        binding.bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> openFragment(createHomeFragment())
                R.id.nav_categories -> openFragment(CategoriesFragment())
                R.id.nav_submit -> openFragment(SubmitFragment())
                R.id.nav_rewards -> openFragment(RewardsFragment())
                R.id.nav_profile -> openFragment(ProfileFragment())
            }
            true
        }
    }

    fun selectTab(tabId: Int) {
        binding.bottomNav.selectedItemId = tabId
    }

    fun openHistoryScreen() {
        openOverlayFragment(HistoryFragment())
    }

    fun openOverlayFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .addToBackStack(null)
            .commit()
    }

    private fun openFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }

    private fun createHomeFragment(): Fragment {
        val role = AppRepository.getInstance(this).getUserRole()
        return if (role == UserRole.LGU) {
            LguDashboardFragment()
        } else {
            HomeFragment()
        }
    }
}
