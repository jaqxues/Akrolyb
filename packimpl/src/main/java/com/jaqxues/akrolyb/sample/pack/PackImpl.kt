package com.jaqxues.akrolyb.sample.pack

import android.content.Context
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.jaqxues.akrolyb.sample.ipack.ModPack
import com.jaqxues.akrolyb.sample.ipack.PackMetadata
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.XposedHelpers.*


/**
 * This file was created by Jacques Hoffmann (jaqxues) in the Project Akrolyb.<br>
 * Date: 20.04.20 - Time 12:25.
 */
class PackImpl(metadata: PackMetadata): ModPack(metadata) {
    override fun <T> customMethods(): T {
        throw NotImplementedError()
    }

    override fun getFragments(): Array<Fragment> {
        throw NotImplementedError()
    }

    override fun showSuccessToast(context: Context) {
        Toast.makeText(context, "Pack loaded successfully", Toast.LENGTH_LONG).show()
    }

    override fun injectHooks(classLoader: ClassLoader) {
        val cls = findClass("com.jaqxues.akrolyb.sample.target.MainActivity", classLoader)
        val states = getStaticObjectField(cls, "states")
        callMethod(states, "put", "Initialized Pack Hooks", true)
    }
}