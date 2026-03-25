package com.progreen.recycling.ui.redemptions

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.progreen.recycling.data.repository.AppRepository
import com.progreen.recycling.databinding.FragmentRedemptionHistoryBinding

class RedemptionHistoryFragment : Fragment() {

    private var _binding: FragmentRedemptionHistoryBinding? = null
    private val binding get() = _binding!!

    private lateinit var repository: AppRepository

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentRedemptionHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        repository = AppRepository.getInstance(requireContext())
        refresh()
    }

    override fun onResume() {
        super.onResume()
        refresh()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun refresh() {
        val items = repository.getRedemptionHistory()
        binding.redemptionRecycler.layoutManager = LinearLayoutManager(requireContext())
        binding.redemptionRecycler.adapter = RedemptionHistoryAdapter(items)
        binding.emptyRedemptionView.visibility = if (items.isEmpty()) View.VISIBLE else View.GONE
    }
}
