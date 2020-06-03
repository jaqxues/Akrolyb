package com.jaqxues.akrolyb.genhook

import com.jaqxues.akrolyb.prefs.Preference
import com.jaqxues.akrolyb.prefs.getPref
import com.jaqxues.akrolyb.prefs.minusAssign
import com.jaqxues.akrolyb.prefs.plusAssign
import kotlin.reflect.KClass


/**
 * This file was created by Jacques Hoffmann (jaqxues) in the Project Instaprefs.<br>
 * Date: 16.03.20 - Time 15:55.
 */
interface FeatureProvider<T: Feature> {
    val disabledFeatures: DisabledFeatures

    val optionalFeatures: Map<String, KClass<out T>>

    val forcedFeatures: Map<String, KClass<out T>>
}

interface DisabledFeatures {
    val list: List<String>
    fun disable(feature: String)
    fun enable(feature: String)

    companion object {
        fun fromPref(disabledPref: Preference<List<String>>) = object : DisabledFeatures {
            override val list: List<String>
                get() = disabledPref.getPref()

            override fun disable(feature: String) {
                disabledPref += feature
            }

            override fun enable(feature: String) {
                disabledPref -= feature
            }
        }
    }
}