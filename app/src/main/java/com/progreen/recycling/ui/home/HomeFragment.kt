package com.progreen.recycling.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.progreen.recycling.R
import com.progreen.recycling.data.repository.AppRepository
import com.progreen.recycling.databinding.FragmentHomeBinding
import com.progreen.recycling.ui.common.AppViewModelFactory
import com.progreen.recycling.ui.main.MainActivity

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HomeViewModel by viewModels {
        AppViewModelFactory(AppRepository.getInstance(requireContext()))
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.pointsCard.startAnimation(AnimationUtils.loadAnimation(requireContext(), R.anim.slide_up_fade))

        binding.cardCategories.setOnClickListener { (activity as? MainActivity)?.selectTab(R.id.nav_categories) }
        binding.cardSubmit.setOnClickListener { (activity as? MainActivity)?.selectTab(R.id.nav_submit) }
        binding.cardRewards.setOnClickListener { (activity as? MainActivity)?.selectTab(R.id.nav_rewards) }
        binding.cardHistory.setOnClickListener { (activity as? MainActivity)?.openHistoryScreen() }
    }

    override fun onResume() {
        super.onResume()
        binding.pointsValue.text = viewModel.getPoints().toString()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
