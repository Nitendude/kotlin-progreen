package com.progreen.recyclingapp.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.progreen.recyclingapp.R
import com.progreen.recyclingapp.data.local.PrefsManager
import com.progreen.recyclingapp.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.cardCategories.setOnClickListener {
            findNavController().navigate(R.id.categoriesFragment)
        }
        binding.cardSubmit.setOnClickListener {
            findNavController().navigate(R.id.submitFragment)
        }
        binding.cardRewards.setOnClickListener {
            findNavController().navigate(R.id.rewardsFragment)
        }
        binding.cardHistory.setOnClickListener {
            findNavController().navigate(R.id.historyFragment)
        }

        animateCards()
    }

    override fun onResume() {
        super.onResume()
        val points = PrefsManager(requireContext()).getPoints()
        binding.pointsValue.text = points.toString()
    }

    private fun animateCards() {
        val cards = listOf(binding.pointsCard, binding.cardCategories, binding.cardSubmit, binding.cardRewards, binding.cardHistory)
        cards.forEachIndexed { index, card ->
            card.alpha = 0f
            card.translationY = 40f
            card.animate()
                .alpha(1f)
                .translationY(0f)
                .setStartDelay((index * 70).toLong())
                .setDuration(360)
                .start()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
