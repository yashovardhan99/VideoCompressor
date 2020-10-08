package com.yashovardhan99.videocompressor

import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.core.net.toUri
import androidx.core.text.isDigitsOnly
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.arthenica.mobileffmpeg.Config
import com.arthenica.mobileffmpeg.FFmpeg
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File
import java.util.*

class VideoSelectedViewModel(application: Application) : AndroidViewModel(application) {
    private val _uri = MutableLiveData<Uri>()
    private val context = application.applicationContext
    val uri: LiveData<Uri> = _uri
    val bitRate = MutableLiveData<String>()
    private val _error = MutableLiveData("")
    private val _compressing = MutableLiveData(false)
    val error: LiveData<String> = _error
    val compressing: LiveData<Boolean> = _compressing
    private val _done = MutableLiveData<Uri>()
    val done: LiveData<Uri> = _done
    private var file: File? = null
    fun setVideo(uri: Uri) {
        _uri.value = uri
        file?.delete()
        val stream = context.contentResolver.openInputStream(uri)
        file = File.createTempFile("Original_", ".mp4", context.filesDir)
        stream?.let { file?.writeBytes(it.readBytes()) }
    }

    fun compress() {
        _error.value = ""
        val rate = bitRate.value?.trim()
        Timber.d("compress for $rate")
        if (rate.isNullOrEmpty()) {
            _error.value = "Please enter a bitrate"
            return
        }
        if (!rate.isDigitsOnly()) {
            _error.value = "Invalid bitrate"
            return
        }
        _compressing.value = true
        val outputPath = context.getExternalFilesDir("CompressedVideos")
        val outputFile = File.createTempFile("compressed_${Date().time}", ".mp4", outputPath)
        viewModelScope.launch {
            val rc =
                withContext(Dispatchers.IO) { FFmpeg.execute("-y -i ${file?.path}  -b:v $rate ${outputFile.path}") }
            when (rc) {
                Config.RETURN_CODE_SUCCESS -> {
                    Timber.d("Compression successful!")
                    file?.delete()
                    _compressing.value = false
                    _done.value = outputFile.toUri()
                }
                Config.RETURN_CODE_CANCEL -> {
                    Timber.d("Compression cancelled")
                    _error.value = "Compression was cancelled!"
                    _compressing.value = false
                }
                else -> {
                    Timber.d("Failed with rc=$rc")
                    _error.value = "Compression Failed"
                    Config.printLastCommandOutput(Log.DEBUG)
                    _compressing.value = false
                }
            }
        }
    }

}