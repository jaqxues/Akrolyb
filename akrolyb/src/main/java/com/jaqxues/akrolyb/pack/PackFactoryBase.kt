package com.jaqxues.akrolyb.pack

import android.content.Context
import androidx.annotation.CallSuper
import java.io.File
import java.util.jar.Attributes


/**
 * This file was created by Jacques Hoffmann (jaqxues) in the Project Akrolyb.<br>
 * Date: 21.04.20 - Time 11:29.
 */

/**
 * Used to allow for more flexibility, e.g. letting the user set custom metadata on packs. This also checks if it is
 * safe to load a pack in the current environment. If this is not wanted, override [performChecks] and follow the
 * instructions in the documentation of this method.
 */
abstract class PackFactoryBase<T : IPackMetadata> {
    abstract val appData: AppData
    abstract fun buildMeta(attributes: Attributes, context: Context, file: File): T

    open fun getEncryptionKey(packMetadata: T, context: Context, file: File): ByteArray =
        error("Encrypted Packs not supported by PackFactory")

    /**
     * Overridable Method to perform checks.
     * `@CallSuper` enforced since these checks are highly recommended.
     *
     * If you want / need to ignore built-in checks, the super call in a try and catch clause. Please only do this if
     * you know what you are doing and can replace the checks with appropriate other ones.
     *
     * @param packMetadata The given metadata to perform checks on.
     */
    @CallSuper
    open fun performChecks(packMetadata: T, context: Context, file: File) {
        performBasicChecks(packMetadata)
    }

    /**
     * This checks various things to ensure that it is safe to load the Pack. Always called by [performChecks]
     */
    private fun performBasicChecks(packMetadata: T) {
        val data = appData
        // Performing Basic Checks
        if (packMetadata.devPack && !data.debug)
            throw DeveloperPackException()
        if (packMetadata.minApkVersionCode > data.versionCode)
            throw CheckMinApkVersionException(data.versionCode, packMetadata.minApkVersionCode)
        try {
            ModPackBase::class.java.classLoader!!.loadClass(packMetadata.packImplClass)
            throw FoundPackClassloader()
        } catch (ignored: ClassNotFoundException) {}
    }
}

data class AppData(val versionCode: Int, val debug: Boolean)