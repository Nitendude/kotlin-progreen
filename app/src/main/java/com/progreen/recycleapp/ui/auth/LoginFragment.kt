package com.progreen.recycleapp.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.progreen.recycleapp.R
import com.progreen.recycleapp.databinding.FragmentLoginBinding
import com.progreen.recycleapp.ui.common.AppViewModelFactory
import com.progreen.recycleapp.ui.common.Injection
import com.progreen.recycleapp.ui.main.MainActivity
import com.progreen.recycleapp.utils.toast
import com.progreen.recycleapp.viewmodel.AuthViewModel

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AuthViewModel by viewModels {
        AppViewModelFactory(
            Injection.authRepository(this),
            Injection.recyclingRepository(this)
        )
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.logoHolder.startAnimation(AnimationUtils.loadAnimation(requireContext(), R.anim.splash_logo_in))
        binding.formContainer.startAnimation(AnimationUtils.loadAnimation(requireContext(), R.anim.fade_slide_up))

        binding.btnLogin.setOnClickListener {
            viewModel.login(
                binding.etEmail.text.toString(),
                binding.etPassword.text.toString()
            )
        }

        binding.tvRegister.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .setCustomAnimations(
                    R.anim.slide_in_right,
                    R.anim.slide_out_left,
                    R.anim.slide_in_left,
                    R.anim.slide_out_right
                )
                .replace(R.id.authContainer, RegisterFragment())
                .addToBackStack(null)
                .commit()
        }

        viewModel.authResult.observe(viewLifecycleOwner) { result ->
            result.onSuccess {
                val intent = Intent(requireContext(), MainActivity::class.java)
                startActivity(intent)
                requireActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                requireActivity().finish()
            }.onFailure {
                requireContext().toast(it.message ?: "Login failed")
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
