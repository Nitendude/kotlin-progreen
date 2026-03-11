package com.progreen.recycling.ui.scan

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import com.progreen.recycling.data.repository.AppRepository
import com.progreen.recycling.databinding.FragmentPlasticScanBinding

class PlasticScanFragment : Fragment() {

    private var _binding: FragmentPlasticScanBinding? = null
    private val binding get() = _binding!!

    private lateinit var repository: AppRepository

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPlasticScanBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        repository = AppRepository.getInstance(requireContext())

        val types = repository.getAcceptedPlasticTypes()
        binding.scannerTypeSpinner.adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            types
        )

        binding.scanButton.setOnClickListener {
            val selected = types[binding.scannerTypeSpinner.selectedItemPosition]
            val category = repository.getCategories().first { it.name == selected }
            binding.scanResultTitle.text = "Accepted: ${category.name}"
            binding.scanResultText.text = "This type is accepted. You can earn ${category.pointsPerKg} pts/kg when donating at partner LGUs."
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
