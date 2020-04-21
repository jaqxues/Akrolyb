package com.jaqxues.akrolyb.pack

import android.content.Context
import androidx.annotation.CallSuper
import com.jaqxues.akrolyb.BuildConfig
import java.io.File
import java.util.jar.Attributes


/**
 * This file was created by Jacques Hoffmann (jaqxues) in the Project Akrolyb.<br>
 * Date: 21.04.20 - Time 11:29.
 */
abstract class PackFactory<T: PackMetadata> {
    abstract val appData: AppData
    abstract fun buildMeta(attributes: Attributes, context: Context, file: File): T

    @CallSuper
    open fun performChecks(packMetadata: T) {
        performBasicChecks(packMetadata)
    }

    open fun performBasicChecks(packMetadata: T) {
        val data = appData
        // Performing Basic Checks
        if (packMetadata.devPack && !data.debug)
            throw IllegalStateException("Developer Pack with non-debuggable Apk")
        if (packMetadata.minApkVersionCode > data.versionCode)
            throw IllegalStateException("Pack requires newer Apk")
        if (data.appId.endsWith(".pack") || data.flavor == "pack")
            throw IllegalStateException("Detected Pack Flavor as current APK Build")
        try {
            ModPack::class.java.classLoader!!.loadClass(packMetadata.packImplClass)
            throw IllegalStateException("Detected Pack in Classloader")
        } catch (ignored: ClassNotFoundException) {}
    }
}

data class AppData(val versionCode: Int, val debug: Boolean, val appId: String, val flavor: String)