package com.jaqxues.akrolyb.sample.target

import android.app.Application
import timber.log.Timber

class SampleTargetApplication: Application() {
    override fun onCreate() {
        super.onCreate()
        Timber.plant(Timber.DebugTree())
    }
}