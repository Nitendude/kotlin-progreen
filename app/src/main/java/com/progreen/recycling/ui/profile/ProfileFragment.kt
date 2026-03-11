package com.progreen.recycling.ui.profile

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.progreen.recycling.data.repository.AppRepository
import com.progreen.recycling.databinding.FragmentProfileBinding
import com.progreen.recycling.ui.auth.LoginActivity
import com.progreen.recycling.ui.common.AppViewModelFactory
import com.progreen.recycling.ui.main.MainActivity

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ProfileViewModel by viewModels {
        AppViewModelFactory(AppRepository.getInstance(requireContext()))
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.historyButton.setOnClickListener {
            (activity as? MainActivity)?.openHistoryScreen()
        }

        binding.logoutButton.setOnClickListener {
            viewModel.logout()
            startActivity(Intent(requireContext(), LoginActivity::class.java))
            requireActivity().finish()
        }
    }

    override fun onResume() {
        super.onResume()
        binding.profileName.text = viewModel.getName()
        binding.profileEmail.text = viewModel.getEmail()
        binding.profilePoints.text =
            "Role: ${viewModel.getRoleLabel()} | Points: ${viewModel.getPoints()} | Submissions: ${viewModel.getSubmissionCount()}"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
