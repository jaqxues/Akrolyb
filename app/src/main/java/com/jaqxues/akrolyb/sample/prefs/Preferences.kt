package com.jaqxues.akrolyb.sample.prefs

import com.jaqxues.akrolyb.prefs.Preference
import com.jaqxues.akrolyb.prefs.Types.Companion.genericType


/**
 * This file was created by Jacques Hoffmann (jaqxues) in the Project Akrolyb.<br>
 * Date: 27.03.20 - Time 20:42.
 */
object Preferences {

    // genericType allows us to use Preference<List<String>> and not Preference<List<*>> (Subtype of List required for Serialization)
    val DISABLED_FEATURES = Preference<List<String>>("disabled_features", emptyList(), genericType<List<String>>())

    val SHOULD_SAVE = Preference("should_save", false, Boolean::class)
}