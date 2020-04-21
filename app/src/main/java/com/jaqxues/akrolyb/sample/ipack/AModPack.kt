package com.jaqxues.akrolyb.sample.ipack

import android.content.Context
import androidx.fragment.app.Fragment
import com.jaqxues.akrolyb.pack.AppData
import com.jaqxues.akrolyb.pack.ModPack
import com.jaqxues.akrolyb.pack.PackFactory
import com.jaqxues.akrolyb.sample.BuildConfig
import java.io.File
import java.util.jar.Attributes


/**
 * This file was created by Jacques Hoffmann (jaqxues) in the Project Akrolyb.<br>
 * Date: 20.04.20 - Time 12:25.
 */
abstract class AModPack(metadata: AMetadata) : ModPack<AMetadata>(metadata) {
    abstract fun <T> customMethods(): T
    abstract fun getFragments(): Array<Fragment>
    abstract fun showSuccessToast(context: Context)
}

object APackFactory: PackFactory<AMetadata>() {
    override val appData: AppData
        get() = AppData(BuildConfig.VERSION_CODE, BuildConfig.DEBUG, BuildConfig.APPLICATION_ID, BuildConfig.FLAVOR)

    override fun buildMeta(attributes: Attributes, context: Context, file: File) =
        AMetadata.toPackMetadata(attributes, context, file)
}