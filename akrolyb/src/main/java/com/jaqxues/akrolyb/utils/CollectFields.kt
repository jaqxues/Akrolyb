package com.jaqxues.akrolyb.utils

import timber.log.Timber
import kotlin.reflect.KClass


/**
 * This file was created by Jacques Hoffmann (jaqxues) in the Project Instaprefs.<br>
 * Date: 15.03.20 - Time 19:17.
 */

typealias Predicate<T> = (T) -> Boolean

inline fun <reified T: Any> KClass<*>.collectAll(obj: Any? = null, noinline test: Predicate<in T>? = null) =
        CollectFields.collectAll(this.java, T::class.java, obj, test)

object CollectFields {
    /**
     * @param obj May be null for static fields.
     *
     * Ignores fields with values of null
     */
    @Suppress("UNCHECKED_CAST")
    @JvmStatic
    fun <T: Any> collectAll(aClass: Class<*>, type: Class<T>, obj: Any? = null, test: Predicate<in T>? = null) =
            aClass.declaredFields.mapNotNull { field ->
                try {
                    if (!type.isAssignableFrom(field.type))
                        return@mapNotNull null
                    field.isAccessible = true
                    val value = field.get(obj)
                    if (type.isInstance(value) && (test == null || test(value as T)))
                        return@mapNotNull value as T
                } catch (e: IllegalAccessException) {
                    Timber.e(e)
                }
                null
            }
}