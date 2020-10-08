package com.yashovardhan99.videocompressor

import android.app.Application
import timber.log.Timber

class CompressorApp : Application() {
    override fun onCreate() {
        super.onCreate()
        Timber.plant(Timber.DebugTree())
    }
}