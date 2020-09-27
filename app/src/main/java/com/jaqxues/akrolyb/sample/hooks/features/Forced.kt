package com.jaqxues.akrolyb.sample.hooks.features

import android.content.Context
import com.jaqxues.akrolyb.genhook.FeatureHelper
import com.jaqxues.akrolyb.genhook.decs.HookWrapper
import com.jaqxues.akrolyb.sample.R
import com.jaqxues.akrolyb.sample.hooks.Declarations


/**
 * This file was created by Jacques Hoffmann (jaqxues) in the Project Akrolyb.<br>
 * Date: 27.03.20 - Time 20:26.
 */
class Forced : FeatureHelper() {
    override fun loadFeature(classLoader: ClassLoader, context: Context) {

        hookMethod(Declarations.FORCED_METHOD, object: HookWrapper() {
            override fun before(param: MethodHookParam) {
                param.result = null
            }
        })
    }
}