package com.jaqxues.akrolyb.pack

import android.content.Context
import com.jaqxues.akrolyb.BuildConfig
import java.io.File
import java.util.jar.Attributes


/**
 * This file was created by Jacques Hoffmann (jaqxues) in the Project Akrolyb.<br>
 * Date: 21.04.20 - Time 11:29.
 */
interface PackFactory<T: PackMetadata> {
    fun buildMeta(attributes: Attributes, context: Context, file: File): T

    fun performChecks(packMetadata: T)

    companion object {
        fun <T: PackMetadata> performBasicChecks(packMetadata: T, data: AppData) {
            // Performing Basic Checks
            if (packMetadata.devPack && !data.debug)
                throw IllegalStateException("Developer Pack with non-debuggable Apk")
            if (packMetadata.minApkVersionCode > data.versionCode)
                throw IllegalStateException("Pack requires newer Apk")
        }
    }
}

data class AppData(val versionCode: Int, val debug: Boolean)