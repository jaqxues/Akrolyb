package com.jaqxues.akrolyb.prefs

import com.google.gson.reflect.TypeToken
import com.jaqxues.akrolyb.utils.collectAll
import java.lang.reflect.Type
import kotlin.reflect.KClass


/**
 * This file was created by Jacques Hoffmann (jaqxues) in the Project Instaprefs.<br>
 * Date: 15.03.20 - Time 19:09.
 */

class Preference<T> private constructor(
        val key: String,
        val default: T,
        val type: Types
) {
    constructor(name: String, default: T, type: KClass<*>) : this(name, default, Types.KClassType(type))
    constructor(name: String, default: T, type: Type) : this(name, default, Types.ReflectType(type))

    companion object {
        private lateinit var prefList: Map<String, Preference<*>>

        internal fun collectPreferences(vararg prefClasses: KClass<*>) {
            var map = mapOf<String, Preference<*>>()
            for (prefClass in prefClasses) {
                map = map + prefClass.collectAll<Preference<*>>().associateBy { it.key }
            }
            prefList = map.toMap()
        }

        @Suppress("UNCHECKED_CAST")
        fun <T : Any> findPrefByKey(key: String): Preference<T> {
            return prefList[key] as Preference<T>?
                    ?: throw Exception("Could not resolve Preference $key")
        }
    }
}

sealed class Types {
    class KClassType(val type: KClass<*>) : Types()
    class ReflectType(val type: Type) : Types()

    companion object {
        inline fun <reified T> genericType(): Type = object : TypeToken<T>() {}.type
    }
}

fun Preference<Boolean>.toggle(): Boolean {
    val newValue = !getPref()
    putPref(newValue)
    return newValue
}

fun <T> Preference<List<T>>.add(item: T, allowDuplicate: Boolean = false) {
    val list = getPref()
    val contains = item in list
    if ((contains && allowDuplicate) || !contains) {
        val newList = list.toMutableList()
        newList.add(item)
        putPref(newList)
    }
}

fun <T> Preference<List<T>>.remove(item: T) {
    val list = getPref()
    if (item in list) {
        val newList = list.toMutableList()
        newList.remove(item)
        putPref(newList)
    }
}

operator fun Preference<Boolean>.not() = toggle()

operator fun <T> Preference<List<T>>.plusAssign(item: T) {
    add(item, false)
}

operator fun <T> Preference<List<T>>.minusAssign(item: T) {
    remove(item)
}