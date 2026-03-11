package com.progreen.recyclingapp.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.progreen.recyclingapp.data.local.PrefsManager
import com.progreen.recyclingapp.data.repository.RecyclingRepository
import com.progreen.recyclingapp.databinding.FragmentCategoriesBinding
import com.progreen.recyclingapp.ui.adapters.CategoryAdapter

class CategoriesFragment : Fragment() {

    private var _binding: FragmentCategoriesBinding? = null
    private val binding get() = _binding!!

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

        val repository = RecyclingRepository(PrefsManager(requireContext()))
        val adapter = CategoryAdapter()
        adapter.submitList(repository.getCategories())

        binding.categoriesRecycler.layoutManager = LinearLayoutManager(requireContext())
        binding.categoriesRecycler.adapter = adapter
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
