package com.jaqxues.akrolyb.prefs

import com.google.gson.reflect.TypeToken
import com.jaqxues.akrolyb.utils.collectAll
import java.lang.reflect.Type
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.typeOf


/**
 * This file was created by Jacques Hoffmann (jaqxues) in the Project Instaprefs.<br>
 * Date: 15.03.20 - Time 19:09.
 */

class Preference<T> constructor(val key: String, val default: T, val type: Types) {
    constructor(name: String, default: T, type: KClass<*>) :
            this(name, default, Types.KClassType(type))

    constructor(name: String, default: T, type: Type) :
            this(name, default, Types.ReflectType(type))

    constructor(name: String, default: T, type: KType) :
            this(name, default, Types.KTypeType(type))

    companion object {
        private lateinit var prefList: MutableMap<String, Preference<*>>

        internal fun collectPreferences(vararg prefClasses: KClass<*>) {
            val map = if (::prefList.isInitialized) prefList else mutableMapOf()
            for (prefClass in prefClasses) {
                map += prefClass.collectAll<Preference<*>>().associateBy { it.key }
            }
            prefList = map
        }

        @Suppress("UNCHECKED_CAST")
        fun <T> findPrefByKey(key: String): Preference<T> {
            return prefList[key] as Preference<T>?
                ?: throw Exception("Could not resolve Preference $key")
        }
    }

}

@Suppress("FunctionName")
@OptIn(ExperimentalStdlibApi::class)
inline fun <reified T> Preference(key: String, default: T) =
    Preference(
        key, default, typeOf<T>().let {
            if (it.isMarkedNullable || it.arguments.isNotEmpty())
                Types.KTypeType(it)
            else Types.KClassType(T::class)
        }
    )

sealed class Types {
    class KClassType(val type: KClass<*>) : Types()
    class ReflectType(val type: Type) : Types()
    class KTypeType(val type: KType) : Types()

    companion object {
        inline fun <reified T> genericType(): Type = object : TypeToken<T>() {}.type
    }
}