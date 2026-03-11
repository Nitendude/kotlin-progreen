package com.progreen.recycleapp.ui.categories

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.progreen.recycleapp.databinding.FragmentCategoriesBinding
import com.progreen.recycleapp.ui.adapters.CategoryAdapter
import com.progreen.recycleapp.ui.common.AppViewModelFactory
import com.progreen.recycleapp.ui.common.Injection
import com.progreen.recycleapp.viewmodel.MainViewModel

class CategoriesFragment : Fragment() {

    private var _binding: FragmentCategoriesBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MainViewModel by activityViewModels {
        AppViewModelFactory(
            Injection.authRepository(requireActivity() as androidx.appcompat.app.AppCompatActivity),
            Injection.recyclingRepository(requireActivity() as androidx.appcompat.app.AppCompatActivity)
        )
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentCategoriesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.rvCategories.layoutManager = LinearLayoutManager(requireContext())
        binding.rvCategories.adapter = CategoryAdapter(viewModel.categories())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
