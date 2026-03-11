package com.progreen.recyclingapp.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.progreen.recyclingapp.data.local.PrefsManager
import com.progreen.recyclingapp.data.repository.RecyclingRepository
import com.progreen.recyclingapp.databinding.FragmentRewardsBinding
import com.progreen.recyclingapp.ui.adapters.RewardAdapter

class RewardsFragment : Fragment() {

    private var _binding: FragmentRewardsBinding? = null
    private val binding get() = _binding!!

    private lateinit var repository: RecyclingRepository
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
        repository = RecyclingRepository(PrefsManager(requireContext()))

        adapter = RewardAdapter(onRedeemClick = { reward ->
            val success = repository.redeemReward(reward)
            if (success) {
                Toast.makeText(requireContext(), "Redeemed ${reward.name}", Toast.LENGTH_SHORT).show()
                updatePoints()
            } else {
                Toast.makeText(requireContext(), "Not enough points", Toast.LENGTH_SHORT).show()
            }
        })

        binding.rewardsRecycler.layoutManager = LinearLayoutManager(requireContext())
        binding.rewardsRecycler.adapter = adapter
        adapter.submitList(repository.getRewards())

        updatePoints()
    }

    override fun onResume() {
        super.onResume()
        updatePoints()
    }

    private fun updatePoints() {
        binding.rewardsPointsView.text = "Your Points: ${repository.getPoints()}"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
