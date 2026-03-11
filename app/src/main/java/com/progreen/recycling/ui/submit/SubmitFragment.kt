package com.progreen.recycling.ui.submit

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.progreen.recycling.data.repository.AppRepository
import com.progreen.recycling.databinding.FragmentSubmitBinding
import com.progreen.recycling.ui.common.AppViewModelFactory
import com.progreen.recycling.util.QrCodeGenerator

class SubmitFragment : Fragment() {

    private var _binding: FragmentSubmitBinding? = null
    private val binding get() = _binding!!

    private val viewModel: SubmitViewModel by viewModels {
        AppViewModelFactory(AppRepository.getInstance(requireContext()))
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSubmitBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.regenerateSubmitQrButton.setOnClickListener {
            renderQr()
        }

        renderQr()
    }

    private fun renderQr() {
        val payload = viewModel.getProfileQrPayload()
        binding.submitQrPayloadPreview.text = payload
        binding.submitQrImage.setImageBitmap(QrCodeGenerator.generate(payload))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
