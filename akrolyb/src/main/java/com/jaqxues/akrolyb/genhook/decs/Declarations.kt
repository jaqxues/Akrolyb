package com.jaqxues.akrolyb.genhook.decs

import com.jaqxues.akrolyb.genhook.Feature
import de.robv.android.xposed.XposedHelpers
import java.lang.reflect.Constructor
import java.lang.reflect.Method


/**
 * This file was created by Jacques Hoffmann (jaqxues) in the Project Instaprefs.<br>
 * Date: 21.07.2019 - Time 00:02.
 * Pulled out to SocialLib on 16.03.2020 - Time 15:00.
 */

private val classCache = mutableMapOf<String, Class<*>>()

open class ClassDec(val className: String) {
    /**
     * Find Classes by ClassDec and ClassLoader.
     *
     * Caching is disabled while initializing Members. [com.marz.instaprefs.xposed.FeatureHelper] has its own little
     * caching. This functionality is reserved for actions that require Classes after resolving Methods, such as
     * accessing Additional Static Fields or "Regular" Static Fields
     *
     * Open: Allows Returning Class by direct reference (e.g. if Class is on BootClassLoader)
     */
    open fun findClass(classLoader: ClassLoader, cache: Boolean = true): Class<*> {
        return if (cache && className in classCache) {
            classCache[className] ?: throw IllegalStateException("Value of key $className null")
        } else {
            XposedHelpers.findClass(className, classLoader).also {
                if (cache)
                    classCache[className] = it
            }
        }
    }
}

sealed class MemberDec(
    open val classDec: ClassDec,
    open val usedInFeature: Array<Class<out Feature>>,
    open vararg val params: Any
) {
    class ConstructorDec(
        override val classDec: ClassDec,
        override val usedInFeature: Array<Class<out Feature>>,
        override vararg val params: Any
    ) : MemberDec(classDec, usedInFeature, params) {
        fun findConstructor(resolvedClass: Class<*>): Constructor<*> =
                XposedHelpers.findConstructorExact(
                        resolvedClass,
                        *params.map {
                            if (it is ClassDec)
                                it.className
                            else
                                it
                        }.toTypedArray()
                )
    }

    class MethodDec(
        override val classDec: ClassDec,
        val methodName: String,
        override val usedInFeature: Array<Class<out Feature>>,
        override vararg val params: Any
    ) : MemberDec(classDec, usedInFeature, params) {
        fun findMethod(resolvedClass: Class<*>): Method {
            return XposedHelpers.findMethodExact(
                    resolvedClass,
                    methodName,
                    *params.map {
                        if (it is ClassDec)
                            it.className
                        else
                            it
                    }.toTypedArray()
            )
        }
    }

    fun findMember(clazz: Class<*>) =
            when (this) {
                is ConstructorDec -> findConstructor(clazz)
                is MethodDec -> findMethod(clazz)
            }
}

inline fun <reified T> VariableDec(name: String) = VariableDec<T>(name, null is T)

@Suppress("UNCHECKED_CAST", "UNUSED", "MemberVisibilityCanBePrivate")
class VariableDec<T>(val name: String, val nullable: Boolean) {
    fun setStaticVar(clazz: Class<*>, value: T) {
        XposedHelpers.setStaticObjectField(clazz, name, checkVal { value })
    }

    fun getStaticVar(clazz: Class<*>) =
            checkVal { XposedHelpers.getStaticObjectField(clazz, name) as T }


    fun setVar(obj: Any, value: T) {
        XposedHelpers.setObjectField(obj, name, checkVal { value })
    }

    fun getVar(obj: Any) =
            checkVal { XposedHelpers.getObjectField(obj, name) as T }

    // Convenience Methods
    fun setStaticVar(classDec: ClassDec, classLoader: ClassLoader, value: T) {
        setStaticVar(classDec.findClass(classLoader), checkVal { value })
    }
    fun getStaticVar(classDec: ClassDec, classLoader: ClassLoader) =
            checkVal { getStaticVar(classDec.findClass(classLoader)) }

    private inline fun <T> checkVal(value: () -> T): T =
            if (nullable) value() else (value() ?:
            throw NullPointerException("Value of VariableDec defined as non-nullable, but received null value"))
}

@Suppress("UNCHECKED_CAST")
class AddInsField<T> {

    // === Additional Fields on Objects ==
    /**
     * @return The previously stored value for this instance/key combination, or null if there was none.
     */
    fun set(obj: Any, value: T?) =
            XposedHelpers.setAdditionalInstanceField(obj, toString(), value) as T?

    fun get(obj: Any) =
            XposedHelpers.getAdditionalInstanceField(obj, toString()) as T?

    // === Static Additional Fields ==
    fun get(clazz: Class<*>) =
            XposedHelpers.getAdditionalStaticField(clazz, toString()) as T?

    /**
     * @return The previously stored value for this instance/key combination, or null if there was none.
     */
    fun set(clazz: Class<*>, value: T?) =
            XposedHelpers.setAdditionalStaticField(clazz, toString(), value) as T?


    // Convenience methods
    fun get(classDec: ClassDec, classLoader: ClassLoader) =
            get(classDec.findClass(classLoader))

    fun set(classDec: ClassDec, classLoader: ClassLoader, value: T?) =
            set(classDec.findClass(classLoader), value)
}
