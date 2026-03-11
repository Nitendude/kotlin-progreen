package com.progreen.recyclingapp.ui.main

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.progreen.recyclingapp.data.local.PrefsManager
import com.progreen.recyclingapp.data.repository.RecyclingRepository
import com.progreen.recyclingapp.databinding.FragmentSubmitBinding

class SubmitFragment : Fragment() {

    private var _binding: FragmentSubmitBinding? = null
    private val binding get() = _binding!!

    private lateinit var repository: RecyclingRepository

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

        repository = RecyclingRepository(PrefsManager(requireContext()))
        val categories = repository.getCategories()

        val spinnerAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            categories.map { it.name }
        )
        binding.categorySpinner.adapter = spinnerAdapter

        binding.quantityInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit
            override fun afterTextChanged(s: Editable?) {
                updateEstimatedPoints(categories)
            }
        })

        binding.submitButton.setOnClickListener {
            val category = categories[binding.categorySpinner.selectedItemPosition]
            val quantity = binding.quantityInput.text.toString().toDoubleOrNull()

            if (quantity == null || quantity <= 0.0) {
                Toast.makeText(requireContext(), "Enter valid quantity", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val earned = repository.submitRecyclable(category, quantity)
            Toast.makeText(requireContext(), "Great! You earned $earned points", Toast.LENGTH_SHORT).show()
            binding.quantityInput.setText("")
            updateEstimatedPoints(categories)
        }

        updateEstimatedPoints(categories)
    }

    private fun updateEstimatedPoints(categories: List<com.progreen.recyclingapp.model.Category>) {
        val quantity = binding.quantityInput.text.toString().toDoubleOrNull() ?: 0.0
        val category = categories[binding.categorySpinner.selectedItemPosition]
        val estimate = (quantity * category.pointsPerKg).toInt()
        binding.estimatedPoints.text = "Estimated Points: $estimate"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
