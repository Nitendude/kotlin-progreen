package com.progreen.recycling.ui.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.fragment.app.Fragment
import com.progreen.recycling.R
import com.progreen.recycling.data.repository.AppRepository
import com.progreen.recycling.databinding.FragmentAdminDashboardBinding
import com.progreen.recycling.ui.main.MainActivity

class AdminDashboardFragment : Fragment() {

    private var _binding: FragmentAdminDashboardBinding? = null
    private val binding get() = _binding!!

    private lateinit var repository: AppRepository

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentAdminDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        repository = AppRepository.getInstance(requireContext())
        binding.adminTitle.text = "${repository.getUserName()} Admin Dashboard"
        binding.root.startAnimation(AnimationUtils.loadAnimation(requireContext(), R.anim.slide_up_fade))

        binding.manageCategoriesButton.setOnClickListener {
            (activity as? MainActivity)?.selectTab(R.id.nav_categories)
        }
        binding.reviewRewardsButton.setOnClickListener {
            (activity as? MainActivity)?.selectTab(R.id.nav_rewards)
        }
        binding.openHistoryButton.setOnClickListener {
            (activity as? MainActivity)?.openHistoryScreen()
        }
        binding.openProfileButton.setOnClickListener {
            (activity as? MainActivity)?.selectTab(R.id.nav_profile)
        }

        refreshDashboard()
    }

    override fun onResume() {
        super.onResume()
        refreshDashboard()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun refreshDashboard() {
        binding.categoryCountValue.text = repository.getCategories().size.toString()
        binding.rewardCountValue.text = repository.getRewards().size.toString()
        binding.siteCountValue.text = repository.getCollectionSiteCount().toString()
        binding.materialCountValue.text = repository.getAcceptedPlasticTypes().size.toString()
    }
}
