package com.progreen.recycling.ui.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.progreen.recycling.R
import com.progreen.recycling.data.repository.AppRepository
import com.progreen.recycling.databinding.FragmentAdminDashboardBinding
import com.progreen.recycling.ui.main.MainActivity
import com.progreen.recycling.util.toast

class AdminDashboardFragment : Fragment() {

    private var _binding: FragmentAdminDashboardBinding? = null
    private val binding get() = _binding!!

    private lateinit var repository: AppRepository
    private lateinit var pendingAdapter: PendingAccountAdapter
    private lateinit var applicationAdapter: PendingApplicationAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentAdminDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        repository = AppRepository.getInstance(requireContext())
        binding.adminTitle.text = "${repository.getUserName()} Admin Dashboard"
        binding.root.startAnimation(AnimationUtils.loadAnimation(requireContext(), R.anim.slide_up_fade))

        pendingAdapter = PendingAccountAdapter(
            items = emptyList(),
            onApprove = { account -> updateApproval(account.id, "APPROVED") },
            onReject = { account -> updateApproval(account.id, "REJECTED") }
        )
        binding.pendingAccountsRecycler.layoutManager = LinearLayoutManager(requireContext())
        binding.pendingAccountsRecycler.adapter = pendingAdapter

        applicationAdapter = PendingApplicationAdapter(
            items = emptyList(),
            onApprove = { application -> reviewApplication(application.id, "APPROVED") },
            onReject = { application -> reviewApplication(application.id, "REJECTED") }
        )
        binding.pendingApplicationsRecycler.layoutManager = LinearLayoutManager(requireContext())
        binding.pendingApplicationsRecycler.adapter = applicationAdapter

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
        val stats = repository.getAdminDashboardStats()
        binding.categoryCountValue.text = stats.usersCount.toString()
        binding.rewardCountValue.text = stats.pendingCount.toString()
        binding.siteCountValue.text = stats.lgusCount.toString()
        binding.materialCountValue.text = stats.companiesCount.toString()
        pendingAdapter.update(repository.getPendingAccounts())
        applicationAdapter.update(repository.getPendingApplications())
    }

    private fun updateApproval(userId: Long, approvalStatus: String) {
        val result = repository.updateAccountApproval(userId, approvalStatus)
        if (result.isSuccess) {
            requireContext().toast("Account updated")
            refreshDashboard()
        } else {
            requireContext().toast(result.exceptionOrNull()?.message ?: "Could not update account")
        }
    }

    private fun reviewApplication(applicationId: Long, decision: String) {
        val result = repository.reviewRoleApplication(applicationId, decision)
        if (result.isSuccess) {
            requireContext().toast("Application reviewed")
            refreshDashboard()
        } else {
            requireContext().toast(result.exceptionOrNull()?.message ?: "Could not review application")
        }
    }
}
