package com.progreen.recycling.ui.company

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ArrayAdapter
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.progreen.recycling.R
import com.progreen.recycling.data.repository.AppRepository
import com.progreen.recycling.databinding.FragmentCompanyDashboardBinding
import com.progreen.recycling.ui.lgu.LguManagedRewardAdapter
import com.progreen.recycling.ui.main.MainActivity
import com.progreen.recycling.util.toast
import java.io.ByteArrayOutputStream

class CompanyDashboardFragment : Fragment() {

    private var _binding: FragmentCompanyDashboardBinding? = null
    private val binding get() = _binding!!

    private lateinit var repository: AppRepository
    private lateinit var rewardAdapter: LguManagedRewardAdapter
    private lateinit var redemptionAdapter: CompanyRedemptionAdapter
    private var selectedRewardImageBase64: String? = null

    private val pickRewardImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri == null) return@registerForActivityResult
        val encoded = encodeImage(uri)
        if (encoded == null) {
            requireContext().toast("Unable to read selected image")
            return@registerForActivityResult
        }
        selectedRewardImageBase64 = encoded
        binding.rewardImagePreview.setImageBitmap(decodeBase64(encoded))
        binding.rewardImageHint.text = "Campaign image ready"
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentCompanyDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        repository = AppRepository.getInstance(requireContext())
        binding.companyTitle.text = "${repository.getUserName()} Company Dashboard"
        binding.root.startAnimation(AnimationUtils.loadAnimation(requireContext(), R.anim.slide_up_fade))

        binding.rewardTypeSpinner.adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            listOf("Voucher", "Item", "Service")
        )

        binding.rewardHasCodeSwitch.setOnCheckedChangeListener { _, isChecked ->
            binding.newRewardCodeInput.visibility = if (isChecked) View.VISIBLE else View.GONE
            if (!isChecked) binding.newRewardCodeInput.text?.clear()
        }

        rewardAdapter = LguManagedRewardAdapter(emptyList())
        binding.companyRewardsRecycler.layoutManager = LinearLayoutManager(requireContext())
        binding.companyRewardsRecycler.adapter = rewardAdapter

        redemptionAdapter = CompanyRedemptionAdapter(emptyList())
        binding.companyRedemptionsRecycler.layoutManager = LinearLayoutManager(requireContext())
        binding.companyRedemptionsRecycler.adapter = redemptionAdapter

        binding.uploadRewardImageButton.setOnClickListener {
            pickRewardImage.launch("image/*")
        }

        binding.addRewardButton.setOnClickListener {
            addReward()
        }

        binding.openRewardsButton.setOnClickListener {
            (activity as? MainActivity)?.selectTab(R.id.nav_rewards)
        }

        binding.openProfileButton.setOnClickListener {
            (activity as? MainActivity)?.selectTab(R.id.nav_profile)
        }

        refreshDashboard()
    }

    override fun onResume() {
        super.onResume()
        if (::rewardAdapter.isInitialized) refreshDashboard()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun refreshDashboard() {
        val managedRewards = repository.getManagedRewardsForCurrentUser()
        val stats = repository.getCompanyDashboardStats()
        val redemptions = repository.getCompanyRedemptionRecords()

        binding.activeCampaignsValue.text = stats.activeCampaigns.toString()
        binding.marketRewardsValue.text = stats.totalRedemptions.toString()
        binding.partnerLgusValue.text = stats.pendingClaims.toString()
        binding.materialsValue.text = repository.getCategories().size.toString()
        rewardAdapter.update(managedRewards)
        redemptionAdapter.update(redemptions)
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

        val result = repository.addManagedReward(
            title = title,
            costPoints = cost,
            rewardType = rewardType,
            description = description,
            imageBase64 = selectedRewardImageBase64,
            redeemCode = code.ifBlank { null }
        )

        if (result.isSuccess) {
            requireContext().toast("Company reward published")
            binding.newRewardTitleInput.text?.clear()
            binding.newRewardDescriptionInput.text?.clear()
            binding.newRewardCostInput.text?.clear()
            binding.newRewardCodeInput.text?.clear()
            binding.rewardHasCodeSwitch.isChecked = false
            selectedRewardImageBase64 = null
            binding.rewardImagePreview.setImageResource(R.drawable.ic_reward)
            binding.rewardImageHint.text = "Upload campaign image"
            refreshDashboard()
        } else {
            requireContext().toast(result.exceptionOrNull()?.message ?: "Failed to publish reward")
        }
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
