package com.progreen.recycleapp.ui.history

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.progreen.recycleapp.databinding.FragmentHistoryBinding
import com.progreen.recycleapp.ui.adapters.HistoryAdapter
import com.progreen.recycleapp.ui.common.AppViewModelFactory
import com.progreen.recycleapp.ui.common.Injection
import com.progreen.recycleapp.viewmodel.MainViewModel

class HistoryFragment : Fragment() {

    private var _binding: FragmentHistoryBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MainViewModel by activityViewModels {
        AppViewModelFactory(
            Injection.authRepository(requireActivity() as androidx.appcompat.app.AppCompatActivity),
            Injection.recyclingRepository(requireActivity() as androidx.appcompat.app.AppCompatActivity)
        )
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        binding.rvHistory.layoutManager = LinearLayoutManager(requireContext())
        binding.rvHistory.adapter = HistoryAdapter(viewModel.history())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
