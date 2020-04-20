package com.jaqxues.akrolyb.sample.ipack

import androidx.fragment.app.Fragment
import com.jaqxues.akrolyb.pack.ModPack


/**
 * This file was created by Jacques Hoffmann (jaqxues) in the Project Akrolyb.<br>
 * Date: 20.04.20 - Time 12:25.
 */
abstract class AModPack(metadata: AMetadata) : ModPack<AMetadata>(metadata) {
    abstract fun <T> customMethods(): T
    abstract fun getFragments(): Array<Fragment>
}