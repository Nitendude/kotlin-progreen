package com.progreen.recycling.ui.categories

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.progreen.recycling.data.repository.AppRepository
import com.progreen.recycling.databinding.FragmentCategoriesBinding
import com.progreen.recycling.ui.common.AppViewModelFactory

class CategoriesFragment : Fragment() {

    private var _binding: FragmentCategoriesBinding? = null
    private val binding get() = _binding!!

    private val viewModel: CategoriesViewModel by viewModels {
        AppViewModelFactory(AppRepository.getInstance(requireContext()))
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCategoriesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.categoriesRecycler.layoutManager = LinearLayoutManager(requireContext())
        binding.categoriesRecycler.adapter = CategoryAdapter(viewModel.getCategories())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
