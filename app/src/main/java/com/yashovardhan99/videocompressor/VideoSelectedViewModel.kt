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

    private val _compressingResult:MutableLiveData<Resource<Uri>> = MutableLiveData()
    val compressingResult:LiveData<Resource<Uri>>
        get() = _compressingResult

    private val _uri = MutableLiveData<Uri>()
    private val context = application.applicationContext
    val uri: LiveData<Uri> = _uri
    val bitRate = MutableLiveData<String>()
    private val _compressing = MutableLiveData(false)
    val compressing: LiveData<Boolean> = _compressing


    private var file: File? = null

    fun setVideo(uri: Uri) {
        _uri.value = uri
        file?.delete()
        val stream = context.contentResolver.openInputStream(uri)
        file = File.createTempFile("Original_", ".mp4", context.filesDir)
        stream?.let { file?.writeBytes(it.readBytes()) }
    }

    fun compress() {

        val rate = bitRate.value?.trim()
        Timber.d("compress for $rate")
        if (rate.isNullOrEmpty()) {
            _compressingResult.value=Resource.Error("Please enter a bitrate")
            _compressing.value=false
            return
        }
        if (!rate.isDigitsOnly()) {
            _compressingResult.value=Resource.Error("Invalid bitrate")
            _compressing.value=false
            return
        }
        _compressingResult.value=Resource.Loading()
        _compressing.value=true
        val outputPath = context.getExternalFilesDir("CompressedVideos")
        val outputFile = File.createTempFile("compressed_${Date().time}", ".mp4", outputPath)
        viewModelScope.launch {
            val rc =
                withContext(Dispatchers.IO) { FFmpeg.execute("-y -i ${file?.path}  -b:v $rate ${outputFile.path}") }
            when (rc) {
                Config.RETURN_CODE_SUCCESS -> {
                    Timber.d("Compression successful!")
                    file?.delete()
                    _compressingResult.value=Resource.Success(outputFile.toUri())
                    _compressing.value=false
                }
                Config.RETURN_CODE_CANCEL -> {
                    Timber.d("Compression cancelled")
                    _compressingResult.value=Resource.Error("Compression was cancelled")
                    _compressing.value = false
                }
                else -> {
                    Timber.d("Failed with rc=$rc")
                    _compressingResult.value=Resource.Error("Compression Failed")
                    _compressing.value=false
                    Config.printLastCommandOutput(Log.DEBUG)
                }
            }
        }
    }

}