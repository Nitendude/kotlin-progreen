package com.progreen.recycleapp.ui.rewards

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.progreen.recycleapp.databinding.FragmentRewardsBinding
import com.progreen.recycleapp.ui.adapters.RewardAdapter
import com.progreen.recycleapp.ui.common.AppViewModelFactory
import com.progreen.recycleapp.ui.common.Injection
import com.progreen.recycleapp.utils.toast
import com.progreen.recycleapp.viewmodel.MainViewModel

class RewardsFragment : Fragment() {

    private var _binding: FragmentRewardsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MainViewModel by activityViewModels {
        AppViewModelFactory(
            Injection.authRepository(requireActivity() as androidx.appcompat.app.AppCompatActivity),
            Injection.recyclingRepository(requireActivity() as androidx.appcompat.app.AppCompatActivity)
        )
    }

    private lateinit var adapter: RewardAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentRewardsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = RewardAdapter(viewModel.points.value ?: 0, viewModel.rewards()) { reward ->
            val result = viewModel.redeem(reward)
            result.onSuccess {
                requireContext().toast("Reward redeemed")
                viewModel.refreshPoints()
            }.onFailure {
                requireContext().toast(it.message ?: "Redemption failed")
            }
        }

        binding.rvRewards.layoutManager = LinearLayoutManager(requireContext())
        binding.rvRewards.adapter = adapter

        viewModel.points.observe(viewLifecycleOwner) { points ->
            binding.tvBalance.text = "Current balance: $points pts"
            adapter.updatePoints(points)
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.refreshPoints()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
