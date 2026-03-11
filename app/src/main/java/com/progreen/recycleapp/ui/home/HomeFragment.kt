package com.progreen.recycleapp.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.progreen.recycleapp.R
import com.progreen.recycleapp.databinding.FragmentHomeBinding
import com.progreen.recycleapp.model.DashboardAction
import com.progreen.recycleapp.ui.adapters.DashboardAdapter
import com.progreen.recycleapp.ui.common.AppViewModelFactory
import com.progreen.recycleapp.ui.common.Injection
import com.progreen.recycleapp.viewmodel.MainViewModel

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MainViewModel by activityViewModels {
        AppViewModelFactory(
            Injection.authRepository(requireActivity() as androidx.appcompat.app.AppCompatActivity),
            Injection.recyclingRepository(requireActivity() as androidx.appcompat.app.AppCompatActivity)
        )
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.rvDashboard.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.rvDashboard.adapter = DashboardAdapter(getActions()) { action ->
            when (action.route) {
                "categories" -> findNavController().navigate(R.id.action_home_to_categories)
                "submit" -> findNavController().navigate(R.id.action_home_to_submit)
                "rewards" -> findNavController().navigate(R.id.action_home_to_rewards)
                "history" -> findNavController().navigate(R.id.historyFragment)
            }
        }

        binding.tvWelcome.text = getString(R.string.welcome_user, viewModel.userName())

        viewModel.points.observe(viewLifecycleOwner) {
            binding.tvPoints.text = "$it"
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.refreshPoints()
    }

    private fun getActions(): List<DashboardAction> {
        return listOf(
            DashboardAction("Categories", "Browse recyclable types", R.drawable.ic_category, "categories"),
            DashboardAction("Submit", "Log your recyclables", R.drawable.ic_home, "submit"),
            DashboardAction("Rewards", "Redeem your points", R.drawable.ic_rewards, "rewards"),
            DashboardAction("History", "Track eco impact", R.drawable.ic_history, "history")
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
