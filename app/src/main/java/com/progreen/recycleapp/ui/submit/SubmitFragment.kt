package com.progreen.recycleapp.ui.submit

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.progreen.recycleapp.databinding.FragmentSubmitBinding
import com.progreen.recycleapp.model.RecyclingCategory
import com.progreen.recycleapp.ui.common.AppViewModelFactory
import com.progreen.recycleapp.ui.common.Injection
import com.progreen.recycleapp.utils.toast
import com.progreen.recycleapp.viewmodel.MainViewModel

class SubmitFragment : Fragment() {

    private var _binding: FragmentSubmitBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MainViewModel by activityViewModels {
        AppViewModelFactory(
            Injection.authRepository(requireActivity() as androidx.appcompat.app.AppCompatActivity),
            Injection.recyclingRepository(requireActivity() as androidx.appcompat.app.AppCompatActivity)
        )
    }

    private lateinit var categories: List<RecyclingCategory>

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentSubmitBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        categories = viewModel.categories()
        val names = categories.map { it.name }
        binding.actvCategory.setAdapter(ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, names))

        binding.btnSubmit.setOnClickListener {
            val selected = categories.find { it.name == binding.actvCategory.text.toString() }
            val weight = binding.etWeight.text.toString().toDoubleOrNull()

            if (selected == null || weight == null) {
                requireContext().toast("Select a category and valid weight")
                return@setOnClickListener
            }

            val result = viewModel.submit(selected, weight)
            result.onSuccess { points ->
                binding.tvEarned.visibility = View.VISIBLE
                binding.tvEarned.text = "You earned $points points"
                binding.etWeight.text?.clear()
                requireContext().toast("Submission saved")
            }.onFailure {
                requireContext().toast(it.message ?: "Failed to submit")
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
