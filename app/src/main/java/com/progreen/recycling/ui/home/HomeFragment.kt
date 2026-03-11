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
import com.progreen.recycling.ui.lgu.LguMapFragment
import com.progreen.recycling.ui.common.AppViewModelFactory
import com.progreen.recycling.ui.main.MainActivity
import com.progreen.recycling.ui.qr.UserQrFragment
import com.progreen.recycling.ui.scan.PlasticScanFragment

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
        binding.cardScanPlastic.setOnClickListener {
            (activity as? MainActivity)?.openOverlayFragment(PlasticScanFragment())
        }
        binding.cardLguMap.setOnClickListener {
            (activity as? MainActivity)?.openOverlayFragment(LguMapFragment())
        }
        binding.cardMyQr.setOnClickListener {
            (activity as? MainActivity)?.openOverlayFragment(UserQrFragment())
        }
    }

    override fun onResume() {
        super.onResume()
        binding.pointsValue.text = viewModel.getPoints().toString()
        binding.welcomeName.text = "Hi, ${viewModel.getUserName()}"
        val role = viewModel.getUserRoleLabel()
        binding.roleBadge.text = "$role DASHBOARD"

        val isUser = viewModel.isUserRole()
        binding.roleNotice.text = "$role dashboard is reserved for next phase. User dashboard is fully enabled now."
        binding.roleNotice.visibility = if (isUser) View.GONE else View.VISIBLE
        binding.cardCategories.visibility = if (isUser) View.VISIBLE else View.GONE
        binding.cardSubmit.visibility = if (isUser) View.VISIBLE else View.GONE
        binding.cardRewards.visibility = if (isUser) View.VISIBLE else View.GONE
        binding.cardHistory.visibility = if (isUser) View.VISIBLE else View.GONE
        binding.cardScanPlastic.visibility = if (isUser) View.VISIBLE else View.GONE
        binding.cardLguMap.visibility = if (isUser) View.VISIBLE else View.GONE
        binding.cardMyQr.visibility = if (isUser) View.VISIBLE else View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
