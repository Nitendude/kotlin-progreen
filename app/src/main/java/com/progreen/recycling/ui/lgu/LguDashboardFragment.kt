package com.progreen.recycling.ui.lgu

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import com.progreen.recycling.R
import com.progreen.recycling.data.model.RecyclingCategory
import com.progreen.recycling.data.repository.AppRepository
import com.progreen.recycling.databinding.FragmentLguDashboardBinding
import com.progreen.recycling.util.toast
import java.util.Locale

class LguDashboardFragment : Fragment() {

    private var _binding: FragmentLguDashboardBinding? = null
    private val binding get() = _binding!!

    private lateinit var repository: AppRepository
    private var categories: List<RecyclingCategory> = emptyList()
    private var scannedPayload: String? = null

    private lateinit var rewardAdapter: LguManagedRewardAdapter
    private lateinit var donationRecordAdapter: LguDonationRecordAdapter

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
        binding.root.startAnimation(AnimationUtils.loadAnimation(requireContext(), R.anim.slide_up_fade))

        categories = repository.getCategories()
        binding.donationCategorySpinner.adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            categories.map { "${it.name} (${it.pointsPerKg} pts/kg)" }
        )
        binding.donationCategorySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                updateEstimate()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) = Unit
        }
        binding.donationKgInput.doAfterTextChanged { updateEstimate() }

        rewardAdapter = LguManagedRewardAdapter(repository.getLguManagedRewards())
        binding.lguRewardsRecycler.layoutManager = LinearLayoutManager(requireContext())
        binding.lguRewardsRecycler.adapter = rewardAdapter

        donationRecordAdapter = LguDonationRecordAdapter(repository.getLguDonationRecords())
        binding.lguDonationRecordsRecycler.layoutManager = LinearLayoutManager(requireContext())
        binding.lguDonationRecordsRecycler.adapter = donationRecordAdapter

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

        refreshDashboard()
        updateEstimate()
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
            refreshDashboard()
            updateEstimate()
        } else {
            requireContext().toast(result.exceptionOrNull()?.message ?: "Failed to credit donation")
        }
    }

    private fun addReward() {
        val title = binding.newRewardTitleInput.text?.toString()?.trim().orEmpty()
        val description = binding.newRewardDescriptionInput.text?.toString()?.trim().orEmpty()
        val cost = binding.newRewardCostInput.text?.toString()?.toIntOrNull()
        val imageUrl = binding.newRewardImageUrlInput.text?.toString()?.trim().orEmpty()
        val code = binding.newRewardCodeInput.text?.toString()?.trim().orEmpty()

        if (title.isBlank() || description.isBlank() || cost == null || cost <= 0) {
            requireContext().toast("Enter title, description, and valid points")
            return
        }

        val result = repository.addLguReward(
            title = title,
            costPoints = cost,
            description = description,
            imageUrl = imageUrl.ifBlank { null },
            redeemCode = code.ifBlank { null }
        )
        if (result.isSuccess) {
            requireContext().toast("Reward added")
            binding.newRewardTitleInput.text?.clear()
            binding.newRewardDescriptionInput.text?.clear()
            binding.newRewardCostInput.text?.clear()
            binding.newRewardImageUrlInput.text?.clear()
            binding.newRewardCodeInput.text?.clear()
            refreshDashboard()
        } else {
            requireContext().toast(result.exceptionOrNull()?.message ?: "Failed to add reward")
        }
    }

    private fun refreshDashboard() {
        val stats = repository.getLguDashboardStats()
        binding.totalKgValue.text = String.format(Locale.getDefault(), "%.2f", stats.donatedKgTotal)
        binding.totalPointsValue.text = stats.pointsCreditedTotal.toString()
        binding.totalDonationsValue.text = stats.donationsCount.toString()
        binding.totalRewardsValue.text = stats.rewardsCount.toString()

        rewardAdapter.update(repository.getLguManagedRewards())
        donationRecordAdapter.update(repository.getLguDonationRecords())
    }

    private fun updateEstimate() {
        val category = categories.getOrNull(binding.donationCategorySpinner.selectedItemPosition)
        val kg = binding.donationKgInput.text?.toString()?.toDoubleOrNull() ?: 0.0
        val estimate = if (category == null || kg <= 0.0) 0 else (kg * category.pointsPerKg).toInt()
        binding.donationEstimate.text = "Estimated credit: $estimate pts"
    }

    override fun onResume() {
        super.onResume()
        if (::rewardAdapter.isInitialized && ::donationRecordAdapter.isInitialized) {
            refreshDashboard()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
