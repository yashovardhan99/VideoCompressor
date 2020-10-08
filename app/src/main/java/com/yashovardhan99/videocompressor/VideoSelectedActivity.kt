package com.yashovardhan99.videocompressor

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.yashovardhan99.videocompressor.databinding.ActivityVideoSelectedBinding
import timber.log.Timber

class VideoSelectedActivity : AppCompatActivity() {
    private val viewModel: VideoSelectedViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = DataBindingUtil.setContentView<ActivityVideoSelectedBinding>(
            this,
            R.layout.activity_video_selected
        )
        binding.viewModel = viewModel
        binding.lifecycleOwner = this
        intent?.data?.let { viewModel.setVideo(it) }
        viewModel.uri.observe(this) { uri ->
            Timber.d("Video uri: $uri")
            binding.videoView.setVideoURI(uri)
            binding.videoView.start()
        }
        viewModel.error.observe(this) {
            binding.bitrate.error = it
        }
        viewModel.done.observe(this) {
            Timber.d("Compressed Video: $it")
            if (it != null) startActivity(Intent(this, CompressedActivity::class.java).apply {
                data = viewModel.uri.value
            })
        }
    }
}