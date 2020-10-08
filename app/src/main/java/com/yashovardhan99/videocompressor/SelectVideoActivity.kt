package com.yashovardhan99.videocompressor

import android.content.Intent
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.yashovardhan99.videocompressor.databinding.ActivitySelectVideoBinding
import timber.log.Timber

class SelectVideoActivity : AppCompatActivity() {
    private val viewModel: SelectVideoViewModel by viewModels()
    private val videoFetcher = registerForActivityResult(ActivityResultContracts.GetContent()) {
        Timber.d("Uri : $it")
        if (it == null) return@registerForActivityResult
        startActivity(Intent(this, VideoSelectedActivity::class.java).apply {
            data = it
        })
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = DataBindingUtil.setContentView<ActivitySelectVideoBinding>(
            this,
            R.layout.activity_select_video
        )
        binding.viewModel = viewModel
        binding.lifecycleOwner = this
        viewModel.showSelectVideo.observe(this) {
            if (it) {
                getVideo()
                viewModel.doneSelection()
            }
        }
    }

    private fun getVideo() {
        Timber.d("Attempting to register activity")
        Timber.d(videoFetcher.toString())
        videoFetcher.launch("video/*")
    }
}