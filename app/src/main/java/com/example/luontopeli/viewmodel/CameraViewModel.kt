package com.example.luontopeli.viewmodel

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.luontopeli.data.local.entity.NatureSpot
import com.example.luontopeli.data.repository.NatureSpotRepository
import com.example.luontopeli.ml.ClassificationResult
import com.example.luontopeli.ml.PlantClassifier
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@HiltViewModel
class CameraViewModel @Inject constructor(
    application: Application,
    private val repository: NatureSpotRepository
) : AndroidViewModel(application) {

    private val _capturedImagePath = MutableStateFlow<String?>(null)
    val capturedImagePath: StateFlow<String?> = _capturedImagePath.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _comment = MutableStateFlow("")
    val comment: StateFlow<String> = _comment.asStateFlow()

    // Nykyinen sijainti (asetetaan MapViewModelista)
    var currentLatitude: Double = 0.0
    var currentLongitude: Double = 0.0

    private val classifier = PlantClassifier()

    // Tunnistustulos
    private val _classificationResult = MutableStateFlow<ClassificationResult?>(null)
    val classificationResult: StateFlow<ClassificationResult?> = _classificationResult.asStateFlow()

    fun setComment(text: String) { _comment.value = text }
    fun clearComment() { _comment.value = "" }

    

    fun takePhotoAndClassify(context: Context, imageCapture: ImageCapture) {
        _isLoading.value = true
        viewModelScope.launch {
            // 1. Ota kuva
            val imagePath = takePhotoSuspend(context, imageCapture)
            if (imagePath == null) { _isLoading.value = false; return@launch }

            _capturedImagePath.value = imagePath

            // 2. Tunnista kasvi kuvasta
            try {
                val uri = Uri.fromFile(File(imagePath))
                val result = classifier.classify(uri, context)
                _classificationResult.value = result
            } catch (e: Exception) {
                _classificationResult.value = ClassificationResult.Error(e.message ?: "Tuntematon virhe")
            }

            _isLoading.value = false
        }
    }

    fun clearCapturedImage() {
        _capturedImagePath.value = null
    }

    fun saveCurrentSpot() {
        val imagePath = _capturedImagePath.value ?: return
        viewModelScope.launch {
            val result = _classificationResult.value

            val spot = NatureSpot(
                name = when (result) {
                    is ClassificationResult.Success -> result.label
                    else -> "Luontolöytö"
                },
                latitude = currentLatitude,
                longitude = currentLongitude,
                imageLocalPath = imagePath,
                plantLabel = (result as? ClassificationResult.Success)?.label,
                confidence = (result as? ClassificationResult.Success)?.confidence
                ,
                comment = _comment.value
            )
            repository.insertSpot(spot)
            clearCapturedImage()
            _classificationResult.value = null
            clearComment()
        }
    }

    override fun onCleared() {
        super.onCleared()
        classifier.close()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private suspend fun takePhotoSuspend(context: Context, imageCapture: ImageCapture): String? =
        kotlinx.coroutines.suspendCancellableCoroutine { continuation ->

            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val outputDir = File(context.filesDir, "nature_photos").also { it.mkdirs() }
            val outputFile = File(outputDir, "IMG_${timestamp}.jpg")
            val outputOptions = ImageCapture.OutputFileOptions.Builder(outputFile).build()

            imageCapture.takePicture(
                outputOptions,
                ContextCompat.getMainExecutor(context),
                object : ImageCapture.OnImageSavedCallback {
                    override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                        // Jatka coroutinea palauttamalla polku
                        if (continuation.isActive) {
                            continuation.resume(outputFile.absolutePath, onCancellation = null)
                        }
                    }

                    override fun onError(exception: ImageCaptureException) {
                        // Jatka coroutinea palauttamalla null virheen sattuessa
                        if (continuation.isActive) {
                            continuation.resume(null, onCancellation = null)
                        }
                    }
                }
            )
        }
}