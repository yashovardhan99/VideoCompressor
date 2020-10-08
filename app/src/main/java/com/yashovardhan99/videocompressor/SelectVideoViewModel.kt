package com.yashovardhan99.videocompressor

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SelectVideoViewModel() : ViewModel() {
    private val _showSelectVideo = MutableLiveData<Boolean>()
    val showSelectVideo: LiveData<Boolean> = _showSelectVideo
    fun selectVideo() {
        _showSelectVideo.postValue(true)
    }

    fun doneSelection() {
        _showSelectVideo.value = false
    }
}