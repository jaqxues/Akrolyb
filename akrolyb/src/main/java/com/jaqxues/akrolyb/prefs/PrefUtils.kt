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


// Operations on Collection Preferences

fun <T> Preference<out Collection<T>>.add(item: T, allowDuplicate: Boolean = false) {
    val list = getPref()
    val contains = item in list
    if ((contains && allowDuplicate) || !contains) {
        val newList = list.toMutableList()
        newList.add(item)
        putPref(newList)
    }
}

fun <T> Preference<out Collection<T>>.remove(item: T) {
    val list = getPref()
    if (item in list) {
        val newList = list.toMutableList()
        newList.remove(item)
        putPref(newList)
    }
}


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

operator fun Preference<Boolean>.not() = toggle()
operator fun <T> Preference<out Collection<T>>.plusAssign(item: T) = add(item, false)
operator fun <T> Preference<out Collection<T>>.minusAssign(item: T) = remove(item)