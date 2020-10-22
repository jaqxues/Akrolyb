package com.jaqxues.akrolyb.genhook

import kotlin.reflect.KClass


/**
 * This file was created by Jacques Hoffmann (jaqxues) in the Project Instaprefs.<br>
 * Date: 16.03.20 - Time 15:55.
 */
interface FeatureProvider<T: Feature> {
    val disabledFeatures: () -> Set<String>

    val optionalFeatures: Map<String, KClass<out T>>

    val forcedFeatures: Map<String, KClass<out T>>

    val hookDefs: Array<out Any>
}
