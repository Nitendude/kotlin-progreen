package com.progreen.recycling.ui.scan

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.progreen.recycling.data.model.PlasticDetectionResult
import com.progreen.recycling.data.repository.AppRepository
import com.progreen.recycling.databinding.FragmentPlasticScanBinding
import com.progreen.recycling.util.toast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.util.concurrent.TimeUnit

class PlasticScanFragment : Fragment() {

    private var _binding: FragmentPlasticScanBinding? = null
    private val binding get() = _binding!!

    private lateinit var repository: AppRepository
    private var cameraProvider: ProcessCameraProvider? = null

    private val mainHandler = Handler(Looper.getMainLooper())
    private var isLiveScanning = false
    private var isAnalyzing = false

    private val permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) {
            startCameraPreview()
        } else {
            requireContext().toast("Camera permission is required for live scanning")
        }
    }

    private val scanRunnable = object : Runnable {
        override fun run() {
            if (!isLiveScanning || isAnalyzing) {
                scheduleNextScan()
                return
            }

            val frame = binding.cameraPreviewView.bitmap
            if (frame == null) {
                scheduleNextScan()
                return
            }

            analyzeFrame(frame)
        }
    }

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
        binding.acceptedTypesLabel.text = "Accepted: ${repository.getAcceptedPlasticTypes().joinToString()}"

        if (hasCameraPermission()) {
            startCameraPreview()
        } else {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }

        binding.toggleLiveScanButton.setOnClickListener {
            if (isLiveScanning) stopLiveScanning() else startLiveScanning()
        }
    }

    private fun hasCameraPermission(): Boolean {
        return ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
    }

    private fun startCameraPreview() {
        val future = ProcessCameraProvider.getInstance(requireContext())
        future.addListener(
            {
                cameraProvider = future.get()
                val provider = cameraProvider ?: return@addListener

                val preview = Preview.Builder().build().also {
                    it.surfaceProvider = binding.cameraPreviewView.surfaceProvider
                }

                provider.unbindAll()
                provider.bindToLifecycle(
                    viewLifecycleOwner,
                    CameraSelector.DEFAULT_BACK_CAMERA,
                    preview
                )
            },
            ContextCompat.getMainExecutor(requireContext())
        )
    }

    private fun startLiveScanning() {
        if (!hasCameraPermission()) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
            return
        }
        isLiveScanning = true
        binding.toggleLiveScanButton.text = "Stop Live Scan"
        binding.scanResultTitle.text = "Live scanner is running"
        binding.scanResultText.text = "Detecting plastic type every 5 seconds..."
        mainHandler.removeCallbacks(scanRunnable)
        mainHandler.post(scanRunnable)
    }

    private fun stopLiveScanning() {
        isLiveScanning = false
        binding.toggleLiveScanButton.text = "Start Live Scan"
        mainHandler.removeCallbacks(scanRunnable)
    }

    private fun scheduleNextScan() {
        if (isLiveScanning) {
            mainHandler.postDelayed(scanRunnable, TimeUnit.SECONDS.toMillis(5))
        }
    }

    private fun analyzeFrame(frame: Bitmap) {
        isAnalyzing = true
        setLoading(true)

        lifecycleScope.launch {
            val result = withContext(Dispatchers.IO) {
                val compressed = resizeAndCompress(frame)
                val dataUrl = "data:image/jpeg;base64,${Base64.encodeToString(compressed, Base64.NO_WRAP)}"
                repository.detectPlasticWithGroq(dataUrl)
            }

            setLoading(false)
            isAnalyzing = false

            if (result.isSuccess) {
                renderResult(result.getOrThrow())
            } else {
                val message = result.exceptionOrNull()?.message ?: "Live detection failed"
                binding.scanResultTitle.text = "Error"
                binding.scanResultText.text = message
            }
            scheduleNextScan()
        }
    }

    private fun renderResult(result: PlasticDetectionResult) {
        val verdict = if (result.donatable) "Donatable" else "Not Donatable"
        binding.scanResultTitle.text = "Detected: ${result.plasticType} ($verdict)"
        binding.scanResultText.text = "Confidence: ${result.confidence}%\nReason: ${result.reason}"
    }

    private fun setLoading(isLoading: Boolean) {
        binding.analyzingProgress.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.toggleLiveScanButton.isEnabled = !isLoading
    }

    private fun resizeAndCompress(bitmap: Bitmap): ByteArray {
        val maxSide = 1024
        val ratio = bitmap.width.toFloat() / bitmap.height.toFloat()
        val targetWidth: Int
        val targetHeight: Int

        if (bitmap.width >= bitmap.height) {
            targetWidth = maxSide
            targetHeight = (maxSide / ratio).toInt().coerceAtLeast(1)
        } else {
            targetHeight = maxSide
            targetWidth = (maxSide * ratio).toInt().coerceAtLeast(1)
        }

        val scaled = Bitmap.createScaledBitmap(bitmap, targetWidth, targetHeight, true)
        val output = ByteArrayOutputStream()
        scaled.compress(Bitmap.CompressFormat.JPEG, 85, output)
        scaled.recycle()

        var data = output.toByteArray()
        if (data.size > 2_000_000) {
            val fallback = BitmapFactory.decodeByteArray(data, 0, data.size)
            val retryStream = ByteArrayOutputStream()
            fallback.compress(Bitmap.CompressFormat.JPEG, 70, retryStream)
            fallback.recycle()
            data = retryStream.toByteArray()
        }
        return data
    }

    override fun onDestroyView() {
        super.onDestroyView()
        stopLiveScanning()
        cameraProvider?.unbindAll()
        _binding = null
    }
}
