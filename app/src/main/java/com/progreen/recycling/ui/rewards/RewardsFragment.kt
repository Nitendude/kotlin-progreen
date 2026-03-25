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
import com.progreen.recycling.ui.main.MainActivity
import com.progreen.recycling.ui.redemptions.RedemptionHistoryFragment
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
                    val code = redemption.getOrNull()
                    val message = if (code.isNullOrBlank()) {
                        "Redeemed ${reward.title}"
                    } else {
                        "Redeemed ${reward.title}. Claim code: $code"
                    }
                    requireContext().toast(message)
                    refreshPoints()
                } else {
                    requireContext().toast("Not enough points")
                }
            }
        )

        binding.rewardsRecycler.layoutManager = LinearLayoutManager(requireContext())
        binding.rewardsRecycler.adapter = adapter
        binding.openRedemptionHistoryButton.setOnClickListener {
            (activity as? MainActivity)?.openOverlayFragment(RedemptionHistoryFragment())
        }
        refreshPoints()
    }

    override fun onResume() {
        super.onResume()
        refreshPoints()
    }

    private fun refreshPoints() {
        val points = viewModel.getPoints()
        val rewards = viewModel.getRewards().sortedWith(
            compareByDescending<com.progreen.recycling.data.model.RewardItem> { points >= it.costPoints }
                .thenBy { it.costPoints }
        )
        val redeemable = rewards.count { points >= it.costPoints }

        binding.rewardsPointsView.text = "Your Points: $points"
        binding.rewardsSummaryView.text = "$redeemable rewards available with your current points"
        if (::adapter.isInitialized) {
            adapter.updateData(points, rewards)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
