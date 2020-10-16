package com.jaqxues.akrolyb.prefs

/**
 * This file was created by Jacques Hoffmann (jaqxues) in the Project Akrolyb.<br>
 * Date: 24.05.20 - Time 19:57.
 *
 * Utility functions on Preferences.
 */

// Operations on generic Preferences

inline fun <T> Preference<T>.updatePref(action: (T) -> T) {
    putPref(action(getPref()))
}

inline fun <T, K: T?> Preference<T>.edit(action: (T) -> K): K =
    getPref().let { pref -> (action(pref)).also { edited -> if (pref != edited) putPref(edited) } }


// Operation on other Preferences

fun Preference<Boolean>.toggle(): Boolean {
    val newValue = !getPref()
    putPref(newValue)
    return newValue
}


// Operators
operator fun <T> Preference<T>.invoke() = getPref()
operator fun <T> Preference<T>.invoke(action: (T) -> T) = updatePref(action)
operator fun <T> Preference<T>.invoke(value: T) = putPref(value)
operator fun <T> Preference<T>.unaryMinus() = removePref()

operator fun <T> Preference<out Collection<T>>.contains(element: T) = getPref().contains(element)
operator fun Preference<Boolean>.not() = toggle()
