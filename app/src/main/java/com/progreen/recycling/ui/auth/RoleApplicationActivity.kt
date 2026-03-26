package com.progreen.recycling.ui.auth

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.util.Base64
import android.view.animation.AnimationUtils
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.progreen.recycling.R
import com.progreen.recycling.data.repository.AppRepository
import com.progreen.recycling.databinding.ActivityRoleApplicationBinding
import com.progreen.recycling.util.toast
import java.io.IOException

class RoleApplicationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRoleApplicationBinding
    private val repository by lazy { AppRepository.getInstance(this) }

    private var selectedDocumentName: String? = null
    private var selectedDocumentBase64: String? = null

    private val documentPicker = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            handleDocumentSelection(uri)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRoleApplicationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val applicationType = intent.getStringExtra(EXTRA_APPLICATION_TYPE)?.uppercase().orEmpty()
        if (applicationType !in SUPPORTED_TYPES) {
            finish()
            return
        }

        binding.applicationTitle.text = if (applicationType == "LGU") "Apply as LGU" else "Apply as Company"
        binding.applicationSubtitle.text = if (applicationType == "LGU") {
            "Submit your local government office details and proof for admin review."
        } else {
            "Submit your company details and business proof for admin review."
        }
        binding.organizationNameInput.hint =
            if (applicationType == "LGU") "LGU / Office Name" else "Company Name"
        binding.contactEmailInput.hint =
            if (applicationType == "LGU") "Government Email" else "Business Email"
        binding.uploadDocumentButton.text =
            if (applicationType == "LGU") "Upload Permit or Office Proof" else "Upload SEC / DTI / Permit"
        binding.applicationCard.startAnimation(AnimationUtils.loadAnimation(this, R.anim.slide_up_fade))

        binding.uploadDocumentButton.setOnClickListener {
            documentPicker.launch("*/*")
        }

        binding.submitApplicationButton.setOnClickListener {
            submitApplication(applicationType)
        }

        binding.backToLoginCta.setOnClickListener {
            finish()
            overridePendingTransition(R.anim.activity_pop_enter, R.anim.activity_pop_exit)
        }
    }

    private fun handleDocumentSelection(uri: Uri) {
        try {
            val bytes = contentResolver.openInputStream(uri)?.use { it.readBytes() }
                ?: throw IOException("Could not read selected file")
            selectedDocumentName = resolveDisplayName(uri)
            selectedDocumentBase64 = Base64.encodeToString(bytes, Base64.NO_WRAP)
            binding.documentStatus.text = selectedDocumentName ?: "Document selected"
        } catch (e: Exception) {
            selectedDocumentName = null
            selectedDocumentBase64 = null
            toast(e.message ?: "Could not read selected document")
        }
    }

    private fun resolveDisplayName(uri: Uri): String {
        contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (nameIndex >= 0 && cursor.moveToFirst()) {
                return cursor.getString(nameIndex)
            }
        }
        return uri.lastPathSegment ?: "uploaded_document"
    }

    private fun submitApplication(applicationType: String) {
        val name = binding.fullNameInput.text?.toString()?.trim().orEmpty()
        val email = binding.emailInput.text?.toString()?.trim().orEmpty()
        val password = binding.passwordInput.text?.toString()?.trim().orEmpty()
        val organizationName = binding.organizationNameInput.text?.toString()?.trim().orEmpty()
        val officeAddress = binding.officeAddressInput.text?.toString()?.trim().orEmpty()
        val contactPerson = binding.contactPersonInput.text?.toString()?.trim().orEmpty()
        val contactEmail = binding.contactEmailInput.text?.toString()?.trim().orEmpty()

        if (
            name.isBlank() ||
            email.isBlank() ||
            password.isBlank() ||
            organizationName.isBlank() ||
            officeAddress.isBlank() ||
            contactPerson.isBlank() ||
            contactEmail.isBlank()
        ) {
            toast("Complete all required fields")
            return
        }

        if (selectedDocumentBase64.isNullOrBlank()) {
            toast("Upload a permit or proof document")
            return
        }

        val success = repository.submitRoleApplication(
            name = name,
            email = email,
            password = password,
            applicationType = applicationType,
            organizationName = organizationName,
            officeAddress = officeAddress,
            contactPerson = contactPerson,
            contactEmail = contactEmail,
            documentName = selectedDocumentName,
            documentBase64 = selectedDocumentBase64
        )

        if (success) {
            toast("Application submitted. Verify your email next.")
            startActivity(
                Intent(this, VerifyEmailActivity::class.java)
                    .putExtra(VerifyEmailActivity.EXTRA_EMAIL, email)
                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            )
            setResult(Activity.RESULT_OK)
            finish()
            overridePendingTransition(R.anim.activity_enter, R.anim.activity_exit)
        } else {
            toast(repository.getLastErrorMessage() ?: "Could not submit application")
        }
    }

    companion object {
        const val EXTRA_APPLICATION_TYPE = "application_type"
        private val SUPPORTED_TYPES = setOf("LGU", "COMPANY")
    }
}
