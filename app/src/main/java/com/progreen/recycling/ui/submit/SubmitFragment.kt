package com.progreen.recycling.ui.submit

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AdapterView
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.progreen.recycling.data.model.RecyclingCategory
import com.progreen.recycling.data.repository.AppRepository
import com.progreen.recycling.databinding.FragmentSubmitBinding
import com.progreen.recycling.ui.common.AppViewModelFactory
import com.progreen.recycling.util.toast

class SubmitFragment : Fragment() {

    private var _binding: FragmentSubmitBinding? = null
    private val binding get() = _binding!!

    private val viewModel: SubmitViewModel by viewModels {
        AppViewModelFactory(AppRepository.getInstance(requireContext()))
    }

    private var categories: List<RecyclingCategory> = emptyList()

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
        categories = viewModel.getCategories()

        val labels = categories.map { "${it.name} (${it.pointsPerKg} pts/kg)" }
        binding.categorySpinner.adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            labels
        )
        binding.categorySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                updateEstimatedPoints()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) = Unit
        }
        binding.quantityInput.doAfterTextChanged { updateEstimatedPoints() }

        binding.submitButton.setOnClickListener {
            val weight = binding.quantityInput.text?.toString()?.toDoubleOrNull()
            if (weight == null || weight <= 0) {
                requireContext().toast("Enter valid weight")
                return@setOnClickListener
            }

            val selected = categories[binding.categorySpinner.selectedItemPosition]
            val notes = ""
            val result = viewModel.submit(selected.id, weight, notes)

            requireContext().toast("Submitted: +${result.pointsEarned} points")
            binding.quantityInput.text?.clear()
            binding.estimatedPoints.text = "Estimated Points: 0"
        }
    }

    private fun updateEstimatedPoints() {
        if (categories.isEmpty()) return
        val weight = binding.quantityInput.text?.toString()?.toDoubleOrNull() ?: 0.0
        val selected = categories[binding.categorySpinner.selectedItemPosition]
        val estimated = (weight * selected.pointsPerKg).toInt()
        binding.estimatedPoints.text = "Estimated Points: $estimated"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
