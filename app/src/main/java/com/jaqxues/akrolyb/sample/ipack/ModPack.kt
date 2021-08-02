package com.jaqxues.akrolyb.sample.ipack

import android.content.Context
import androidx.fragment.app.Fragment
import com.jaqxues.akrolyb.pack.PackFactoryBase
import com.jaqxues.akrolyb.pack.AppData
import com.jaqxues.akrolyb.pack.ModPackBase
import com.jaqxues.akrolyb.sample.BuildConfig
import java.io.File
import java.util.jar.Attributes


/**
 * This file was created by Jacques Hoffmann (jaqxues) in the Project Akrolyb.<br>
 * Date: 20.04.20 - Time 12:25.
 */
abstract class ModPack(metadata: PackMetadata) : ModPackBase<PackMetadata>(metadata) {
    abstract fun <T> customMethods(): T
    abstract fun getFragments(): Array<Fragment>
    abstract fun showSuccessToast(context: Context)
    abstract fun injectHooks(classLoader: ClassLoader)
}

object PackFactory: PackFactoryBase<PackMetadata>() {
    override val appData: AppData
        get() = AppData(BuildConfig.VERSION_CODE, BuildConfig.DEBUG)

    override fun buildMeta(attributes: Attributes, context: Context, file: File) =
        PackMetadata.toPackMetadata(attributes, context, file)
}
