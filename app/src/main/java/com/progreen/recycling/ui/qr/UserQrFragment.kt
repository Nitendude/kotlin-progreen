package com.progreen.recycling.ui.qr

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.progreen.recycling.data.repository.AppRepository
import com.progreen.recycling.databinding.FragmentUserQrBinding
import com.progreen.recycling.util.QrCodeGenerator

class UserQrFragment : Fragment() {

    private var _binding: FragmentUserQrBinding? = null
    private val binding get() = _binding!!

    private lateinit var repository: AppRepository

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUserQrBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        repository = AppRepository.getInstance(requireContext())

        binding.regenerateQrButton.setOnClickListener {
            renderQr()
        }

        renderQr()
    }

    private fun renderQr() {
        val payload = repository.getProfileQrPayload()
        binding.qrPayloadPreview.text = payload
        binding.qrImage.setImageBitmap(QrCodeGenerator.generate(payload))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
