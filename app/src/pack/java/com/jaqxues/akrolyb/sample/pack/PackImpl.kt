package com.jaqxues.akrolyb.sample.pack

import android.content.Context
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.jaqxues.akrolyb.genhook.FeatureManager
import com.jaqxues.akrolyb.sample.ipack.AMetadata
import com.jaqxues.akrolyb.sample.ipack.AModPack


/**
 * This file was created by Jacques Hoffmann (jaqxues) in the Project Akrolyb.<br>
 * Date: 20.04.20 - Time 12:25.
 */
class PackImpl(metadata: AMetadata): AModPack(metadata) {
    override fun <T> customMethods(): T {
        throw NotImplementedError()
    }

    override fun getFragments(): Array<Fragment> {
        throw NotImplementedError()
    }

    override fun showSuccessToast(context: Context) {
        Toast.makeText(context, "Pack loaded successfully", Toast.LENGTH_LONG).show()
    }

    override fun loadFeatureManager(): FeatureManager {
        throw NotImplementedError()
    }
}