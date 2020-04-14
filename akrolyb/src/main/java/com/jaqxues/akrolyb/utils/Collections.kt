package com.jaqxues.akrolyb.utils


/**
 * This file was created by Jacques Hoffmann (jaqxues) in the Project Akrolyb.<br>
 * Date: 14.04.20 - Time 13:29.
 */
inline fun <E, K, V> List<E>.associateNotNull(transform: (E) -> Pair<K, V>?): MutableMap<K, V> {
    val map = mutableMapOf<K, V>()
    for (item in this) {
        val (k, v) = transform(item) ?: continue
        map[k] = v
    }
    return map
}