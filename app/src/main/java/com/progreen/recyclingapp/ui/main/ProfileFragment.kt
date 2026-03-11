package com.progreen.recyclingapp.ui.main

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.progreen.recyclingapp.R
import com.progreen.recyclingapp.data.local.PrefsManager
import com.progreen.recyclingapp.databinding.FragmentProfileBinding
import com.progreen.recyclingapp.ui.auth.LoginActivity

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private lateinit var prefsManager: PrefsManager

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
        prefsManager = PrefsManager(requireContext())

        binding.historyButton.setOnClickListener {
            findNavController().navigate(R.id.historyFragment)
        }

        binding.logoutButton.setOnClickListener {
            prefsManager.logout()
            val intent = Intent(requireContext(), LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            requireActivity().overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
        }
    }

    override fun onResume() {
        super.onResume()
        binding.profileName.text = prefsManager.getUserName()
        binding.profileEmail.text = prefsManager.getUserEmail()
        binding.profilePoints.text = "Points: ${prefsManager.getPoints()}"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
