package com.progreen.recycling.ui.lgu

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.activity.result.contract.ActivityResultContracts
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
import java.io.ByteArrayOutputStream
import java.util.Locale

class LguDashboardFragment : Fragment() {

    private enum class ScanMode {
        USER_QR,
        CLAIM_TOKEN
    }

    private var _binding: FragmentLguDashboardBinding? = null
    private val binding get() = _binding!!

    private lateinit var repository: AppRepository
    private var categories: List<RecyclingCategory> = emptyList()
    private var scannedPayload: String? = null
    private var selectedRewardImageBase64: String? = null
    private var scanMode: ScanMode = ScanMode.USER_QR

    private lateinit var rewardAdapter: LguManagedRewardAdapter
    private lateinit var donationRecordAdapter: LguDonationRecordAdapter

    private val pickRewardImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri == null) return@registerForActivityResult
        val encoded = encodeImage(uri)
        if (encoded == null) {
            requireContext().toast("Unable to read selected image")
            return@registerForActivityResult
        }
        selectedRewardImageBase64 = encoded
        binding.rewardImagePreview.setImageBitmap(decodeBase64(encoded))
        binding.rewardImageHint.text = "Image selected"
    }

    private val qrScanner = registerForActivityResult(ScanContract()) { result ->
        val raw = result.contents
        if (raw.isNullOrBlank()) {
            requireContext().toast("QR scan cancelled")
            return@registerForActivityResult
        }

        when (scanMode) {
            ScanMode.USER_QR -> {
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

            ScanMode.CLAIM_TOKEN -> {
                binding.claimTokenInput.setText(raw)
                requireContext().toast("Claim token scanned")
            }
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
        binding.rewardTypeSpinner.adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            listOf("Item", "Voucher", "Service")
        )
        binding.rewardHasCodeSwitch.setOnCheckedChangeListener { _, isChecked ->
            binding.newRewardCodeInput.visibility = if (isChecked) View.VISIBLE else View.GONE
            if (!isChecked) binding.newRewardCodeInput.text?.clear()
        }

        rewardAdapter = LguManagedRewardAdapter(repository.getLguManagedRewards())
        binding.lguRewardsRecycler.layoutManager = LinearLayoutManager(requireContext())
        binding.lguRewardsRecycler.adapter = rewardAdapter

        donationRecordAdapter = LguDonationRecordAdapter(repository.getLguDonationRecords())
        binding.lguDonationRecordsRecycler.layoutManager = LinearLayoutManager(requireContext())
        binding.lguDonationRecordsRecycler.adapter = donationRecordAdapter

        binding.scanUserQrButton.setOnClickListener {
            scanMode = ScanMode.USER_QR
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
        binding.uploadRewardImageButton.setOnClickListener {
            pickRewardImage.launch("image/*")
        }
        binding.scanClaimTokenButton.setOnClickListener {
            scanMode = ScanMode.CLAIM_TOKEN
            val options = ScanOptions()
                .setDesiredBarcodeFormats(ScanOptions.QR_CODE)
                .setPrompt("Scan reward claim token")
                .setBeepEnabled(false)
                .setOrientationLocked(false)
                .setCaptureActivity(com.journeyapps.barcodescanner.CaptureActivity::class.java)
            qrScanner.launch(options)
        }
        binding.validateClaimButton.setOnClickListener {
            validateClaim()
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
        val rewardType = binding.rewardTypeSpinner.selectedItem?.toString()?.trim().orEmpty()
        val description = binding.newRewardDescriptionInput.text?.toString()?.trim().orEmpty()
        val cost = binding.newRewardCostInput.text?.toString()?.toIntOrNull()
        val code = if (binding.rewardHasCodeSwitch.isChecked) {
            binding.newRewardCodeInput.text?.toString()?.trim().orEmpty()
        } else {
            ""
        }

        if (title.isBlank() || description.isBlank() || cost == null || cost <= 0) {
            requireContext().toast("Enter title, description, and valid points")
            return
        }
        if (binding.rewardHasCodeSwitch.isChecked && code.isBlank()) {
            requireContext().toast("Enter a claim code or disable claim code")
            return
        }

        val result = repository.addLguReward(
            title = title,
            costPoints = cost,
            rewardType = rewardType,
            description = description,
            imageBase64 = selectedRewardImageBase64,
            redeemCode = code.ifBlank { null }
        )
        if (result.isSuccess) {
            requireContext().toast("Reward added")
            binding.newRewardTitleInput.text?.clear()
            binding.newRewardDescriptionInput.text?.clear()
            binding.newRewardCostInput.text?.clear()
            binding.newRewardCodeInput.text?.clear()
            binding.rewardHasCodeSwitch.isChecked = false
            selectedRewardImageBase64 = null
            binding.rewardImagePreview.setImageResource(R.drawable.ic_reward)
            binding.rewardImageHint.text = "Upload product image"
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

    private fun validateClaim() {
        val claimToken = binding.claimTokenInput.text?.toString()?.trim().orEmpty()
        if (claimToken.isBlank()) {
            requireContext().toast("Enter or scan a claim token")
            return
        }

        val result = repository.claimRewardToken(claimToken)
        if (result.isSuccess) {
            requireContext().toast(result.getOrThrow())
            binding.claimTokenInput.text?.clear()
        } else {
            requireContext().toast(result.exceptionOrNull()?.message ?: "Could not validate claim")
        }
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

    private fun encodeImage(uri: Uri): String? {
        return try {
            val source = requireContext().contentResolver.openInputStream(uri)?.use { input ->
                BitmapFactory.decodeStream(input)
            } ?: return null

            val maxSize = 640
            val scaled = if (source.width > maxSize || source.height > maxSize) {
                val ratio = minOf(maxSize.toFloat() / source.width, maxSize.toFloat() / source.height)
                Bitmap.createScaledBitmap(
                    source,
                    (source.width * ratio).toInt().coerceAtLeast(1),
                    (source.height * ratio).toInt().coerceAtLeast(1),
                    true
                )
            } else {
                source
            }

            ByteArrayOutputStream().use { stream ->
                scaled.compress(Bitmap.CompressFormat.JPEG, 80, stream)
                Base64.encodeToString(stream.toByteArray(), Base64.NO_WRAP)
            }
        } catch (_: Exception) {
            null
        }
    }

    private fun decodeBase64(base64: String): Bitmap? {
        return try {
            val bytes = Base64.decode(base64, Base64.DEFAULT)
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        } catch (_: Exception) {
            null
        }
    }
}
