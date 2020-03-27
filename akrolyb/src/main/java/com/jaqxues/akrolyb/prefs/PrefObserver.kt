package com.jaqxues.akrolyb.prefs

import android.os.Build
import android.os.FileObserver
import androidx.annotation.RequiresApi
import timber.log.Timber
import java.io.File
import java.util.concurrent.atomic.AtomicBoolean


/**
 * This file was created by Jacques Hoffmann (jaqxues) in the Project Instaprefs.<br>
 * Date: 15.03.20 - Time 20:52.
 */
class PrefObserver : FileObserver {

    @RequiresApi(29)
    private constructor(file: File) : super(file)
    @Suppress("DEPRECATION")
    private constructor(path: String) : super(path)

    private val isLocalChange = AtomicBoolean()

    override fun onEvent(event: Int, path: String?) {
        if (isLocalChange.get()) {
            Timber.d("Local even occurred --> Skipping reload")

            if (event == CLOSE_WRITE || event == CLOSE_NOWRITE) {
                Timber.d("Finished local events")
                isLocalChange.set(false)
            }
            return
        }

        when (event) {
            CLOSE_WRITE -> {
                Timber.d("Preference closed with potential changes, reloading PreferenceMap")
                PrefManager.loadPrefMap()
            }
            CLOSE_NOWRITE -> {
                Timber.d("Preference closed with no changes")
            }
        }
    }

    fun notifyLocalChange() {
        isLocalChange.set(true)
    }

    companion object {
        operator fun invoke(file: File) =
                if (Build.VERSION.SDK_INT > 29)
                    PrefObserver(file)
                else
                    PrefObserver(file.absolutePath)
    }
}