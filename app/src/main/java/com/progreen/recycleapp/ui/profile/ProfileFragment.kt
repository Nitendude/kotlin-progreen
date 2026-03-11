package com.progreen.recycleapp.ui.profile

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.progreen.recycleapp.data.repository.AuthRepository
import com.progreen.recycleapp.databinding.FragmentProfileBinding
import com.progreen.recycleapp.ui.auth.AuthActivity
import com.progreen.recycleapp.ui.common.AppViewModelFactory
import com.progreen.recycleapp.ui.common.Injection
import com.progreen.recycleapp.viewmodel.MainViewModel

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private lateinit var authRepository: AuthRepository

    private val viewModel: MainViewModel by activityViewModels {
        AppViewModelFactory(
            Injection.authRepository(requireActivity() as androidx.appcompat.app.AppCompatActivity),
            Injection.recyclingRepository(requireActivity() as androidx.appcompat.app.AppCompatActivity)
        )
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        authRepository = Injection.authRepository(requireActivity() as androidx.appcompat.app.AppCompatActivity)

        binding.btnLogout.setOnClickListener {
            authRepository.logout()
            val intent = Intent(requireContext(), AuthActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.refreshPoints()
        binding.tvName.text = viewModel.userName()
        binding.tvEmail.text = viewModel.userEmail()
        binding.tvPoints.text = "${viewModel.points.value ?: 0} pts"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
