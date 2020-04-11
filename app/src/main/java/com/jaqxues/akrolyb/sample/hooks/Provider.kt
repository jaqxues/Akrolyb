package com.jaqxues.akrolyb.sample.hooks

import com.jaqxues.akrolyb.genhook.FeatureManager
import com.jaqxues.akrolyb.genhook.FeatureProvider
import com.jaqxues.akrolyb.sample.hooks.features.Forced
import com.jaqxues.akrolyb.sample.hooks.features.SavingFeature
import com.jaqxues.akrolyb.sample.prefs.Preferences


/**
 * This file was created by Jacques Hoffmann (jaqxues) in the Project Akrolyb.<br>
 * Date: 27.03.20 - Time 20:34.
 */
object Provider {
    val features = FeatureManager(object : FeatureProvider {

        override val disabledFeaturesPref = Preferences.DISABLED_FEATURES

        override val optionalFeatures = mapOf(
            "saving" to SavingFeature::class
        )

        override val forcedFeatures = mapOf(
            "forced" to Forced::class
        )
    })
}