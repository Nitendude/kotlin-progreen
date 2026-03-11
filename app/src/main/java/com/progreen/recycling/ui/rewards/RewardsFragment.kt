package com.progreen.recycling.ui.rewards

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.progreen.recycling.data.repository.AppRepository
import com.progreen.recycling.databinding.FragmentRewardsBinding
import com.progreen.recycling.ui.common.AppViewModelFactory
import com.progreen.recycling.util.toast

class RewardsFragment : Fragment() {

    private var _binding: FragmentRewardsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: RewardsViewModel by viewModels {
        AppViewModelFactory(AppRepository.getInstance(requireContext()))
    }

    private lateinit var adapter: RewardAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRewardsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adapter = RewardAdapter(
            pointsBalance = viewModel.getPoints(),
            items = viewModel.getRewards(),
            onRedeemClick = { reward ->
                val redemption = viewModel.redeem(reward.id)
                if (redemption.isSuccess) {
                    requireContext().toast("Redeemed ${reward.title}")
                    refreshPoints()
                } else {
                    requireContext().toast("Not enough points")
                }
            }
        )

        binding.rewardsRecycler.layoutManager = LinearLayoutManager(requireContext())
        binding.rewardsRecycler.adapter = adapter
        refreshPoints()
    }

    override fun onResume() {
        super.onResume()
        refreshPoints()
    }

    private fun refreshPoints() {
        val points = viewModel.getPoints()
        binding.rewardsPointsView.text = "Your Points: $points"
        if (::adapter.isInitialized) {
            adapter.updatePoints(points)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
