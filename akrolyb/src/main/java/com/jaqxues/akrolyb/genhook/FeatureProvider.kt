package com.jaqxues.akrolyb.genhook

import com.jaqxues.akrolyb.prefs.Preference
import kotlin.reflect.KClass


/**
 * This file was created by Jacques Hoffmann (jaqxues) in the Project Instaprefs.<br>
 * Date: 16.03.20 - Time 15:55.
 */
interface FeatureProvider {
    val disabledFeaturesPref: Preference<List<String>>

    val optionalFeatures: Map<String, KClass<out Feature>>

    val forcedFeatures: Map<String, KClass<out Feature>>
}