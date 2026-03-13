package com.progreen.recycling.ui.lgu

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import com.progreen.recycling.data.model.RecyclingCategory
import com.progreen.recycling.data.repository.AppRepository
import com.progreen.recycling.databinding.FragmentLguDashboardBinding
import com.progreen.recycling.util.toast

class LguDashboardFragment : Fragment() {

    private var _binding: FragmentLguDashboardBinding? = null
    private val binding get() = _binding!!

    private lateinit var repository: AppRepository
    private var categories: List<RecyclingCategory> = emptyList()
    private var scannedPayload: String? = null

    private lateinit var rewardAdapter: LguManagedRewardAdapter

    private val qrScanner = registerForActivityResult(ScanContract()) { result ->
        val raw = result.contents
        if (raw.isNullOrBlank()) {
            requireContext().toast("QR scan cancelled")
            return@registerForActivityResult
        }

        scannedPayload = raw
        val user = repository.resolveUserFromQr(raw)
        if (user.isSuccess) {
            val (name, email) = user.getOrThrow()
            binding.scannedUserLabel.text = "Scanned user: $name ($email)"
        } else {
            binding.scannedUserLabel.text = "Invalid user QR"
            requireContext().toast(user.exceptionOrNull()?.message ?: "Invalid user QR")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLguDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        repository = AppRepository.getInstance(requireContext())
        binding.lguTitle.text = "${repository.getUserName()} LGU Dashboard"

        categories = repository.getCategories()
        binding.donationCategorySpinner.adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            categories.map { "${it.name} (${it.pointsPerKg} pts/kg)" }
        )

        rewardAdapter = LguManagedRewardAdapter(repository.getLguManagedRewards())
        binding.lguRewardsRecycler.layoutManager = LinearLayoutManager(requireContext())
        binding.lguRewardsRecycler.adapter = rewardAdapter

        binding.scanUserQrButton.setOnClickListener {
            val options = ScanOptions()
                .setDesiredBarcodeFormats(ScanOptions.QR_CODE)
                .setPrompt("Scan user donation QR")
                .setBeepEnabled(false)
                .setOrientationLocked(false)
                .setCaptureActivity(com.journeyapps.barcodescanner.CaptureActivity::class.java)
            qrScanner.launch(options)
        }

        binding.confirmDonationButton.setOnClickListener {
            confirmDonation()
        }

        binding.addRewardButton.setOnClickListener {
            addReward()
        }
    }

    private fun confirmDonation() {
        val payload = scannedPayload
        if (payload.isNullOrBlank()) {
            requireContext().toast("Scan user QR first")
            return
        }

        val kg = binding.donationKgInput.text?.toString()?.toDoubleOrNull()
        if (kg == null || kg <= 0) {
            requireContext().toast("Enter a valid weight")
            return
        }

        val category = categories[binding.donationCategorySpinner.selectedItemPosition]
        val result = repository.creditUserDonationFromQr(payload, category.id, kg)

        if (result.isSuccess) {
            val outcome = result.getOrThrow()
            requireContext().toast("Credited +${outcome.pointsEarned} pts to ${outcome.userName}")
            binding.donationKgInput.text?.clear()
            binding.scannedUserLabel.text = "Scanned user: ${outcome.userName} (${outcome.userEmail}) | Balance: ${outcome.newBalance}"
        } else {
            requireContext().toast(result.exceptionOrNull()?.message ?: "Failed to credit donation")
        }
    }

    private fun addReward() {
        val title = binding.newRewardTitleInput.text?.toString()?.trim().orEmpty()
        val cost = binding.newRewardCostInput.text?.toString()?.toIntOrNull()

        if (title.isBlank() || cost == null || cost <= 0) {
            requireContext().toast("Enter valid reward title and points")
            return
        }

        val result = repository.addLguReward(title, cost)
        if (result.isSuccess) {
            requireContext().toast("Reward added")
            binding.newRewardTitleInput.text?.clear()
            binding.newRewardCostInput.text?.clear()
            rewardAdapter.update(repository.getLguManagedRewards())
        } else {
            requireContext().toast(result.exceptionOrNull()?.message ?: "Failed to add reward")
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
