package com.jaqxues.akrolyb.sample.target

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Environment
import android.util.Log
import androidx.core.content.ContextCompat


enum class StorageAccessType(val displayName: String) {
    LEGACY("Legacy"), DENIED("Denied"), SCOPED("Scoped");

    companion object {
        fun determineFromCtx(ctx: Context) = try {
            val hasWritePermission = ContextCompat.checkSelfPermission(
                ctx, Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED

            when {
                !hasWritePermission -> DENIED

                Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
                        && !Environment.isExternalStorageLegacy() -> SCOPED

                else -> LEGACY
            }
        } catch (t: Throwable) {
            Log.e("AkrolybSample", "Could not determine Storage Access Type", t)
            null
        }
    }
}

object Utils {
    fun hookTarget(): Boolean = false
}