package com.progreen.recycling.ui.lgu

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.progreen.recycling.data.repository.AppRepository
import com.progreen.recycling.databinding.FragmentLguMapBinding

class LguMapFragment : Fragment() {

    private var _binding: FragmentLguMapBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLguMapBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val sites = AppRepository.getInstance(requireContext()).getNearestLguSites()

        binding.lguRecycler.layoutManager = LinearLayoutManager(requireContext())
        binding.lguRecycler.adapter = LguSiteAdapter(sites)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
